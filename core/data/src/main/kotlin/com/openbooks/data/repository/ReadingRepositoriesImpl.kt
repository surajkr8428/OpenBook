package com.openbooks.data.repository

import com.openbooks.data.db.AppDatabase
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingPosition
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.domain.repository.BookmarkRepository
import com.openbooks.domain.repository.NoteRepository
import com.openbooks.domain.repository.ReadingPositionRepository
import com.openbooks.domain.repository.ReadingSessionRepository
import com.openbooks.domain.repository.ReaderSettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ReadingPositionRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ReadingPositionRepository {
    private val dao = database.readingPositionDao()

    override suspend fun getPosition(bookId: String): ReadingPosition? = dao.getByBookIdSync(bookId)

    override suspend fun savePosition(position: ReadingPosition) = dao.insert(position)

    override suspend fun deletePosition(bookId: String) = dao.deleteByBookId(bookId)

    override fun observePosition(bookId: String): Flow<ReadingPosition?> = dao.getByBookId(bookId)
}

class BookmarkRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : BookmarkRepository {
    private val dao = database.bookmarkDao()

    override suspend fun getBookmarks(bookId: String): List<Bookmark> = dao.getByBookIdSync(bookId)

    override suspend fun getAllBookmarks(): List<Bookmark> = dao.getAll()

    override suspend fun insertBookmark(bookmark: Bookmark) = dao.insert(bookmark)

    override suspend fun updateBookmark(bookmark: Bookmark) = dao.update(bookmark)

    override suspend fun deleteBookmark(id: Long) = dao.deleteById(id)

    override suspend fun deleteBookmarksForBook(bookId: String) = dao.deleteByBookId(bookId)

    override fun observeBookmarks(bookId: String): Flow<List<Bookmark>> = dao.getByBookId(bookId)
}

class NoteRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : NoteRepository {
    private val dao = database.noteDao()

    override suspend fun getNotes(bookId: String): List<Note> = dao.getByBookIdSync(bookId)

    override suspend fun getAllNotes(): List<Note> = dao.getAll()

    override suspend fun insertNote(note: Note) = dao.insert(note)

    override suspend fun updateNote(note: Note) = dao.update(note)

    override suspend fun deleteNote(id: Long) = dao.deleteById(id)

    override suspend fun deleteNotesForBook(bookId: String) = dao.deleteByBookId(bookId)

    override fun observeNotes(bookId: String): Flow<List<Note>> = dao.getByBookId(bookId)
}

class ReadingSessionRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ReadingSessionRepository {
    private val dao = database.readingSessionDao()

    override suspend fun getSessions(bookId: String): List<ReadingSession> = dao.getByBookIdSync(bookId)

    override suspend fun getAllSessions(): List<ReadingSession> = dao.getAll()

    override suspend fun insertSession(session: ReadingSession) = dao.insert(session)

    override suspend fun updateSession(session: ReadingSession) = dao.update(session)

    override fun observeSessions(bookId: String): Flow<List<ReadingSession>> = dao.getByBookId(bookId)
}

class ReaderSettingsRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ReaderSettingsRepository {
    private val dao = database.readerSettingsDao()

    override suspend fun getSettings(bookId: String): ReaderSettings = 
        dao.getByBookIdSync(bookId) ?: dao.getGlobalSettingsSync() ?: ReaderSettings(bookId = bookId)

    override suspend fun saveSettings(settings: ReaderSettings) = dao.insert(settings)

    override suspend fun resetSettings(bookId: String) = dao.deleteByBookId(bookId)

    override fun observeSettings(bookId: String): Flow<ReaderSettings> = 
        dao.getByBookId(bookId)
            .map { it ?: ReaderSettings(bookId = bookId) }
}