package com.openbooks.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.openbooks.domain.model.TTSConfig
import com.openbooks.domain.tts.TTSEngine
import com.openbooks.domain.tts.TTSProgress
import com.openbooks.domain.tts.TTSState
import com.openbooks.domain.tts.TTSVoice
import com.openbooks.domain.tts.TTSWordHighlight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

class AndroidTTSEngine @Inject constructor(
    private val context: Context,
    private val scope: CoroutineScope
) : TTSEngine {
    private var tts: TextToSpeech? = null
    private var currentConfig: TTSConfig = TTSConfig()
    private val wordHighlightChannel = Channel<TTSWordHighlight>(Channel.UNLIMITED)
    private val stateFlow = MutableStateFlow(TTSState.IDLE)
    private val progressFlow = MutableStateFlow(TTSProgress(0, 0, 0, 0, 0f))
    private var currentText = ""
    private var currentPosition = 0L
    private var totalDuration = 0L

    override val isPlaying: Boolean get() = stateFlow.value == TTSState.PLAYING
    override val isPaused: Boolean get() = stateFlow.value == TTSState.PAUSED
    override val currentPosition: Long get() = currentPosition
    override val duration: Long get() = totalDuration

    override suspend fun initialize(config: TTSConfig) {
        currentConfig = config
        stateFlow.value = TTSState.INITIALIZING
        
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                applyConfig(config)
                stateFlow.value = TTSState.IDLE
            } else {
                stateFlow.value = TTSState.ERROR
            }
        }
        
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                stateFlow.value = TTSState.PLAYING
            }
            
            override fun onDone(utteranceId: String) {
                stateFlow.value = TTSState.STOPPED
                currentPosition = totalDuration
                progressFlow.value = progressFlow.value.copy(
                    currentPosition = totalDuration,
                    chapterProgress = 1f
                )
            }
            
            override fun onError(utteranceId: String) {
                stateFlow.value = TTSState.ERROR
            }
        })
    }

    override suspend fun speak(text: String, config: TTSConfig) {
        currentConfig = config
        currentText = text
        applyConfig(config)
        
        val utteranceId = "speak_${System.currentTimeMillis()}"
        val params = android.os.Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
        }
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        totalDuration = estimateDuration(text)
    }

    override suspend fun speakChapter(chapterText: String, config: TTSConfig) {
        speak(chapterText, config)
    }

    override suspend fun pause() {
        tts?.pause()
        stateFlow.value = TTSState.PAUSED
    }

    override suspend fun resume() {
        tts?.resume()
        stateFlow.value = TTSState.PLAYING
    }

    override suspend fun stop() {
        tts?.stop()
        stateFlow.value = TTSState.STOPPED
        currentPosition = 0
    }

    override suspend fun seekTo(position: Long) {
        // Android TTS doesn't support seeking directly
        // Would need to split text and restart from position
        stop()
        // Implementation would require text segmentation
    }

    override suspend fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate)
        currentConfig = currentConfig.copy(speechRate = rate)
    }

    override suspend fun setPitch(pitch: Float) {
        tts?.setPitch(pitch)
        currentConfig = currentConfig.copy(pitch = pitch)
    }

    override suspend fun setVolume(volume: Float) {
        // Volume is handled by system stream volume
        currentConfig = currentConfig.copy(volume = volume)
    }

    override suspend fun setVoice(voiceName: String) {
        tts?.voice?.let { voice ->
            if (voice.name == voiceName) return
        }
        tts?.voices?.firstOrNull { it.name == voiceName }?.let { voice ->
            tts?.setVoice(voice)
            currentConfig = currentConfig.copy(voiceName = voiceName)
        }
    }

    override fun getAvailableVoices(): List<TTSVoice> {
        return tts?.voices?.map { voice ->
            TTSVoice(
                name = voice.name,
                language = voice.locale.toLanguageTag(),
                gender = when (voice.name.lowercase()) {
                    *.contains("female") -> TTSGender.FEMALE
                    *.contains("male") -> TTSGender.MALE
                    else -> TTSGender.NEUTRAL
                },
                quality = TTSQuality.MEDIUM,
                networkRequired = voice.quality == TextToSpeech.Engine.CHECK_VOICE_DATA_FAIL
            )
        } ?: emptyList()
    }

    override fun observeState() = stateFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(), TTSState.IDLE)

    override fun observeProgress() = progressFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(), TTSProgress(0, 0, 0, 0, 0f))

    override fun observeWordHighlight(): ReceiveChannel<TTSWordHighlight> = wordHighlightChannel

    private fun applyConfig(config: TTSConfig) {
        tts?.setSpeechRate(config.speechRate)
        tts?.setPitch(config.pitch)
        config.voiceName?.let { setVoice(it) }
    }

    private fun estimateDuration(text: String): Long {
        // Rough estimate: ~150 words per minute
        val words = text.split("\\s+".toRegex()).count { it.isNotBlank() }
        return (words / 150.0 * 60 * 1000).toLong()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}