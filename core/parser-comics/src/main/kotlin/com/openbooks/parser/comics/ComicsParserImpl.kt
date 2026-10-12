package com.openbooks.parser.comics

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.domain.parser.BookMetadata
import com.openbooks.domain.parser.BookParser
import com.openbooks.domain.parser.BookRenderer
import com.openbooks.domain.parser.ParsedBook
import com.openbooks.domain.parser.ParsedPage
import com.openbooks.domain.parser.PageImage
import com.openbooks.domain.parser.RenderSettings
import com.openbooks.domain.parser.SearchResult
import com.openbooks.domain.parser.TOCEntry
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.IOUtils
import junrar.Archive
import junrar.rarfile.FileHeader
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.inject.Inject

class ComicsParserImpl @Inject constructor() : BookParser {
    override val supportedFormats = setOf(BookFormat.CBZ, BookFormat.CBR)

    override suspend fun parseBook(filePath: String): ParsedBook {
        val file = File(filePath)
        val isCbz = filePath.lowercase().endsWith(".cbz")
        val isCbr = filePath.lowercase().endsWith(".cbr")
        
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        val metadata = extractMetadata(imageFiles, filePath, file.length())
        
        val book = Book(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            authors = metadata.authors,
            description = metadata.description,
            coverPath = null,
            filePath = filePath,
            format = if (isCbz) BookFormat.CBZ else BookFormat.CBR,
            fileSize = file.length(),
            pageCount = imageFiles.size,
            language = metadata.language,
            publisher = metadata.publisher,
            publishedDate = metadata.publishedDate,
            isbn = metadata.isbn,
            tags = emptyList(),
            series = null,
            seriesIndex = null
        )

        val chapters = imageFiles.mapIndexed { index, name ->
            Chapter(
                id = "page_$index",
                title = "Page ${index + 1}",
                href = name,
                order = index,
                level = 1
            )
        }

        val toc = chapters.map { chapter ->
            TOCEntry(
                id = chapter.id,
                title = chapter.title,
                href = chapter.href,
                level = 1
            )
        }

        val coverImage = if (imageFiles.isNotEmpty()) {
            extractFirstImage(file, imageFiles.first(), isCbz)
        } else null

        return ParsedBook(
            book = book,
            chapters = chapters,
            toc = toc,
            coverImage = coverImage,
            metadata = metadata
        )
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata {
        val file = File(filePath)
        val isCbz = filePath.lowercase().endsWith(".cbz")
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        return extractMetadata(imageFiles, filePath, file.length())
    }

    override suspend fun getTableOfContents(filePath: String): List<TOCEntry> {
        val file = File(filePath)
        val isCbz = filePath.lowercase().endsWith(".cbz")
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        
        return imageFiles.mapIndexed { index, name ->
            TOCEntry(
                id = "page_$index",
                title = "Page ${index + 1}",
                href = name,
                level = 1
            )
        }
    }

    override suspend fun getCoverImage(filePath: String): ByteArray? {
        val file = File(filePath)
        val isCbz = filePath.lowercase().endsWith(".cbz")
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        return if (imageFiles.isNotEmpty()) extractFirstImage(file, imageFiles.first(), isCbz) else null
    }

    override suspend fun getPageCount(filePath: String): Int {
        val file = File(filePath)
        val isCbz = filePath.lowercase().endsWith(".cbz")
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        return imageFiles.size
    }

    override fun canParse(filePath: String): Boolean {
        val lower = filePath.lowercase()
        return lower.endsWith(".cbz") || lower.endsWith(".cbr")
    }

    private fun extractCbzImages(file: File): List<String> {
        val images = mutableListOf<String>()
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.isDirectory && isImageFile(entry.name)) {
                    images.add(entry.name)
                }
            }
        }
        return images.sortedWith(compareByNatural())
    }

    private fun extractCbrImages(file: File): List<String> {
        val images = mutableListOf<String>()
        val archive = Archive(FileInputStream(file))
        val header = archive.nextFileHeader
        while (header != null) {
            if (!header.isDirectory && isImageFile(header.fileNameW)) {
                images.add(header.fileNameW)
            }
            archive.nextFileHeader
        }
        return images.sortedWith(compareByNatural())
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
               lower.endsWith(".png") || lower.endsWith(".webp") ||
               lower.endsWith(".bmp") || lower.endsWith(".gif")
    }

    private fun extractMetadata(imageFiles: List<String>, filePath: String, fileSize: Long): BookMetadata {
        val fileName = File(filePath).nameWithoutExtension
        return BookMetadata(
            title = fileName,
            authors = emptyList(),
            description = null,
            publisher = null,
            publishedDate = null,
            isbn = null,
            language = null,
            subject = null,
            rights = null,
            identifier = null,
            format = if (filePath.lowercase().endsWith(".cbz")) BookFormat.CBZ else BookFormat.CBR,
            pageCount = imageFiles.size,
            wordCount = 0,
            customMetadata = emptyMap()
        )
    }

    private fun extractFirstImage(file: File, firstImageName: String, isCbz: Boolean): ByteArray? {
        return if (isCbz) {
            ZipFile(file).use { zipFile ->
                val entry = zipFile.getEntry(firstImageName)
                entry?.let { zipFile.getInputStream(it)?.readAllBytes() }
            }
        } else {
            val archive = Archive(FileInputStream(file))
            var header = archive.nextFileHeader
            while (header != null) {
                if (header.fileNameW == firstImageName) {
                    val buffer = java.io.ByteArrayOutputStream()
                    IOUtils.copy(archive.getInputStream(header), buffer)
                    return buffer.toByteArray()
                }
                header = archive.nextFileHeader
            }
            null
        }
    }
}

