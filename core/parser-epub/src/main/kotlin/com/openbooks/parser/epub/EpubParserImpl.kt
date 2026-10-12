package com.openbooks.parser.epub

import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.domain.parser.BookMetadata
import com.openbooks.domain.parser.BookParser
import com.openbooks.domain.parser.Chapter
import com.openbooks.domain.parser.ParsedBook
import com.openbooks.domain.parser.TOCEntry
import nl.siegmann.epublib.domain.Book as EpubBook
import nl.siegmann.epublib.domain.Resource
import nl.siegmann.epublib.epub.EpubReader
import nl.siegmann.epublib.service.MediatypeService
import org.jetbrains.kotlinx.io.core.buffer
import org.jetbrains.kotlinx.io.core.readBytes
import java.io.File
import java.io.FileInputStream
import java.util.UUID
import javax.inject.Inject

class EpubParserImpl @Inject constructor() : BookParser {
    override val supportedFormats = setOf(BookFormat.EPUB)

    private val epubReader = EpubReader()
    private val mediatypeService = MediatypeService()

    override suspend fun parseBook(filePath: String): ParsedBook {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val epubBook = epubReader.readEpub(inputStream)
        inputStream.close()

        val metadata = extractMetadataFromEpub(epubBook, filePath, file.length())
        val chapters = extractChapters(epubBook)
        val toc = extractTOC(epubBook)
        val coverImage = extractCoverImage(epubBook)

        val book = Book(
            id = UUID.randomUUID().toString(),
            title = metadata.title,
            authors = metadata.authors,
            description = metadata.description,
            coverPath = null,
            filePath = filePath,
            format = BookFormat.EPUB,
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
            chapters = chapters,
            toc = toc,
            coverImage = coverImage,
            metadata = metadata
        )
    }

    override suspend fun extractMetadata(filePath: String): BookMetadata {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val epubBook = epubReader.readEpub(inputStream)
        inputStream.close()
        return extractMetadataFromEpub(epubBook, filePath, file.length())
    }

    override suspend fun getTableOfContents(filePath: String): List<TOCEntry> {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val epubBook = epubReader.readEpub(inputStream)
        inputStream.close()
        return extractTOC(epubBook)
    }

    override suspend fun getCoverImage(filePath: String): ByteArray? {
        val file = File(filePath)
        val inputStream = FileInputStream(file)
        val epubBook = epubReader.readEpub(inputStream)
        inputStream.close()
        return extractCoverImage(epubBook)
    }

    override suspend fun getPageCount(filePath: String): Int {
        val metadata = extractMetadata(filePath)
        return metadata.pageCount
    }

    override fun canParse(filePath: String): Boolean {
        return filePath.lowercase().endsWith(".epub")
    }

    private fun extractMetadataFromEpub(epubBook: EpubBook, filePath: String, fileSize: Long): BookMetadata {
        val metadata = epubBook.metadata
        val spine = epubBook.spine
        
        var pageCount = spine.spineReferences.size * 10 // Rough estimate
        
        return BookMetadata(
            title = metadata.titles.firstOrNull() ?: File(filePath).nameWithoutExtension,
            authors = metadata.authors.map { it.name }.toList(),
            description = metadata.descriptions.firstOrNull(),
            publisher = metadata.publishers.firstOrNull(),
            publishedDate = metadata.dates.firstOrNull()?.toString(),
            isbn = metadata.identifiers.firstOrNull { it.id.startsWith("ISBN") }?.id,
            language = metadata.languages.firstOrNull(),
            subject = metadata.subjects.firstOrNull(),
            rights = metadata.rights.firstOrNull(),
            identifier = metadata.identifiers.firstOrNull()?.id,
            format = BookFormat.EPUB,
            pageCount = pageCount,
            wordCount = 0,
            customMetadata = emptyMap()
        )
    }

    private fun extractChapters(epubBook: EpubBook): List<Chapter> {
        val chapters = mutableListOf<Chapter>()
        var order = 0
        
        for (spineRef in epubBook.spine.spineReferences) {
            val resource = spineRef.resource
            if (resource.mediatype == MediatypeService.XHTML || 
                resource.mediatype == MediatypeService.HTML) {
                val content = resource.inputStream?.buffer()?.readBytes()?.decodeToString() ?: ""
                chapters.add(Chapter(
                    id = resource.href,
                    title = resource.title ?: "Chapter ${order + 1}",
                    href = resource.href,
                    order = order++,
                    level = 1,
                    content = content
                ))
            }
        }
        return chapters
    }

    private fun extractTOC(epubBook: EpubBook): List<TOCEntry> {
        val toc = mutableListOf<TOCEntry>()
        
        fun processTOCReferences(references: List<nl.siegmann.epublib.domain.TOCReference>, level: Int): List<TOCEntry> {
            return references.map { ref ->
                TOCEntry(
                    id = ref.resource?.href ?: UUID.randomUUID().toString(),
                    title = ref.title,
                    href = ref.resource?.href ?: "",
                    level = level,
                    children = processTOCReferences(ref.children, level + 1)
                )
            }
        }
        
        return processTOCReferences(epubBook.tableOfContents.tocReferences, 1)
    }

    private fun extractCoverImage(epubBook: EpubBook): ByteArray? {
        val coverId = epubBook.metadata.metaAttributes.firstOrNull { it.name == "cover" }?.content
        if (coverId != null) {
            val coverResource = epubBook.resources.getById(coverId)
            return coverResource?.inputStream?.buffer()?.readBytes()
        }
        
        // Fallback: first image resource
        return epubBook.resources.all.firstOrNull { it.mediatype.startsWith("image/") }
            ?.inputStream?.buffer()?.readBytes()
    }
}