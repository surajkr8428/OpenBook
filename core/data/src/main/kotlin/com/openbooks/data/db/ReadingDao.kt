package com.openbooks.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.openbooks.domain.model.ReadingPosition
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingPositionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(position: ReadingPosition)

    @Update
    suspend fun update(position: ReadingPosition)

    @Delete
    suspend fun delete(position: ReadingPosition)

    @Query("DELETE FROM reading_positions WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: String)

    @Query("SELECT * FROM reading_positions WHERE bookId = :bookId")
    fun getByBookId(bookId: String): Flow<ReadingPosition?>

    @Query("SELECT * FROM reading_positions WHERE bookId = :bookId")
    suspend fun getByBookIdSync(bookId: String): ReadingPosition?

    @Query("SELECT * FROM reading_positions WHERE deviceId = :deviceId")
    suspend fun getByDeviceId(deviceId: String): List<ReadingPosition>
}

@Dao
interface BookmarkDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: Bookmark)

    @Update
    suspend fun update(bookmark: Bookmark)

    @Delete
    suspend fun delete(bookmark: Bookmark)

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bookmarks WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: String)

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getByBookId(bookId: String): Flow<List<Bookmark>>

    @Query("SELECT * FROM bookmarks WHERE bookId = :bookId ORDER BY timestamp DESC")
    suspend fun getByBookIdSync(bookId: String): List<Bookmark>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    suspend fun getAll(): List<Bookmark>
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM notes WHERE bookId = :bookId")
    suspend fun deleteByBookId(bookId: String)

    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY timestamp DESC")
    fun getByBookId(bookId: String): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE bookId = :bookId ORDER BY timestamp DESC")
    suspend fun getByBookIdSync(bookId: String): List<Note>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAll(): List<Note>
}