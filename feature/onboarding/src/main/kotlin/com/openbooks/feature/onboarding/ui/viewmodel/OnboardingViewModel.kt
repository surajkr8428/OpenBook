package com.openbooks.feature.onboarding.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openbooks.core.data.db.AppDatabase
import com.openbooks.core.scanner.BookScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val database: AppDatabase,
    private val bookScanner: BookScanner
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(OnboardingUiState(
        currentPage = 0,
        pages = listOf(
            OnboardingPageData(
                title = "Welcome to OpenBooks",
                description = "Your personal e-book library. Read anywhere, sync everywhere.",
                icon = "book"
            ),
            OnboardingPageData(
                title = "Add Your Books",
                description = "Scan your device for EPUB, PDF, comics, and more. We'll organize them automatically.",
                icon = "folder"
            ),
            OnboardingPageData(
                title = "Read Your Way",
                description = "Customize fonts, themes, margins, and more. Dark mode, sepia, or create your own theme.",
                icon = "format"
            ),
            OnboardingPageData(
                title = "Listen to Books",
                description = "Built-in text-to-speech with natural voices. Adjust speed, pitch, and auto-scroll.",
                icon = "volume"
            ),
            OnboardingPageData(
                title = "Sync Across Devices",
                description = "Sign in to sync reading progress, bookmarks, and notes across all your devices.",
                icon = "cloud"
            ),
            OnboardingPageData(
                title = "Ready to Read",
                description = "Start by scanning for books, or add them manually. Happy reading!",
                icon = "check"
            )
        ),
        isScanning = false,
        scanResult = null
    ))
    
    val uiState = _uiState.asStateFlow()

    fun nextPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage + 1) coerceAtMost it.pages.lastIndex) }
    }

    fun previousPage() {
        _uiState.update { it.copy(currentPage = (it.currentPage - 1) coerceAtLeast 0) }
    }

    fun startScan() {
        _uiState.update { it.copy(isScanning = true) }
        viewModelScope.launch {
            try {
                val result = bookScanner.scanLibrary()
                _uiState.update { it.copy(isScanning = false, scanResult = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isScanning = false) }
            }
        }
    }

    fun completeOnboarding() {
        // Mark onboarding as complete in preferences
        // Navigate to main app
    }
}

data class OnboardingUiState(
    val currentPage: Int = 0,
    val pages: List<OnboardingPageData>,
    val isScanning: Boolean = false,
    val scanResult: com.openbooks.core.scanner.ScanResult? = null
)