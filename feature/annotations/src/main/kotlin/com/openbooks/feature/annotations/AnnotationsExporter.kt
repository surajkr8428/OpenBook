package com.openbooks.feature.annotations

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.openbooks.core.data.db.AppDatabase
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AnnotationsExporter @Inject constructor(
    private val context: Context,
    private val database: AppDatabase
) {
    
    suspend fun exportAnnotations(bookId: String): ExportResult {
        return withContext(Dispatchers.IO) {
            val book = database.bookDao().getByIdSync(bookId)
            val bookmarks = database.bookmarkDao().getByBookIdSync(bookId)
            val notes = database.noteDao().getByBookIdSync(bookId)
            
            if (book == null) {
                return@withContext ExportResult.Error("Book not found")
            }
            
            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            
            val fileName = "annotations_${book.title.replace(" ", "_")}_${System.currentTimeMillis()}.md"
            val exportFile = File(exportDir, fileName)
            
            try {
                FileWriter(exportFile).use { writer ->
                    writer.write(generateMarkdown(book, bookmarks, notes))
                }
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )
                
                ExportResult.Success(uri, exportFile.absolutePath)
            } catch (e: Exception) {
                ExportResult.Error("Export failed: ${e.message}")
            }
        }
    }
    
    suspend fun exportAllAnnotations(): ExportResult {
        return withContext(Dispatchers.IO) {
            val books = database.bookDao().getAllSync()
            val allBookmarks = database.bookmarkDao().getAll()
            val allNotes = database.noteDao().getAll()
            
            val exportDir = File(context.cacheDir, "exports")
            exportDir.mkdirs()
            
            val fileName = "all_annotations_${System.currentTimeMillis()}.md"
            val exportFile = File(exportDir, fileName)
            
            try {
                FileWriter(exportFile).use { writer ->
                    writer.write("# All Annotations Export\n\n")
                    writer.write("Generated: ${DateTimeFormatter.ISO_INSTANT.format(java.time.Instant.now())}\n\n")
                    
                    for (book in books) {
                        val bookmarks = allBookmarks.filter { it.bookId == book.id }
                        val notes = allNotes.filter { it.bookId == book.id }
                        
                        if (bookmarks.isNotEmpty() || notes.isNotEmpty()) {
                            writer.write(generateMarkdown(book, bookmarks, notes))
                            writer.write("\n---\n\n")
                        }
                    }
                }
                
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    exportFile
                )
                
                ExportResult.Success(uri, exportFile.absolutePath)
            } catch (e: Exception) {
                ExportResult.Error("Export failed: ${e.message}")
            }
        }
    }
    
    suspend fun shareAnnotations(bookId: String) {
        val result = exportAnnotations(bookId)
        if (result is ExportResult.Success) {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, result.uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Annotations"))
        }
    }
    
    private fun generateMarkdown(
        book: Book,
        bookmarks: List<Bookmark>,
        notes: List<Note>
    ): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        val sb = StringBuilder()
        
        sb.append("# Annotations for: ${book.title}\n\n")
        sb.append("**Author(s):** ${book.authors.joinToString(", ")}\n")
        sb.append("**Format:** ${book.format}\n")
        sb.append("**Progress:** ${(book.readingProgress * 100).toInt()}%\n")
        sb.append("**Export Date:** ${formatter.format(java.time.Instant.now())}\n\n")
        
        if (bookmarks.isNotEmpty()) {
            sb.append("## Bookmarks\n\n")
            for (bookmark in bookmarks.sortedBy { it.timestamp }) {
                sb.append("- **Page ${bookmark.pageNumber}** (Chapter ${bookmark.chapterIndex + 1})\n")
                sb.append("  - Location: ${bookmark.location}\n")
                sb.append("  - Date: ${formatter.format(bookmark.timestamp)}\n")
                if (bookmark.note.isNotNullOrBlank()) {
                    sb.append("  - Note: ${bookmark.note}\n")
                }
                sb.append("\n")
            }
        }
        
        if (notes.isNotEmpty()) {
            sb.append("## Notes & Highlights\n\n")
            for (note in notes.sortedBy { it.timestamp }) {
                sb.append("### Highlight (Page ${note.pageNumber}, Chapter ${note.chapterIndex + 1})\n\n")
                sb.append("> ${note.selectedText}\n\n")
                sb.append("**Your Note:** ${note.noteText}\n\n")
                sb.append("*Added: ${formatter.format(note.timestamp)}*\n\n")
            }
        }
        
        return sb.toString()
    }
}

sealed class ExportResult {
    data class Success(val uri: Uri, val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}