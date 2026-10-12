package com.openbooks.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "reader_settings")
data class ReaderSettings(
    @PrimaryKey val bookId: String = "global",
    val fontFamily: String = "System Default",
    val fontSize: Float = 1.0f,
    val lineHeight: Float = 1.5f,
    val margin: Float = 1.0f,
    val theme: ReadingTheme = ReadingTheme.SYSTEM,
    val backgroundColor: Int = 0xFFFFFFFF,
    val textColor: Int = 0xFF000000,
    val pageTurnAnimation: PageTurnAnimation = PageTurnAnimation.SLIDE,
    val scrollDirection: ScrollDirection = ScrollDirection.VERTICAL,
    val showPageNumbers: Boolean = true,
    val showProgressBar: Boolean = true,
    val keepScreenOn: Boolean = true,
    val hyphenation: Boolean = true,
    val justification: TextAlignment = TextAlignment.JUSTIFIED,
    val paragraphSpacing: Float = 0f,
    val textAlign: TextAlignment = TextAlignment.JUSTIFIED,
    val customFontPath: String? = null,
    val brightness: Float = -1f,
    val fontWeight: Int = 400,
    val letterSpacing: Float = 0f
)

@Serializable
enum class ReadingTheme {
    LIGHT, DARK, SEPIA, SYSTEM, CUSTOM
}

@Serializable
enum class PageTurnAnimation {
    NONE, SLIDE, CURL, FADE
}

@Serializable
enum class ScrollDirection {
    VERTICAL, HORIZONTAL
}

@Serializable
enum class TextAlignment {
    LEFT, CENTER, RIGHT, JUSTIFIED
}

@Serializable
data class TTSConfig(
    val language: String = "en-US",
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val volume: Float = 1.0f,
    val voiceName: String? = null,
    val autoScroll: Boolean = true,
    val highlightText: Boolean = true,
    val skipFootnotes: Boolean = true,
    val pauseBetweenParagraphs: Long = 500
)

@Serializable
data class SyncConfig(
    val enabled: Boolean = true,
    val syncOnWifiOnly: Boolean = true,
    val syncIntervalMinutes: Int = 30,
    val syncPositions: Boolean = true,
    val syncBookmarks: Boolean = true,
    val syncNotes: Boolean = true,
    val syncCollections: Boolean = true,
    val conflictResolution: ConflictResolution = ConflictResolution.LAST_WRITE_WINS
)

@Serializable
enum class ConflictResolution {
    LAST_WRITE_WINS, LOCAL_WINS, REMOTE_WINS, MANUAL
}