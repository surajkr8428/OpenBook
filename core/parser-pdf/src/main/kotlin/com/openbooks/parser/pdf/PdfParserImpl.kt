package com.openbooks.parser.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
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
import org.apache.pdfbox.Loader
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.pdfbox.text.PDFTextStripperByArea
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject

class PdfParserImpl @Inject constructor() : BookParser {
    override val supportedFormats = setOf(BookFormat.PDF)

    override suspend fun parseBook(filePath: String): ParsedBook {
        val file = File(filePath)
        val document = Loader.loadPDF(file)
        
        val metadata = extractMetadataFromPdf(document, filePath, file.length())
        val toc = extractTOC(document)
        val coverImage = extractCoverImage(document)
        document.close()

        val book = Book(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            authors = metadata.authors,
            description = metadata.description,
            coverPath = null,
            filePath = filePath,
            format = BookFormat.PDF,
            fileSize = file.length(),
            pageCount = metadata.pageCount,
            language = metadata.language,
            publisher = metadata.publisher,
            publishedDate = metadata.publishedDate,
            isbn = metadata.isbn,
            tags = emptyList(),
            series = null,
            seriesIndex = null
        )

        return ParsedBook(
            book = book,
            chapters = extractChapters(document, filePath),
            toc = toc,
            coverImage = coverImage,
            metadata = metadata
        )
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata {
        val file = File(filePath)
        val document = Loader.loadPDF(file)
        val metadata = extractMetadataFromPdf(document, filePath, file.length())
        document.close()
        return metadata
    }

    override suspend fun getTableOfContents(filePath: String): List<TOCEntry> {
        val file = File(filePath)
        val document = Loader.loadPDF(file)
        val toc = extractTOC(document)
        document.close()
        return toc
    }

    override suspend fun getCoverImage(filePath: String): ByteArray? {
        val file = File(filePath)
        val document = Loader.loadPDF(file)
        val cover = extractCoverImage(document)
        document.close()
        return cover
    }

    override suspend fun getPageCount(filePath: String): Int {
        val file = File(filePath)
        val document = Loader.loadPDF(file)
        val count = document.numberOfPages
        document.close()
        return count
    }

    override fun canParse(filePath: String): Boolean {
        return filePath.lowercase().endsWith(".pdf")
    }

    private fun extractMetadataFromPdf(document: PDDocument, filePath: String, fileSize: Long): BookMetadata {
        val info = document.documentInformation
        val stripper = PDFTextStripper()
        val text = stripper.getText(document)
        val wordCount = text.split("\\s+".toRegex()).count { it.isNotBlank() }.toLong()

        return BookMetadata(
            title = info.title ?: File(filePath).nameWithoutExtension,
            authors = if (info.author.isNullOrBlank()) emptyList() else listOf(info.author!!),
            description = info.subject,
            publisher = null,
            publishedDate = info.creationDate?.time?.toString(),
            isbn = null,
            language = null,
            subject = info.subject,
            rights = null,
            identifier = null,
            format = BookFormat.PDF,
            pageCount = document.numberOfPages,
            wordCount = wordCount,
            customMetadata = mapOf(
                "creator" to info.creator ?: "",
                "producer" to info.producer ?: "",
                "keywords" to info.keywords ?: ""
            )
        )
    }

    private fun extractChapters(document: PDDocument, filePath: String): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        val outline = document.documentCatalog.documentOutline
        
        if (outline != null) {
            fun processOutline(item: org.apache.pdfbox.pdmodel.interaction.documentnavigation.outline.PDOutlineItem, level: Int, order: Int): Int {
                var currentOrder = order
                chapters.add(Chapter(
                    id = "outline_$currentOrder",
                    title = item.title ?: "Chapter ${currentOrder + 1}",
                    href = "page_${item.destination?.pageNumber ?: currentOrder}",
                    order = currentOrder,
                    level = level
                ))
                currentOrder++
                var child = item.firstChild
                while (child != null) {
                    currentOrder = processOutline(child, level + 1, currentOrder)
                    child = child.nextSibling
                }
                return currentOrder
            }
            
            var child = outline.firstChild
            var order = 0
            while (child != null) {
                order = processOutline(child, 1, order)
                child = child.nextSibling
            }
        }
        
        // If no outline, create chapters per N pages
        if (chapters.isEmpty()) {
            val pagesPerChapter = 10
            for (i in 0 until document.numberOfPages step pagesPerChapter) {
                chapters.add(Chapter(
                    id = "pages_${i}_${min(i + pagesPerChapter, document.numberOfPages)}",
                    title = "Pages ${i + 1}-${min(i + pagesPerChapter, document.numberOfPages)}",
                    href = "page_$i",
                    order = i / pagesPerChapter,
                    level = 1
                ))
            }
        }
        
        return chapters
    }