class ComicsRendererImpl @Inject constructor() : BookRenderer {
    override val format = BookFormat.CBZ
    
    private var settings = RenderSettings(
        fontFamily = "System Default",
        fontSize = 1.0f,
        lineHeight = 1.5f,
        margin = 0f,
        theme = "DARK",
        backgroundColor = 0xFF000000,
        textColor = 0xFFFFFFFF,
        hyphenation = false,
        justification = "CENTER",
        paragraphSpacing = 0f,
        customFontPath = null
    )

    override suspend fun renderPage(bookPath: String, pageNumber: Int): ParsedPage {
        val file = File(bookPath)
        val isCbz = bookPath.lowercase().endsWith(".cbz")
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        
        val imageName = imageFiles.getOrNull(pageNumber)
        val imageData = imageName?.let { extractImageBytes(file, it, isCbz) }
        
        return ParsedPage(
            pageNumber = pageNumber + 1,
            content = "",
            chapterIndex = pageNumber,
            chapterProgress = (pageNumber + 1).toFloat() / imageFiles.size,
            cfi = "comics://page/$pageNumber",
            images = imageData?.let {
                listOf(PageImage(
                    id = "page_$pageNumber",
                    mimeType = getMimeType(imageName!!),
                    data = it,
                    width = 0,
                    height = 0,
                    altText = null
                ))
            } ?: emptyList()
        )
    }

    override suspend fun renderPageByCfi(bookPath: String, cfi: String): ParsedPage {
        val pageNumber = cfi.removePrefix("comics://page/").toIntOrNull() ?: 0
        return renderPage(bookPath, pageNumber)
    }

    override suspend fun getTotalPages(bookPath: String, settings: RenderSettings): Int {
        val file = File(bookPath)
        val isCbz = bookPath.lowercase().endsWith(".cbz")
        val imageFiles = if (isCbz) extractCbzImages(file) else extractCbrImages(file)
        return imageFiles.size
    }

    override suspend fun searchInBook(bookPath: String, query: String): List<SearchResult> {
        return emptyList() // Comics don't have searchable text
    }

    override fun getSettings(): RenderSettings = settings

    override fun updateSettings(newSettings: RenderSettings) {
        settings = newSettings
    }

    private fun extractCbzImages(file: File): List<String> {
        val images = mutableListOf<String>()
        ZipFile(file).use { zipFile ->
            val entries = zipFile.entries
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (!entry.isDirectory && isImageFile(entry.name)) {
                    images.add(entry.name)
                }
            }
        }
        return images.sortedWith(compareByNatural())
    }

    private fun extractCbrImages(file: File): List<String> {
        val images = mutableListOf<String>()
        val archive = Archive(FileInputStream(file))
        var header = archive.nextFileHeader
        while (header != null) {
            if (!header.isDirectory && isImageFile(header.fileNameW)) {
                images.add(header.fileNameW)
            }
            header = archive.nextFileHeader
        }
        return images.sortedWith(compareByNatural())
    }

    private fun isImageFile(name: String): Boolean {
        val lower = name.lowercase()
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
               lower.endsWith(".png") || lower.endsWith(".webp") ||
               lower.endsWith(".bmp") || lower.endsWith(".gif")
    }

    private fun extractImageBytes(file: File, imageName: String, isCbz: Boolean): ByteArray? {
        return if (isCbz) {
            ZipFile(file).use { zipFile ->
                val entry = zipFile.getEntry(imageName)
                entry?.let { zipFile.getInputStream(it)?.readAllBytes() }
            }
        } else {
            val archive = Archive(FileInputStream(file))
            var header = archive.nextFileHeader
            while (header != null) {
                if (header.fileNameW == imageName) {
                    val buffer = java.io.ByteArrayOutputStream()
                    IOUtils.copy(archive.getInputStream(header), buffer)
                    return buffer.toByteArray()
                }
                header = archive.nextFileHeader
            }
            null
        }
    }

    private fun getMimeType(fileName: String): String {
        return when (fileName.lowercase()) {
            *.endsWith(".jpg"), *.endsWith(".jpeg") -> "image/jpeg"
            *.endsWith(".png") -> "image/png"
            *.endsWith(".webp") -> "image/webp"
            *.endsWith(".bmp") -> "image/bmp"
            *.endsWith(".gif") -> "image/gif"
            else -> "application/octet-stream"
        }
    }
}