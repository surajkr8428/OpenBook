package com.openbooks.domain.tts

import com.openbooks.domain.model.TTSConfig
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.Flow

interface TTSEngine {
    val isPlaying: Boolean
    val isPaused: Boolean
    val currentPosition: Long
    val duration: Long
    
    suspend fun initialize(config: TTSConfig)
    suspend fun speak(text: String, config: TTSConfig)
    suspend fun speakChapter(chapterText: String, config: TTSConfig)
    suspend fun pause()
    suspend fun resume()
    suspend fun stop()
    suspend fun seekTo(position: Long)
    suspend fun setSpeechRate(rate: Float)
    suspend fun setPitch(pitch: Float)
    suspend fun setVolume(volume: Float)
    suspend fun setVoice(voiceName: String)
    fun getAvailableVoices(): List<TTSVoice>
    fun observeState(): Flow<TTSState>
    fun observeProgress(): Flow<TTSProgress>
    fun observeWordHighlight(): ReceiveChannel<TTSWordHighlight>
}

@Serializable
data class TTSVoice(
    val name: String,
    val language: String,
    val gender: TTSGender,
    val quality: TTSQuality,
    val networkRequired: Boolean
)

@Serializable
enum class TTSGender {
    MALE, FEMALE, NEUTRAL
}

@Serializable
enum class TTSQuality {
    LOW, MEDIUM, HIGH, PREMIUM
}

@Serializable
enum class TTSState {
    IDLE, INITIALIZING, PLAYING, PAUSED, STOPPED, ERROR
}

@Serializable
data class TTSProgress(
    val currentPosition: Long,
    val duration: Long,
    val currentWordIndex: Int,
    val currentSentenceIndex: Int,
    val chapterProgress: Float
)

@Serializable
data class TTSWordHighlight(
    val word: String,
    val startIndex: Int,
    val endIndex: Int,
    val timestamp: Long
)

interface TTSRepository {
    suspend fun getConfig(): TTSConfig
    suspend fun saveConfig(config: TTSConfig)
    fun observeConfig(): Flow<TTSConfig>
}