    private fun extractTOC(document: PDDocument): List<TOCEntry> {
        val toc = mutableListOf<TOCEntry>()
        val outline = document.documentCatalog.documentOutline
        
        if (outline != null) {
            fun processOutline(item: org.apache.pdfbox.pdmodel.interaction.documentnavigation.outline.PDOutlineItem, level: Int): TOCEntry {
                val entry = TOCEntry(
                    id = "outline_${toc.size}",
                    title = item.title ?: "Section",
                    href = "page_${item.destination?.pageNumber ?: 0}",
                    level = level
                )
                var child = item.firstChild
                val children = mutableListOf<TOCEntry>()
                while (child != null) {
                    children.add(processOutline(child, level + 1))
                    child = child.nextSibling
                }
                return entry.copy(children = children)
            }
            
            var child = outline.firstChild
            while (child != null) {
                toc.add(processOutline(child, 1))
                child = child.nextSibling
            }
        }
        
        return toc
    }

    private fun extractCoverImage(document: PDDocument): ByteArray? {
        // Try to extract first page as cover
        if (document.numberOfPages > 0) {
            try {
                val renderer = PdfRenderer(ParcelFileDescriptor.open(File("temp"), ParcelFileDescriptor.MODE_READ_ONLY))
                val page = renderer.openPage(0)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                
                val outputStream = java.io.ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
                return outputStream.toByteArray()
            } catch (e: Exception) {
                return null
            }
        }
        return null
    }

    private fun min(a: Int, b: Int) = if (a < b) a else b
}

class PdfRendererImpl @Inject constructor() : BookRenderer {
    override val format = BookFormat.PDF
    
    private var settings = RenderSettings(
        fontFamily = "System Default",
        fontSize = 1.0f,
        lineHeight = 1.5f,
        margin = 1.0f,
        theme = "SYSTEM",
        backgroundColor = 0xFFFFFFFF,
        textColor = 0xFF000000,
        hyphenation = true,
        justification = "JUSTIFIED",
        paragraphSpacing = 0f,
        customFontPath = null
    )

    override suspend fun renderPage(bookPath: String, pageNumber: Int): ParsedPage {
        val file = File(bookPath)
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        
        val page = renderer.openPage(pageNumber)
        val width = page.width
        val height = page.height
        
        // Extract text for this page
        val document = Loader.loadPDF(file)
        val stripper = PDFTextStripper()
        stripper.startPage = pageNumber + 1
        stripper.endPage = pageNumber + 1
        val text = stripper.getText(document)
        document.close()
        
        page.close()
        renderer.close()
        descriptor.close()

        return ParsedPage(
            pageNumber = pageNumber + 1,
            content = text,
            chapterIndex = 0,
            chapterProgress = (pageNumber + 1).toFloat() / renderer.pageCount,
            cfi = "pdf://page/$pageNumber",
            images = extractImages(pageNumber, bookPath)
        )
    }

    override suspend fun renderPageByCfi(bookPath: String, cfi: String): ParsedPage {
        val pageNumber = cfi.removePrefix("pdf://page/").toIntOrNull() ?: 0
        return renderPage(bookPath, pageNumber)
    }

    override suspend fun getTotalPages(bookPath: String, settings: RenderSettings): Int {
        val file = File(bookPath)
        val document = Loader.loadPDF(file)
        val count = document.numberOfPages
        document.close()
        return count
    }

    override suspend fun searchInBook(bookPath: String, query: String): List<SearchResult> {
        val results = mutableListOf<SearchResult>()
        val file = File(bookPath)
        val document = Loader.loadPDF(file)
        
        for (i in 0 until document.numberOfPages) {
            val stripper = PDFTextStripper()
            stripper.startPage = i + 1
            stripper.endPage = i + 1
            val text = stripper.getText(document)
            
            if (text.contains(query, ignoreCase = true)) {
                // Find context around match
                val index = text.indexOf(query, ignoreCase = true)
                val start = max(0, index - 100)
                val end = min(text.length, index + query.length + 100)
                val context = text.substring(start, end)
                
                results.add(SearchResult(
                    chapterIndex = 0,
                    pageNumber = i + 1,
                    context = context,
                    cfi = "pdf://page/$i"
                ))
            }
        }
        
        document.close()
        return results
    }

    override fun getSettings(): RenderSettings = settings

    override fun updateSettings(newSettings: RenderSettings) {
        settings = newSettings
    }

    private suspend fun extractImages(pageNumber: Int, bookPath: String): List<PageImage> {
        // PDF image extraction would go here
        return emptyList()
    }
}