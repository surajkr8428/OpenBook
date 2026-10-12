package com.openbooks.data.repository

import com.openbooks.data.db.AppDatabase
import com.openbooks.data.db.BookDao
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class BookRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : BookRepository {
    private val bookDao: BookDao = database.bookDao()

    override suspend fun getAllBooks(): List<Book> = bookDao.getAllSync()

    override suspend fun getBookById(id: String): Book? = bookDao.getByIdSync(id)

    override suspend fun searchBooks(query: String): List<Book> = bookDao.search(query)

    override suspend fun getBooksByFormat(format: BookFormat): List<Book> = 
        bookDao.getByFormat(format.name)

    override suspend fun getBooksByCollection(collectionId: String): List<Book> = 
        bookDao.getByCollection(collectionId)

    override suspend fun getBooksByShelf(shelfId: String): List<Book> = 
        bookDao.getByShelf(shelfId)

    override suspend fun getRecentBooks(limit: Int): List<Book> = bookDao.getRecent(limit)

    override suspend fun getFavoriteBooks(): List<Book> = bookDao.getFavorites()

    override suspend fun getFinishedBooks(): List<Book> = bookDao.getFinished()

    override suspend fun insertBook(book: Book) = bookDao.insert(book)

    override suspend fun insertBooks(books: List<Book>) = bookDao.insertAll(books)

    override suspend fun updateBook(book: Book) = bookDao.update(book)

    override suspend fun deleteBook(id: String) = bookDao.deleteById(id)

    override suspend fun deleteAllBooks() = bookDao.deleteAll()

    override fun observeAllBooks(): Flow<List<Book>> = bookDao.getAll()

    override fun observeBookById(id: String): Flow<Book?> = bookDao.getById(id)

    override fun observeBooksByCollection(collectionId: String): Flow<List<Book>> = 
        bookDao.getAll().map { books ->
            books.filter { it.id in getCollectionBookIdsSync(collectionId) }
        }

    override fun observeBooksByShelf(shelfId: String): Flow<List<Book>> = 
        bookDao.getAll().map { books ->
            books.filter { it.id in getShelfBookIdsSync(shelfId) }
        }

    override fun observeRecentBooks(limit: Int): Flow<List<Book>> = bookDao.observeRecent(limit)

    override fun observeFavoriteBooks(): Flow<List<Book>> = bookDao.observeFavorites()

    private suspend fun getCollectionBookIdsSync(collectionId: String): Set<String> = 
        database.collectionBookCrossRefDao().getBookIdsForCollection(collectionId).toSet()

    private suspend fun getShelfBookIdsSync(shelfId: String): Set<String> = 
        database.shelfCollectionCrossRefDao().getCollectionIdsForShelf(shelfId)
            .flatMap { database.collectionBookCrossRefDao().getBookIdsForCollection(it) }
            .toSet()
}