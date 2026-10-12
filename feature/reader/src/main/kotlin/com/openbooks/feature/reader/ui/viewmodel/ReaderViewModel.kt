package com.openbooks.feature.reader.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openbooks.core.domain.parser.SearchResult
import com.openbooks.core.domain.parser.TOCEntry
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingPosition
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.domain.usecase.AddBookmarkUseCase
import com.openbooks.domain.usecase.AddNoteUseCase
import com.openbooks.domain.usecase.GetBookByIdUseCase
import com.openbooks.domain.usecase.GetBookmarksUseCase
import com.openbooks.domain.usecase.GetNotesUseCase
import com.openbooks.domain.usecase.GetReaderSettingsUseCase
import com.openbooks.domain.usecase.GetReadingPositionUseCase
import com.openbooks.domain.usecase.RecordReadingSessionUseCase
import com.openbooks.domain.usecase.RemoveBookmarkUseCase
import com.openbooks.domain.usecase.SaveReaderSettingsUseCase
import com.openbooks.domain.usecase.SaveReadingPositionUseCase
import com.openbooks.domain.usecase.SearchInBookUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val getBookByIdUseCase: GetBookByIdUseCase,
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase,
    private val saveReadingPositionUseCase: SaveReadingPositionUseCase,
    private val getReadingPositionUseCase: GetReadingPositionUseCase,
    private val getBookmarksUseCase: GetBookmarksUseCase,
    private val addBookmarkUseCase: AddBookmarkUseCase,
    private val removeBookmarkUseCase: RemoveBookmarkUseCase,
    private val getNotesUseCase: GetNotesUseCase,
    private val addNoteUseCase: AddNoteUseCase,
    private val searchInBookUseCase: SearchInBookUseCase,
    private val recordReadingSessionUseCase: RecordReadingSessionUseCase
) : ViewModel() {
    
    private var currentBook: Book? = null
    private var sessionStartTime: java.time.Instant? = null
    private var sessionStartLocation: String = ""
    private var sessionPagesRead = 0
    
    val settings = MutableStateFlow(ReaderSettings()).asStateFlow()
    val currentChapter = MutableStateFlow<com.openbooks.domain.parser.Chapter?>(null).asStateFlow()
    val chapterContent = MutableStateFlow<String>("").asStateFlow()
    val currentPage = MutableStateFlow(0).asStateFlow()
    val totalPages = MutableStateFlow(0).asStateFlow()
    val pageBitmap = MutableStateFlow<android.graphics.Bitmap?>(null).asStateFlow()
    val readingProgress = MutableStateFlow(0f).asStateFlow()
    val tableOfContents = MutableStateFlow<List<TOCEntry>>(emptyList()).asStateFlow()
    val bookmarks = MutableStateFlow<List<Bookmark>>(emptyList()).asStateFlow()
    val notes = MutableStateFlow<List<Note>>(emptyList()).asStateFlow()
    val readingSessions = MutableStateFlow<List<ReadingSession>>(emptyList()).asStateFlow()
    val searchResults = MutableStateFlow<List<SearchResult>>(emptyList()).asStateFlow()
    val isSearching = MutableStateFlow(false).asStateFlow()

    fun getBook(bookId: String): Book? {
        if (currentBook?.id == bookId) return currentBook
        currentBook = getBookByIdUseCase.execute(bookId)
        if (currentBook != null) {
            loadSettings(currentBook!!.id)
            loadBookContent(currentBook!!)
            startReadingSession()
        }
        return currentBook
    }

    private fun loadSettings(bookId: String) {
        viewModelScope.launch {
            val s = getReaderSettingsUseCase.execute(bookId)
            settings.value = s
        }
    }

    private fun loadBookContent(book: Book) {
        viewModelScope.launch {
            // Load TOC
            // This would use the appropriate parser based on format
            // tableOfContents.value = parser.getTableOfContents(book.filePath)
            
            // Load bookmarks and notes
            bookmarks.value = getBookmarksUseCase.execute(book.id)
            notes.value = getNotesUseCase.execute(book.id)
            
            // Load reading position
            val position = getReadingPositionUseCase.execute(book.id)
            if (position != null) {
                restorePosition(position)
            }
            
            // Load total pages
            // totalPages.value = parser.getPageCount(book.filePath)
        }
    }

    private fun startReadingSession() {
        sessionStartTime = java.time.Instant.now()
        sessionStartLocation = getCurrentLocation()
        sessionPagesRead = 0
    }

    private fun getCurrentLocation(): String {
        return when (currentBook?.format) {
            com.openbooks.domain.model.BookFormat.EPUB -> currentChapter.value?.href ?: ""
            com.openbooks.domain.model.BookFormat.PDF -> "page://$currentPage"
            com.openbooks.domain.model.BookFormat.CBZ, com.openbooks.domain.model.BookFormat.CBR -> "comics://page/$currentPage"
            else -> ""
        }
    }

    private fun restorePosition(position: ReadingPosition) {
        // Restore reading position based on format
    }

    fun saveSettings(newSettings: ReaderSettings) {
        viewModelScope.launch {
            saveReaderSettingsUseCase.execute(newSettings)
            settings.value = newSettings
        }
    }

    fun updateFontSize(size: Float) {
        val newSettings = settings.value.copy(fontSize = size)
        saveSettings(newSettings)
    }

    fun updateTheme(theme: String) {
        val newSettings = settings.value.copy(theme = theme)
        saveSettings(newSettings)
    }

    fun goToPage(page: Int) {
        currentPage.value = page.coerceIn(0, max(0, totalPages.value - 1))
        // Render page based on format
    }

    fun nextPage() {
        goToPage(currentPage.value + 1)
    }

    fun previousPage() {
        goToPage(currentPage.value - 1)
    }

    fun goToChapter(chapterIndex: Int) {
        // Navigate to chapter
    }

    fun goToLocation(location: String) {
        // Navigate to location (CFI, page, etc.)
    }

    fun seekToChapterProgress(progress: Float) {
        // Seek within chapter
    }

    fun addBookmark() {
        val book = currentBook ?: return
        val bookmark = Bookmark(
            bookId = book.id,
            location = getCurrentLocation(),
            pageNumber = currentPage.value + 1,
            chapterIndex = currentChapter.value?.order ?: 0
        )
        viewModelScope.launch {
            addBookmarkUseCase.execute(bookmark)
            bookmarks.value = bookmarks.value + bookmark
        }
    }

    fun deleteBookmark(bookmarkId: Long) {
        viewModelScope.launch {
            removeBookmarkUseCase.execute(bookmarkId)
            bookmarks.value = bookmarks.value.filter { it.id != bookmarkId }
        }
    }

    fun addNote(selectedText: String, noteText: String) {
        val book = currentBook ?: return
        val note = Note(
            bookId = book.id,
            location = getCurrentLocation(),
            selectedText = selectedText,
            noteText = noteText,
            pageNumber = currentPage.value + 1,
            chapterIndex = currentChapter.value?.order ?: 0
        )
        viewModelScope.launch {
            addNoteUseCase.execute(note)
            notes.value = notes.value + note
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            // Would need DeleteNoteUseCase
            notes.value = notes.value.filter { it.id != noteId }
        }
    }

    fun searchInBook(query: String) {
        if (query.isBlank()) {
            searchResults.value = emptyList()
            return
        }
        
        isSearching.value = true
        viewModelScope.launch {
            try {
                val results = searchInBookUseCase.execute(currentBook!!.id, query)
                searchResults.value = results
            } finally {
                isSearching.value = false
            }
        }
    }

    fun startTTS() {
        // Start text-to-speech with current chapter content
    }

    fun stopTTS() {
        // Stop text-to-speech
    }

    fun savePosition() {
        val book = currentBook ?: return
        val position = ReadingPosition(
            bookId = book.id,
            location = getCurrentLocation(),
            pageNumber = currentPage.value + 1,
            totalPages = totalPages.value,
            chapterIndex = currentChapter.value?.order ?: 0,
            chapterProgress = calculateChapterProgress(),
            deviceId = getDeviceId()
        )
        viewModelScope.launch {
            saveReadingPositionUseCase.execute(position)
        }
    }

    private fun calculateChapterProgress(): Float {
        // Calculate progress within current chapter
        return 0f
    }

    private fun getDeviceId(): String {
        // Return unique device ID
        return android.provider.Settings.Secure.getString(
            android.content.Context.getApplicationContext().contentResolver,
            android.provider.Settings.Secure.ANDROID_ID
        ) ?: "unknown"
    }

    override fun onCleared() {
        super.onCleared()
        endReadingSession()
    }

    private fun endReadingSession() {
        val book = currentBook ?: return
        val endTime = java.time.Instant.now()
        val duration = sessionStartTime?.until(endTime, java.time.temporal.ChronoUnit.SECONDS) ?: 0
        
        val session = ReadingSession(
            bookId = book.id,
            startTime = sessionStartTime!!,
            endTime = endTime,
            durationSeconds = duration,
            pagesRead = sessionPagesRead,
            startLocation = sessionStartLocation,
            endLocation = getCurrentLocation()
        )
        
        viewModelScope.launch {
            recordReadingSessionUseCase.execute(session)
        }
    }
}