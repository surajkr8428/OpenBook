package com.openbooks.domain.usecase

import com.openbooks.core.domain.parser.BookParser
import com.openbooks.core.domain.parser.SearchResult
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Collection
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingPosition
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.domain.model.Shelf
import com.openbooks.domain.parser.BookParser
import com.openbooks.domain.parser.ParsedBook
import com.openbooks.domain.repository.BookRepository
import com.openbooks.domain.repository.BookmarkRepository
import com.openbooks.domain.repository.CollectionRepository
import com.openbooks.domain.repository.NoteRepository
import com.openbooks.domain.repository.ReadingPositionRepository
import com.openbooks.domain.repository.ReadingSessionRepository
import com.openbooks.domain.repository.ReaderSettingsRepository
import com.openbooks.domain.repository.ShelfRepository
import kotlinx.coroutines.flow.Flow

class GetAllBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> = bookRepository.observeAllBooks()
    suspend fun execute(): List<Book> = bookRepository.getAllBooks()
}

class SearchBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(query: String): Flow<List<Book>> = 
        bookRepository.observeAllBooks().map { books ->
            if (query.isBlank()) books else books.filter { book ->
                book.title.contains(query, ignoreCase = true) ||
                book.authors.any { it.contains(query, ignoreCase = true) } ||
                book.tags.any { it.contains(query, ignoreCase = true) }
            }
        }
    suspend fun execute(query: String): List<Book> = bookRepository.searchBooks(query)
}

class GetBookByIdUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(bookId: String): Flow<Book?> = bookRepository.observeBookById(bookId)
    suspend fun execute(bookId: String): Book? = bookRepository.getBookById(bookId)
}

class AddBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val parser: BookParser
) {
    suspend fun execute(filePath: String): Book {
        val parsedBook = parser.parseBook(filePath)
        bookRepository.insertBook(parsedBook.book)
        return parsedBook.book
    }
    
    suspend fun executeMultiple(filePaths: List<String>): List<Book> {
        val books = filePaths.map { parser.parseBook(it).book }
        bookRepository.insertBooks(books)
        return books
    }
}

class UpdateBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend fun execute(book: Book) = bookRepository.updateBook(book)
}

class DeleteBookUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    suspend fun execute(bookId: String) = bookRepository.deleteBook(bookId)
}

class GetRecentBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(limit: Int = 10): Flow<List<Book>> = bookRepository.observeRecentBooks(limit)
    suspend fun execute(limit: Int = 10): List<Book> = bookRepository.getRecentBooks(limit)
}

class GetFavoriteBooksUseCase @Inject constructor(
    private val bookRepository: BookRepository
) {
    operator fun invoke(): Flow<List<Book>> = bookRepository.observeFavoriteBooks()
    suspend fun execute(): List<Book> = bookRepository.getFavoriteBooks()
}

class GetReadingPositionUseCase @Inject constructor(
    private val positionRepository: ReadingPositionRepository
) {
    operator fun invoke(bookId: String): Flow<ReadingPosition?> = positionRepository.observePosition(bookId)
    suspend fun execute(bookId: String): ReadingPosition? = positionRepository.getPosition(bookId)
}

class SaveReadingPositionUseCase @Inject constructor(
    private val positionRepository: ReadingPositionRepository
) {
    suspend fun execute(position: ReadingPosition) = positionRepository.savePosition(position)
}

class GetBookmarksUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    operator fun invoke(bookId: String): Flow<List<Bookmark>> = bookmarkRepository.observeBookmarks(bookId)
    suspend fun execute(bookId: String): List<Bookmark> = bookmarkRepository.getBookmarks(bookId)
}

class AddBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend fun execute(bookmark: Bookmark) = bookmarkRepository.insertBookmark(bookmark)
}

class RemoveBookmarkUseCase @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) {
    suspend fun execute(bookmarkId: Long) = bookmarkRepository.deleteBookmark(bookmarkId)
}

class GetNotesUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(bookId: String): Flow<List<Note>> = noteRepository.observeNotes(bookId)
    suspend fun execute(bookId: String): List<Note> = noteRepository.getNotes(bookId)
}

class AddNoteUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    suspend fun execute(note: Note) = noteRepository.insertNote(note)
}

class GetCollectionsUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    operator fun invoke(): Flow<List<Collection>> = collectionRepository.observeAllCollections()
    suspend fun execute(): List<Collection> = collectionRepository.getAllCollections()
}

class CreateCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(collection: Collection) = collectionRepository.insertCollection(collection)
}

class AddBookToCollectionUseCase @Inject constructor(
    private val collectionRepository: CollectionRepository
) {
    suspend fun execute(collectionId: String, bookId: String) = 
        collectionRepository.addBookToCollection(collectionId, bookId)
}

class GetShelvesUseCase @Inject constructor(
    private val shelfRepository: ShelfRepository
) {
    operator fun invoke(): Flow<List<Shelf>> = shelfRepository.observeAllShelves()
    suspend fun execute(): List<Shelf> = shelfRepository.getAllShelves()
}

class GetReaderSettingsUseCase @Inject constructor(
    private val settingsRepository: ReaderSettingsRepository
) {
    operator fun invoke(bookId: String): Flow<ReaderSettings> = settingsRepository.observeSettings(bookId)
    suspend fun execute(bookId: String): ReaderSettings = settingsRepository.getSettings(bookId)
}

class SaveReaderSettingsUseCase @Inject constructor(
    private val settingsRepository: ReaderSettingsRepository
) {
    suspend fun execute(settings: ReaderSettings) = settingsRepository.saveSettings(settings)
}

class RecordReadingSessionUseCase @Inject constructor(
    private val sessionRepository: ReadingSessionRepository
) {
    suspend fun execute(session: ReadingSession) = sessionRepository.insertSession(session)
}

class SearchInBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
    private val epubParser: BookParser,
    private val pdfParser: BookParser,
    private val comicsParser: BookParser
) {
    suspend fun execute(bookId: String, query: String): List<SearchResult> {
        val book = bookRepository.getBookById(bookId) ?: return emptyList()
        
        val parser = when (book.format) {
            com.openbooks.domain.model.BookFormat.EPUB -> epubParser
            com.openbooks.domain.model.BookFormat.PDF -> pdfParser
            com.openbooks.domain.model.BookFormat.CBZ, com.openbooks.domain.model.BookFormat.CBR -> comicsParser
            else -> epubParser
        }
        
        return try {
            (parser as? com.openbooks.domain.parser.BookRenderer)?.searchInBook(book.filePath, query) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}