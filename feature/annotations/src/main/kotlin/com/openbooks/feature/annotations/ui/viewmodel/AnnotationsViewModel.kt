package com.openbooks.feature.annotations.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openbooks.core.data.db.AppDatabase
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnnotationsViewModel @Inject constructor(
    private val database: AppDatabase
) : ViewModel() {
    
    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    
    val bookmarks = _bookmarks.asStateFlow()
    val notes = _notes.asStateFlow()
    
    fun loadAnnotations(bookId: String) {
        viewModelScope.launch {
            _bookmarks.value = database.bookmarkDao().getByBookIdSync(bookId)
            _notes.value = database.noteDao().getByBookIdSync(bookId)
        }
    }
    
    fun deleteAnnotation(item: com.openbooks.feature.annotations.ui.screen.AnnotationItem) {
        viewModelScope.launch {
            when (item) {
                is com.openbooks.feature.annotations.ui.screen.AnnotationItem.BookmarkItem -> {
                    database.bookmarkDao().deleteById(item.bookmark.id)
                    _bookmarks.value = _bookmarks.value.filter { it.id != item.bookmark.id }
                }
                is com.openbooks.feature.annotations.ui.screen.AnnotationItem.NoteItem -> {
                    database.noteDao().deleteById(item.note.id)
                    _notes.value = _notes.value.filter { it.id != item.note.id }
                }
            }
        }
    }
    
    fun addBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            database.bookmarkDao().insert(bookmark)
            _bookmarks.value = _bookmarks.value + bookmark
        }
    }
    
    fun addNote(note: Note) {
        viewModelScope.launch {
            database.noteDao().insert(note)
            _notes.value = _notes.value + note
        }
    }
}