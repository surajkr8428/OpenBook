package com.openbooks.feature.library.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openbooks.domain.model.Book
import com.openbooks.domain.usecase.GetAllBooksUseCase
import com.openbooks.domain.usecase.SearchBooksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val getAllBooksUseCase: GetAllBooksUseCase,
    private val searchBooksUseCase: SearchBooksUseCase
) : ViewModel() {
    
    private val _query = MutableStateFlow("")
    private val _books = MutableStateFlow<List<Book>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: LibraryUiState = combine(_books, _isLoading, _query, _error) { books, isLoading, query, error ->
        LibraryUiState(books = books, isLoading = isLoading, query = query, error = error)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibraryUiState())

    init {
        loadBooks()
    }

    fun loadBooks() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val books = getAllBooksUseCase.execute()
                _books.value = books
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load books"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchBooks(query: String) {
        _query.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                loadBooks()
            } else {
                _isLoading.value = true
                try {
                    val books = searchBooksUseCase.execute(query)
                    _books.value = books
                } catch (e: Exception) {
                    _error.value = e.message ?: "Search failed"
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }
}

data class LibraryUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = "",
    val error: String? = null
)