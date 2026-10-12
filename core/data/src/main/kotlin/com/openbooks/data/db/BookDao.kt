package com.openbooks.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import androidx.room.Transaction
import com.openbooks.domain.model.Book
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(books: List<Book>)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("DELETE FROM books WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM books")
    suspend fun deleteAll()

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    fun getAll(): Flow<List<Book>>

    @Query("SELECT * FROM books ORDER BY dateAdded DESC")
    suspend fun getAllSync(): List<Book>

    @Query("SELECT * FROM books WHERE id = :id")
    fun getById(id: String): Flow<Book?>

    @Query("SELECT * FROM books WHERE id = :id")
    suspend fun getByIdSync(id: String): Book?

    @Query("SELECT * FROM books WHERE title LIKE '%' || :query || '%' OR authors LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY dateAdded DESC")
    suspend fun search(query: String): List<Book>

    @Query("SELECT * FROM books WHERE format = :format ORDER BY dateAdded DESC")
    suspend fun getByFormat(format: String): List<Book>

    @Query("SELECT * FROM books WHERE id IN (SELECT bookId FROM collection_books WHERE collectionId = :collectionId) ORDER BY title")
    suspend fun getByCollection(collectionId: String): List<Book>

    @Query("SELECT * FROM books WHERE id IN (SELECT bookId FROM shelf_books WHERE shelfId = :shelfId) ORDER BY title")
    suspend fun getByShelf(shelfId: String): List<Book>

    @Query("SELECT * FROM books WHERE lastOpened IS NOT NULL ORDER BY lastOpened DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<Book>

    @Query("SELECT * FROM books WHERE lastOpened IS NOT NULL ORDER BY lastOpened DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    suspend fun getFavorites(): List<Book>

    @Query("SELECT * FROM books WHERE isFavorite = 1 ORDER BY dateAdded DESC")
    fun observeFavorites(): Flow<List<Book>>

    @Query("SELECT * FROM books WHERE isFinished = 1 ORDER BY dateAdded DESC")
    suspend fun getFinished(): List<Book>

    @Query("UPDATE books SET lastOpened = :timestamp WHERE id = :id")
    suspend fun updateLastOpened(id: String, timestamp: Long)

    @Query("UPDATE books SET readingProgress = :progress WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float)

    @Query("UPDATE books SET isFavorite = :favorite WHERE id = :id")
    suspend fun setFavorite(id: String, favorite: Boolean)

    @Query("UPDATE books SET isFinished = :finished WHERE id = :id")
    suspend fun setFinished(id: String, finished: Boolean)
}