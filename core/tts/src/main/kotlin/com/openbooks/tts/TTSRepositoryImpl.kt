package com.openbooks.tts

import com.openbooks.domain.model.TTSConfig
import com.openbooks.domain.tts.TTSConfig
import com.openbooks.domain.tts.TTSRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class TTSRepositoryImpl @Inject constructor(
    private val context: android.content.Context
) : TTSRepository {
    
    private val _config = MutableStateFlow(TTSConfig())
    val config = _config.asStateFlow()
    
    override suspend fun getConfig(): TTSConfig = _config.value
    
    override suspend fun saveConfig(config: TTSConfig) {
        _config.value = config
        // Save to preferences
        val prefs = context.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("language", config.language)
            putFloat("speech_rate", config.speechRate)
            putFloat("pitch", config.pitch)
            putFloat("volume", config.volume)
            putString("voice_name", config.voiceName ?: "")
            putBoolean("auto_scroll", config.autoScroll)
            putBoolean("highlight_text", config.highlightText)
            putBoolean("skip_footnotes", config.skipFootnotes)
            putLong("pause_between_paragraphs", config.pauseBetweenParagraphs)
        }
    }
    
    override fun observeConfig() = _config
        .asStateFlow()
        .distinctUntilChanged()
    
    init {
        loadConfig()
    }
    
    private fun loadConfig() {
        val prefs = context.getSharedPreferences("tts_prefs", Context.MODE_PRIVATE)
        _config.value = TTSConfig(
            language = prefs.getString("language", "en-US") ?: "en-US",
            speechRate = prefs.getFloat("speech_rate", 1.0f),
            pitch = prefs.getFloat("pitch", 1.0f),
            volume = prefs.getFloat("volume", 1.0f),
            voiceName = prefs.getString("voice_name", "").takeIf { it.isNotBlank() },
            autoScroll = prefs.getBoolean("auto_scroll", true),
            highlightText = prefs.getBoolean("highlight_text", true),
            skipFootnotes = prefs.getBoolean("skip_footnotes", true),
            pauseBetweenParagraphs = prefs.getLong("pause_between_paragraphs", 500)
        )
    }
}