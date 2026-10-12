package com.openbooks.scanner

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.openbooks.core.data.db.AppDatabase
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.domain.parser.BookParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.concurrent.Executors
import javax.inject.Inject

class BookScanner @Inject constructor(
    private val context: Context,
    private val database: AppDatabase,
    private val epubParser: BookParser,
    private val pdfParser: BookParser,
    private val comicsParser: BookParser,
    private val lcpParser: BookParser
) {
    private val _scanProgress = MutableStateFlow(ScanProgress(0, 0, "", ScanStatus.IDLE))
    val scanProgress = _scanProgress.asStateFlow()
    
    private val parsers = mapOf(
        BookFormat.EPUB to epubParser,
        BookFormat.PDF to pdfParser,
        BookFormat.CBZ to comicsParser,
        BookFormat.CBR to comicsParser,
        BookFormat.LCP to lcpParser
    )
    
    private val supportedExtensions = setOf(".epub", ".pdf", ".mobi", ".fb2", ".djvu", ".txt", ".cbz", ".cbr", ".lcpl", ".lcp")

    suspend fun scanLibrary(): ScanResult {
        _scanProgress.value = ScanProgress(0, 0, "Starting scan...", ScanStatus.SCANNING)
        
        val files = findBookFiles()
        _scanProgress.value = _scanProgress.value.copy(
            total = files.size,
            status = ScanStatus.PARSING
        )
        
        var added = 0
        var updated = 0
        var errors = 0
        
        for ((index, file) in files.withIndex()) {
            _scanProgress.value = _scanProgress.value.copy(
                current = index + 1,
                currentFile = file.name
            )
            
            try {
                val result = processBookFile(file)
                if (result == ScanBookResult.ADDED) added++
                else if (result == ScanBookResult.UPDATED) updated++
            } catch (e: Exception) {
                errors++
            }
        }
        
        _scanProgress.value = ScanProgress(
            current = files.size,
            total = files.size,
            currentFile = "Complete",
            status = ScanStatus.COMPLETE
        )
        
        return ScanResult(added = added, updated = updated, errors = errors, totalScanned = files.size)
    }

    private fun findBookFiles(): List<File> {
        val files = mutableListOf<File>()
        val executor = Executors.newSingleThreadExecutor()
        
        try {
            // Scan external storage directories
            val directories = getScanDirectories()
            
            for (dir in directories) {
                if (dir.exists() && dir.isDirectory) {
                    findFilesRecursive(dir, files)
                }
            }
            
            // Also scan MediaStore for books
            scanMediaStore(files)
            
        } finally {
            executor.shutdown()
        }
        
        return files.distinctBy { it.absolutePath }
    }

    private fun getScanDirectories(): List<File> {
        val dirs = mutableListOf<File>()
        
        // Standard directories
        dirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
        dirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS))
        dirs.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_BOOKS))
        
        // App-specific directory
        dirs.add(File(context.getExternalFilesDir(null)?.parentFile?.parentFile, "Books"))
        
        // Custom directories from preferences
        // TODO: Load from user preferences
        
        return dirs.filter { it.exists() }
    }

    private fun findFilesRecursive(dir: File, files: MutableList<File>) {
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                if (!file.name.startsWith(".")) {
                    findFilesRecursive(file, files)
                }
            } else if (isSupportedBookFile(file)) {
                files.add(file)
            }
        }
    }

    private fun isSupportedBookFile(file: File): Boolean {
        val name = file.name.lowercase()
        return supportedExtensions.any { name.endsWith(it) }
    }

    private fun scanMediaStore(files: MutableList<File>) {
        try {
            val projection = arrayOf(MediaStore.Files.FileColumns.DATA, MediaStore.Files.FileColumns.MIME_TYPE)
            val selection = "${MediaStore.Files.FileColumns.MIME_TYPE} IN (?, ?, ?, ?, ?, ?, ?, ?, ?)"
            val selectionArgs = arrayOf(
                "application/epub+zip",
                "application/pdf",
                "application/x-mobipocket-ebook",
                "application/x-fictionbook+xml",
                "image/vnd.djvu",
                "text/plain",
                "application/zip",
                "application/x-rar-compressed",
                "application/vnd.readium.lcp"
            )
            
            val uri = MediaStore.Files.getContentUri("external")
            context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA))
                    path?.let { files.add(File(it)) }
                }
            }
        } catch (e: Exception) {
            // MediaStore not available or permission denied
        }
    }

    private fun processBookFile(file: File): ScanBookResult {
        val format = detectFormat(file)
        val parser = parsers[format] ?: return ScanBookResult.ERROR
        
        if (!parser.canParse(file.absolutePath)) {
            return ScanBookResult.ERROR
        }
        
        // Check if book already exists in database
        val existingBook = database.bookDao().getByPathSync(file.absolutePath)
        
        if (existingBook != null) {
            // Update if file modified
            if (existingBook.fileSize != file.length() || existingBook.lastOpened == null) {
                val metadata = parser.extractMetadata(file.absolutePath)
                val updatedBook = existingBook.copy(
                    title = metadata.title,
                    authors = metadata.authors,
                    fileSize = file.length(),
                    pageCount = metadata.pageCount
                )
                database.bookDao().update(updatedBook)
                return ScanBookResult.UPDATED
            }
            return ScanBookResult.SKIPPED
        } else {
            // Add new book
            val metadata = parser.extractMetadata(file.absolutePath)
            val book = Book(
                id = java.util.UUID.randomUUID().toString(),
                title = metadata.title,
                authors = metadata.authors,
                description = metadata.description,
                coverPath = null,
                filePath = file.absolutePath,
                format = format,
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
            database.bookDao().insert(book)
            
            // Extract cover in background
            CoroutineScope(Dispatchers.IO).launch {
                val cover = parser.getCoverImage(file.absolutePath)
                cover?.let { saveCover(book.id, it) }
            }
            
            return ScanBookResult.ADDED
        }
    }

    private fun detectFormat(file: File): BookFormat {
        val name = file.name.lowercase()
        return when {
            name.endsWith(".epub") -> BookFormat.EPUB
            name.endsWith(".pdf") -> BookFormat.PDF
            name.endsWith(".mobi") -> BookFormat.MOBI
            name.endsWith(".fb2") -> BookFormat.FB2
            name.endsWith(".djvu") -> BookFormat.DJVU
            name.endsWith(".txt") -> BookFormat.TXT
            name.endsWith(".cbz") -> BookFormat.CBZ
            name.endsWith(".cbr") -> BookFormat.CBR
            name.endsWith(".lcpl"), name.endsWith(".lcp") -> BookFormat.LCP
            else -> BookFormat.EPUB
        }
    }

    private fun saveCover(bookId: String, coverData: ByteArray) {
        // Save cover to file and update book
        // Implementation would save to app cache/files directory
    }
}

data class ScanProgress(
    val current: Int = 0,
    val total: Int = 0,
    val currentFile: String = "",
    val status: ScanStatus = ScanStatus.IDLE
)

enum class ScanStatus {
    IDLE, SCANNING, PARSING, COMPLETE, ERROR
}

data class ScanResult(
    val added: Int,
    val updated: Int,
    val errors: Int,
    val totalScanned: Int
)

enum class ScanBookResult {
    ADDED, UPDATED, SKIPPED, ERROR
}

// Extension for Room DAO
private fun AppDatabase.BookDao.getByPathSync(path: String): Book? {
    // This would be a custom query in BookDao
    // SELECT * FROM books WHERE filePath = :path
    return null // Placeholder
}