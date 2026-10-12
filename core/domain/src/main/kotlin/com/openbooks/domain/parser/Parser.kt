package com.openbooks.domain.parser

import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import kotlinx.coroutines.flow.Flow

interface BookParser {
    val supportedFormats: Set<BookFormat>
    
    suspend fun parseBook(filePath: String): ParsedBook
    suspend fun extractMetadata(filePath: String): BookMetadata
    suspend fun getTableOfContents(filePath: String): List<TOCEntry>
    suspend fun getCoverImage(filePath: String): ByteArray?
    suspend fun getPageCount(filePath: String): Int
    fun canParse(filePath: String): Boolean
}

interface EpubParser : BookParser
interface PdfParser : BookParser
interface ComicsParser : BookParser
interface AudiobookParser : BookParser

@Serializable
data class ParsedBook(
    val book: Book,
    val chapters: List<Chapter>,
    val toc: List<TOCEntry>,
    val coverImage: ByteArray?,
    val metadata: BookMetadata
)

@Serializable
data class BookMetadata(
    val title: String,
    val authors: List<String>,
    val description: String?,
    val publisher: String?,
    val publishedDate: String?,
    val isbn: String?,
    val language: String?,
    val subject: String?,
    val rights: String?,
    val identifier: String?,
    val format: BookFormat,
    val pageCount: Int,
    val wordCount: Long,
    val customMetadata: Map<String, String>
)

@Serializable
data class Chapter(
    val id: String,
    val title: String,
    val href: String,
    val order: Int,
    val level: Int,
    val subChapters: List<Chapter> = emptyList(),
    val content: String? = null
)

@Serializable
data class TOCEntry(
    val id: String,
    val title: String,
    val href: String,
    val level: Int,
    val children: List<TOCEntry> = emptyList()
)

@Serializable
data class ParsedPage(
    val pageNumber: Int,
    val content: String,
    val chapterIndex: Int,
    val chapterProgress: Float,
    val cfi: String?,
    val images: List<PageImage>
)

@Serializable
data class PageImage(
    val id: String,
    val mimeType: String,
    val data: ByteArray,
    val width: Int,
    val height: Int,
    val altText: String?
)

interface BookRenderer {
    val format: BookFormat
    
    suspend fun renderPage(bookPath: String, pageNumber: Int): ParsedPage
    suspend fun renderPageByCfi(bookPath: String, cfi: String): ParsedPage
    suspend fun getTotalPages(bookPath: String, settings: RenderSettings): Int
    suspend fun searchInBook(bookPath: String, query: String): List<SearchResult>
    fun getSettings(): RenderSettings
    fun updateSettings(settings: RenderSettings)
}

@Serializable
data class RenderSettings(
    val fontFamily: String,
    val fontSize: Float,
    val lineHeight: Float,
    val margin: Float,
    val theme: String,
    val backgroundColor: Int,
    val textColor: Int,
    val hyphenation: Boolean,
    val justification: String,
    val paragraphSpacing: Float,
    val customFontPath: String?
)

@Serializable
data class SearchResult(
    val chapterIndex: Int,
    val pageNumber: Int,
    val context: String,
    val cfi: String?
)