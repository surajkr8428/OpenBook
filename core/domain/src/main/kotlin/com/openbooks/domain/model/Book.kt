package com.openbooks.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val coverPath: String?,
    val filePath: String,
    val format: BookFormat,
    val fileSize: Long,
    val pageCount: Int,
    val language: String?,
    val publisher: String?,
    val publishedDate: String?,
    val isbn: String?,
    val tags: List<String>,
    val series: String?,
    val seriesIndex: Double?,
    val dateAdded: Instant = Instant.now(),
    val lastOpened: Instant?,
    val readingProgress: Float = 0f,
    val isFavorite: Boolean = false,
    val isFinished: Boolean = false,
    val customMetadata: Map<String, String> = emptyMap()
)

@Serializable
enum class BookFormat {
    EPUB, PDF, MOBI, FB2, DJVU, TXT, CBZ, CBR, LCP, AUDIOBOOK
}

@Serializable
@Entity(tableName = "collections")
data class Collection(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val bookIds: List<String> = emptyList(),
    val dateCreated: Instant = Instant.now(),
    val dateModified: Instant = Instant.now(),
    val isSystem: Boolean = false
)

@Serializable
@Entity(tableName = "shelves")
data class Shelf(
    @PrimaryKey val id: String,
    val name: String,
    val collectionIds: List<String> = emptyList(),
    val sortOrder: Int = 0,
    val isDefault: Boolean = false
)

@Serializable
@Entity(tableName = "reading_positions")
data class ReadingPosition(
    @PrimaryKey val bookId: String,
    val location: String,
    val cfi: String?,
    val pageNumber: Int,
    val totalPages: Int,
    val chapterIndex: Int,
    val chapterProgress: Float,
    val timestamp: Instant = Instant.now(),
    val deviceId: String
)

@Serializable
@Entity(tableName = "bookmarks")
data class Bookmark(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val location: String,
    val cfi: String?,
    val pageNumber: Int,
    val chapterIndex: Int,
    val text: String?,
    val note: String?,
    val color: Int = 0xFFFFD700,
    val timestamp: Instant = Instant.now()
)

@Serializable
@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookId: String,
    val location: String,
    val cfi: String?,
    val selectedText: String,
    val noteText: String,
    val color: Int = 0xFF4CAF50,
    val timestamp: Instant = Instant.now()
)

@Serializable
@Entity(tableName = "reading_sessions")
data class ReadingSession(
    @PrimaryKey val bookId: String,
    val startTime: Instant,
    val endTime: Instant?,
    val durationSeconds: Long,
    val pagesRead: Int,
    val startLocation: String,
    val endLocation: String?
)

@Serializable
data class LibraryStats(
    val totalBooks: Int,
    val totalCollections: Int,
    val totalReadingTime: Long,
    val booksReadThisYear: Int,
    val currentStreak: Int,
    val favoriteGenres: Map<String, Int>
)