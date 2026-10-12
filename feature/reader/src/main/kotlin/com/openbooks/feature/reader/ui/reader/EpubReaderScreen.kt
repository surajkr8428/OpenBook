package com.openbooks.feature.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.TableOfContents
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.LaunchedEffect
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.feature.reader.ui.viewmodel.ReaderViewModel
import com.openbooks.feature.reader.ui.theme.OpenBooksTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpubReaderScreen(
    book: Book,
    viewModel: ReaderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var showToolbar by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showTOC by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showTTS by remember { mutableStateOf(false) }
    
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentChapter by viewModel.currentChapter.collectAsStateWithLifecycle()
    val chapterContent by viewModel.chapterContent.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Reading area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OpenBooksTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showToolbar = !showToolbar },
                        onDoubleTap = { /* Zoom or other action */ }
                    )
                }
        ) {
            // Chapter content rendered here
            androidx.compose.foundation.layout.Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = settings.margin * 24.dp, vertical = settings.margin * 16.dp)
            ) {
                androidx.compose.ui.text.Text(
                    text = chapterContent,
                    fontSize = 16.sp * settings.fontSize,
                    lineHeight = androidx.compose.ui.unit.sp(24 * settings.lineHeight),
                    textAlign = when (settings.justification) {
                        "JUSTIFIED" -> TextAlign.Justify
                        "LEFT" -> TextAlign.Start
                        "CENTER" -> TextAlign.Center
                        "RIGHT" -> TextAlign.End
                        else -> TextAlign.Justify
                    },
                    color = settings.textColor,
                    fontFamily = androidx.compose.ui.text.font.FontFamily(settings.fontFamily),
                    fontWeight = androidx.compose.ui.text.font.FontWeight(settings.fontWeight)
                )
            }
        }

        // Toolbar
        if (showToolbar) {
            ReaderToolbar(
                book = book,
                onBack = { /* Navigate back */ },
                onMenuClick = { showMenu = true },
                onTOCClick = { showTOC = true },
                onBookmarkClick = { showBookmarks = true },
                onSettingsClick = { showSettings = true },
                onTTSClick = { showTTS = true }
            )
        }

        // Menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(onClick = { showMenu = false; showTOC = true }) {
                Row {
                    Icon(Icons.Filled.TableOfContents, contentDescription = "TOC")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Table of Contents")
                }
            }
            DropdownMenuItem(onClick = { showMenu = false; showBookmarks = true }) {
                Row {
                    Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Bookmarks")
                }
            }
            DropdownMenuItem(onClick = { showMenu = false; showSettings = true }) {
                Row {
                    Icon(Icons.Filled.FormatSize, contentDescription = "Settings")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Reading Settings")
                }
            }
            DropdownMenuItem(onClick = { showMenu = false; showTTS = true }) {
                Row {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "TTS")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Text-to-Speech")
                }
            }
        }

        // Bottom progress bar
        if (showToolbar) {
            ReadingProgressBar(
                progress = viewModel.readingProgress.value,
                chapterTitle = currentChapter?.title ?: "",
                onProgressChange = { viewModel.seekToChapterProgress(it) }
            )
        }
    }
}

@Composable
fun ReaderToolbar(
    book: Book,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onTOCClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTTSClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = { Text(book.title, maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
            actions = {
                IconButton(onClick = onTTSClick) { Icon(Icons.Filled.PlayArrow, contentDescription = "TTS") }
                IconButton(onClick = onSettingsClick) { Icon(Icons.Filled.FormatSize, contentDescription = "Settings") }
                IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OpenBooksTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
            )
        )
    }
}

@Composable
fun ReadingProgressBar(
    progress: Float,
    chapterTitle: String,
    onProgressChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = chapterTitle, fontSize = 12.sp, color = OpenBooksTheme.colorScheme.onSurfaceVariant)
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Slider(
            value = progress,
            onValueChange = onProgressChange,
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.SliderDefaults.colors(
                thumbColor = OpenBooksTheme.colorScheme.primary,
                activeTrackColor = OpenBooksTheme.colorScheme.primary,
                inactiveTrackColor = OpenBooksTheme.colorScheme.outlineVariant
            )
        )
    }
}