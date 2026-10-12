package com.openbooks.opds

import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable
import java.time.Instant

interface OpdsClient {
    suspend fun getCatalog(url: String): OpdsCatalog
    suspend fun searchCatalog(url: String, query: String): OpdsCatalog
    suspend fun getBookDetails(acquisitionLink: String): OpdsBook
    suspend fun downloadBook(acquisitionLink: String, destinationPath: String): Boolean
    fun observeDownloadProgress(): Flow<DownloadProgress>
}

@Serializable
data class OpdsCatalog(
    val id: String,
    val title: String,
    val subtitle: String?,
    val updated: Instant?,
    val books: List<OpdsBook>,
    val navigation: List<OpdsLink>,
    val facets: List<OpdsFacet>
)

@Serializable
data class OpdsBook(
    val id: String,
    val title: String,
    val authors: List<OpdsAuthor>,
    val description: String?,
    val published: Instant?,
    val updated: Instant?,
    val language: String?,
    val subjects: List<String>,
    val links: List<OpdsLink>,
    val coverImage: String?,
    val metadata: Map<String, String>
) {
    fun toBook(): Book {
        val acquisitionLink = links.firstOrNull { it.rel == "http://opds-spec.org/acquisition" }
            ?: links.firstOrNull { it.rel == "http://opds-spec.org/acquisition/open-access" }
        
        val format = acquisitionLink?.type?.let { parseFormat(it) } ?: BookFormat.EPUB
        
        return Book(
            id = id,
            title = title,
            authors = authors.map { it.name },
            description = description,
            coverPath = coverImage,
            filePath = acquisitionLink?.href ?? "",
            format = format,
            fileSize = 0,
            pageCount = 0,
            language = language,
            publisher = null,
            publishedDate = published?.toString(),
            isbn = metadata["isbn"],
            tags = subjects,
            series = metadata["series"],
            seriesIndex = metadata["series_index"]?.toDoubleOrNull()
        )
    }
    
    private fun parseFormat(mimeType: String): BookFormat = when (mimeType) {
        "application/epub+zip" -> BookFormat.EPUB
        "application/pdf" -> BookFormat.PDF
        "application/x-mobipocket-ebook" -> BookFormat.MOBI
        "application/x-fictionbook+xml" -> BookFormat.FB2
        "image/vnd.djvu" -> BookFormat.DJVU
        "text/plain" -> BookFormat.TXT
        "application/zip" -> BookFormat.CBZ
        "application/x-rar-compressed" -> BookFormat.CBR
        "application/vnd.readium.lcp" -> BookFormat.LCP
        else -> BookFormat.EPUB
    }
}

@Serializable
data class OpdsAuthor(
    val name: String,
    val uri: String?
)

@Serializable
data class OpdsLink(
    val href: String,
    val rel: String,
    val type: String?,
    val title: String?,
    val length: Long?
)

@Serializable
data class OpdsFacet(
    val metadata: OpdsFacetMetadata,
    val links: List<OpdsFacetLink>
)

@Serializable
data class OpdsFacetMetadata(
    val type: String,
    val title: String
)

@Serializable
data class OpdsFacetLink(
    val href: String,
    val rel: String,
    val title: String,
    val count: Int?
)

@Serializable
data class DownloadProgress(
    val bookId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val status: DownloadStatus
)

enum class DownloadStatus {
    STARTING, DOWNLOADING, COMPLETE, ERROR, CANCELLED
}

class OpdsClientImpl(
    private val retrofit: retrofit2.Retrofit
) : OpdsClient {
    
    private val api = retrofit.create(OpdsApi::class.java)
    private val downloadProgress = kotlinx.coroutines.channels.Channel<DownloadProgress>()
    
    override suspend fun getCatalog(url: String): OpdsCatalog {
        val response = api.getCatalog(url)
        if (response.isSuccessful) {
            return response.body()!!
        }
        throw Exception("Failed to fetch catalog: ${response.code()}")
    }
    
    override suspend fun searchCatalog(url: String, query: String): OpdsCatalog {
        val response = api.searchCatalog(url, query)
        if (response.isSuccessful) {
            return response.body()!!
        }
        throw Exception("Search failed: ${response.code()}")
    }
    
    override suspend fun getBookDetails(acquisitionLink: String): OpdsBook {
        val response = api.getBookDetails(acquisitionLink)
        if (response.isSuccessful) {
            return response.body()!!
        }
        throw Exception("Failed to get book details: ${response.code()}")
    }
    
    override suspend fun downloadBook(acquisitionLink: String, destinationPath: String): Boolean {
        // Implementation would use OkHttp to download with progress
        return false
    }
    
    override fun observeDownloadProgress() = downloadProgress.receiveAsFlow()
}

interface OpdsApi {
    @retrofit2.http.GET
    suspend fun getCatalog(@retrofit2.http.Url url: String): retrofit2.Response<OpdsCatalog>
    
    @retrofit2.http.GET
    suspend fun searchCatalog(@retrofit2.http.Url url: String, @retrofit2.http.Query("q") query: String): retrofit2.Response<OpdsCatalog>
    
    @retrofit2.http.GET
    suspend fun getBookDetails(@retrofit2.http.Url url: String): retrofit2.Response<OpdsBook>
}