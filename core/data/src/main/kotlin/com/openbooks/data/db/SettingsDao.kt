package com.openbooks.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.ReaderSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ReadingSession)

    @Update
    suspend fun update(session: ReadingSession)

    @Delete
    suspend fun delete(session: ReadingSession)

    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY startTime DESC")
    fun getByBookId(bookId: String): Flow<List<ReadingSession>>

    @Query("SELECT * FROM reading_sessions WHERE bookId = :bookId ORDER BY startTime DESC")
    suspend fun getByBookIdSync(bookId: String): List<ReadingSession>

    @Query("SELECT * FROM reading_sessions ORDER BY startTime DESC")
    suspend fun getAll(): List<ReadingSession>

    @Query("SELECT * FROM reading_sessions WHERE startTime >= :startTime AND startTime <= :endTime")
    suspend fun getByDateRange(startTime: Long, endTime: Long): List<ReadingSession>
}

@Dao
interface ReaderSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: ReaderSettings)

    @Update
    suspend fun update(settings: ReaderSettings)

    @Delete
    suspend fun delete(settings: ReaderSettings)

    @Query("SELECT * FROM reader_settings WHERE bookId = :bookId")
    fun getByBookId(bookId: String): Flow<ReaderSettings?>

    @Query("SELECT * FROM reader_settings WHERE bookId = :bookId")
    suspend fun getByBookIdSync(bookId: String): ReaderSettings?

    @Query("SELECT * FROM reader_settings WHERE bookId = 'global'")
    fun getGlobalSettings(): Flow<ReaderSettings?>

    @Query("SELECT * FROM reader_settings WHERE bookId = 'global'")
    suspend fun getGlobalSettingsSync(): ReaderSettings?
}