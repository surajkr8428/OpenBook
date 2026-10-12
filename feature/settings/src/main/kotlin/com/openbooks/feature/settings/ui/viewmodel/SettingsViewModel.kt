package com.openbooks.feature.settings.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.domain.model.TTSConfig
import com.openbooks.domain.model.SyncConfig
import com.openbooks.domain.usecase.GetReaderSettingsUseCase
import com.openbooks.domain.usecase.SaveReaderSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getReaderSettingsUseCase: GetReaderSettingsUseCase,
    private val saveReaderSettingsUseCase: SaveReaderSettingsUseCase
) : ViewModel() {
    
    private val _fontSize = MutableStateFlow(1.0f)
    private val _fontFamily = MutableStateFlow("System Default")
    private val _lineHeight = MutableStateFlow(1.5f)
    private val _margin = MutableStateFlow(1.0f)
    private val _theme = MutableStateFlow(ReaderSettings.ReadingTheme.SYSTEM)
    private val _keepScreenOn = MutableStateFlow(true)
    private val _pageTurnAnimation = MutableStateFlow(ReaderSettings.PageTurnAnimation.SLIDE)
    private val _ttsLanguage = MutableStateFlow("en-US")
    private val _ttsSpeechRate = MutableStateFlow(1.0f)
    private val _ttsPitch = MutableStateFlow(1.0f)
    private val _ttsAutoScroll = MutableStateFlow(true)
    private val _syncEnabled = MutableStateFlow(true)
    private val _syncOnWifiOnly = MutableStateFlow(true)
    private val _syncIntervalMinutes = MutableStateFlow(30)
    private val _syncPositions = MutableStateFlow(true)
    private val _syncBookmarks = MutableStateFlow(true)

    val uiState = combine(
        _fontSize, _fontFamily, _lineHeight, _margin, _theme,
        _keepScreenOn, _pageTurnAnimation,
        _ttsLanguage, _ttsSpeechRate, _ttsPitch, _ttsAutoScroll,
        _syncEnabled, _syncOnWifiOnly, _syncIntervalMinutes, _syncPositions, _syncBookmarks
    ) { fontSize, fontFamily, lineHeight, margin, theme, keepScreenOn, pageTurnAnimation,
        ttsLanguage, ttsSpeechRate, ttsPitch, ttsAutoScroll,
        syncEnabled, syncOnWifiOnly, syncIntervalMinutes, syncPositions, syncBookmarks ->
        SettingsUiState(
            fontSize = fontSize, fontFamily = fontFamily, lineHeight = lineHeight, margin = margin,
            theme = theme, keepScreenOn = keepScreenOn, pageTurnAnimation = pageTurnAnimation,
            ttsLanguage = ttsLanguage, ttsSpeechRate = ttsSpeechRate, ttsPitch = ttsPitch,
            ttsAutoScroll = ttsAutoScroll, syncEnabled = syncEnabled, syncOnWifiOnly = syncOnWifiOnly,
            syncIntervalMinutes = syncIntervalMinutes, syncPositions = syncPositions, syncBookmarks = syncBookmarks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val settings = getReaderSettingsUseCase.execute("global")
            _fontSize.value = settings.fontSize
            _fontFamily.value = settings.fontFamily
            _lineHeight.value = settings.lineHeight
            _margin.value = settings.margin
            _theme.value = settings.theme
            _keepScreenOn.value = settings.keepScreenOn
            _pageTurnAnimation.value = settings.pageTurnAnimation
        }
    }

    fun updateFontSize(size: Float) {
        _fontSize.value = size
        saveSettings()
    }

    fun updateFontFamily(family: String) {
        _fontFamily.value = family
        saveSettings()
    }

    fun updateLineHeight(height: Float) {
        _lineHeight.value = height
        saveSettings()
    }

    fun updateMargin(margin: Float) {
        _margin.value = margin
        saveSettings()
    }

    fun updateTheme(theme: ReaderSettings.ReadingTheme) {
        _theme.value = theme
        saveSettings()
    }

    fun updateKeepScreenOn(enabled: Boolean) {
        _keepScreenOn.value = enabled
        saveSettings()
    }

    fun updatePageTurnAnimation(animation: ReaderSettings.PageTurnAnimation) {
        _pageTurnAnimation.value = animation
        saveSettings()
    }

    fun updateTTSSpeechRate(rate: Float) {
        _ttsSpeechRate.value = rate
    }

    fun updateTTSPitch(pitch: Float) {
        _ttsPitch.value = pitch
    }

    fun updateTTSAutoScroll(enabled: Boolean) {
        _ttsAutoScroll.value = enabled
    }

    fun updateSyncEnabled(enabled: Boolean) {
        _syncEnabled.value = enabled
    }

    fun updateSyncOnWifiOnly(enabled: Boolean) {
        _syncOnWifiOnly.value = enabled
    }

    fun updateSyncPositions(enabled: Boolean) {
        _syncPositions.value = enabled
    }

    fun updateSyncBookmarks(enabled: Boolean) {
        _syncBookmarks.value = enabled
    }

    fun clearCache() {
        // Clear cache implementation
    }

    private fun saveSettings() {
        viewModelScope.launch {
            val settings = ReaderSettings(
                bookId = "global",
                fontFamily = _fontFamily.value,
                fontSize = _fontSize.value,
                lineHeight = _lineHeight.value,
                margin = _margin.value,
                theme = _theme.value,
                keepScreenOn = _keepScreenOn.value,
                pageTurnAnimation = _pageTurnAnimation.value
            )
            saveReaderSettingsUseCase.execute(settings)
        }
    }
}

data class SettingsUiState(
    val fontSize: Float = 1.0f,
    val fontFamily: String = "System Default",
    val lineHeight: Float = 1.5f,
    val margin: Float = 1.0f,
    val theme: ReaderSettings.ReadingTheme = ReaderSettings.ReadingTheme.SYSTEM,
    val keepScreenOn: Boolean = true,
    val pageTurnAnimation: ReaderSettings.PageTurnAnimation = ReaderSettings.PageTurnAnimation.SLIDE,
    val ttsLanguage: String = "en-US",
    val ttsSpeechRate: Float = 1.0f,
    val ttsPitch: Float = 1.0f,
    val ttsAutoScroll: Boolean = true,
    val syncEnabled: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val syncIntervalMinutes: Int = 30,
    val syncPositions: Boolean = true,
    val syncBookmarks: Boolean = true
)