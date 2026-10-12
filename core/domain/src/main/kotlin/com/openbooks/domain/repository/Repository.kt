package com.openbooks.domain.repository

import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Collection
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingPosition
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.domain.model.Shelf
import kotlinx.coroutines.flow.Flow

interface BookRepository {
    suspend fun getAllBooks(): List<Book>
    suspend fun getBookById(id: String): Book?
    suspend fun searchBooks(query: String): List<Book>
    suspend fun getBooksByFormat(format: Book.Format): List<Book>
    suspend fun getBooksByCollection(collectionId: String): List<Book>
    suspend fun getBooksByShelf(shelfId: String): List<Book>
    suspend fun getRecentBooks(limit: Int): List<Book>
    suspend fun getFavoriteBooks(): List<Book>
    suspend fun getFinishedBooks(): List<Book>
    suspend fun insertBook(book: Book)
    suspend fun insertBooks(books: List<Book>)
    suspend fun updateBook(book: Book)
    suspend fun deleteBook(id: String)
    suspend fun deleteAllBooks()
    fun observeAllBooks(): Flow<List<Book>>
    fun observeBookById(id: String): Flow<Book?>
    fun observeBooksByCollection(collectionId: String): Flow<List<Book>>
    fun observeBooksByShelf(shelfId: String): Flow<List<Book>>
    fun observeRecentBooks(limit: Int): Flow<List<Book>>
    fun observeFavoriteBooks(): Flow<List<Book>>
}

interface CollectionRepository {
    suspend fun getAllCollections(): List<Collection>
    suspend fun getCollectionById(id: String): Collection?
    suspend fun insertCollection(collection: Collection)
    suspend fun updateCollection(collection: Collection)
    suspend fun deleteCollection(id: String)
    suspend fun addBookToCollection(collectionId: String, bookId: String)
    suspend fun removeBookFromCollection(collectionId: String, bookId: String)
    fun observeAllCollections(): Flow<List<Collection>>
    fun observeCollectionById(id: String): Flow<Collection?>
}

interface ShelfRepository {
    suspend fun getAllShelves(): List<Shelf>
    suspend fun getShelfById(id: String): Shelf?
    suspend fun insertShelf(shelf: Shelf)
    suspend fun updateShelf(shelf: Shelf)
    suspend fun deleteShelf(id: String)
    fun observeAllShelves(): Flow<List<Shelf>>
    fun observeShelfById(id: String): Flow<Shelf?>
}

interface ReadingPositionRepository {
    suspend fun getPosition(bookId: String): ReadingPosition?
    suspend fun savePosition(position: ReadingPosition)
    suspend fun deletePosition(bookId: String)
    fun observePosition(bookId: String): Flow<ReadingPosition?>
}

interface BookmarkRepository {
    suspend fun getBookmarks(bookId: String): List<Bookmark>
    suspend fun getAllBookmarks(): List<Bookmark>
    suspend fun insertBookmark(bookmark: Bookmark)
    suspend fun updateBookmark(bookmark: Bookmark)
    suspend fun deleteBookmark(id: Long)
    suspend fun deleteBookmarksForBook(bookId: String)
    fun observeBookmarks(bookId: String): Flow<List<Bookmark>>
}

interface NoteRepository {
    suspend fun getNotes(bookId: String): List<Note>
    suspend fun getAllNotes(): List<Note>
    suspend fun insertNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNote(id: Long)
    suspend fun deleteNotesForBook(bookId: String)
    fun observeNotes(bookId: String): Flow<List<Note>>
}

interface ReadingSessionRepository {
    suspend fun getSessions(bookId: String): List<ReadingSession>
    suspend fun getAllSessions(): List<ReadingSession>
    suspend fun insertSession(session: ReadingSession)
    suspend fun updateSession(session: ReadingSession)
    fun observeSessions(bookId: String): Flow<List<ReadingSession>>
}

interface ReaderSettingsRepository {
    suspend fun getSettings(bookId: String): ReaderSettings
    suspend fun saveSettings(settings: ReaderSettings)
    suspend fun resetSettings(bookId: String)
    fun observeSettings(bookId: String): Flow<ReaderSettings>
}