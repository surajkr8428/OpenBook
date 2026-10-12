package com.openbooks.feature.reader.ui.reader

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.TableOfContents
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComicsReaderScreen(
    book: Book,
    viewModel: ReaderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var showToolbar by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showTOC by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showTTS by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var rotation by remember { mutableStateOf(0) }
    
    val scale by remember { mutableStateOf(1f) }
    val offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalPages by viewModel.totalPages.collectAsStateWithLifecycle()
    val pageBitmap by viewModel.pageBitmap.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isFullscreen) Color.Black else OpenBooksTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { 
                            if (!isFullscreen) showToolbar = !showToolbar 
                        },
                        onDoubleTap = { /* Toggle fullscreen */ }
                    )
                    detectTransformGestures { centroid, pan, zoom, rotation ->
                        // Handle pinch zoom and pan
                    }
                }
        ) {
            pageBitmap?.let { bitmap ->
                androidx.compose.ui.graphics.ImageBitmap.asImageBitmap(bitmap).let { imageBitmap ->
                    androidx.compose.foundation.Image(
                        bitmap = imageBitmap,
                        contentDescription = "Page ${currentPage + 1}",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offset.x
                                translationY = offset.y
                                rotationZ = rotation.toFloat()
                            }
                    )
                }
            }
        }

        if (showToolbar && !isFullscreen) {
            ComicsToolbar(
                book = book,
                currentPage = currentPage,
                totalPages = totalPages,
                rotation = rotation,
                onBack = { /* Navigate back */ },
                onMenuClick = { showMenu = true },
                onTOCClick = { showTOC = true },
                onBookmarkClick = { showBookmarks = true },
                onSettingsClick = { showSettings = true },
                onTTSClick = { showTTS = true },
                onRotateLeft = { rotation = (rotation - 90) % 360 },
                onRotateRight = { rotation = (rotation + 90) % 360 },
                onFullscreenToggle = { isFullscreen = !isFullscreen }
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            DropdownMenuItem(onClick = { showMenu = false; showTOC = true }) {
                Row {
                    Icon(Icons.Filled.TableOfContents, contentDescription = "TOC")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Pages")
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
        }

        if (showToolbar && !isFullscreen) {
            ReadingProgressBar(
                progress = (currentPage + 1).toFloat() / totalPages,
                chapterTitle = "Page ${currentPage + 1} of $totalPages",
                onProgressChange = { viewModel.goToPage((it * totalPages).toInt()) }
            )
        }
    }
}

@Composable
fun ComicsToolbar(
    book: Book,
    currentPage: Int,
    totalPages: Int,
    rotation: Int,
    onBack: () -> Unit,
    onMenuClick: () -> Unit,
    onTOCClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onTTSClick: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFullscreenToggle: () -> Unit
) {
    TopAppBar(
        title = { Text("${book.title} - Page ${currentPage + 1}/$totalPages", maxLines = 1, overflow = androidx.compose.ui.text.TextOverflow.Ellipsis) },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } },
        actions = {
            IconButton(onClick = onRotateLeft) { Icon(Icons.Filled.RotateLeft, contentDescription = "Rotate Left") }
            IconButton(onClick = onRotateRight) { Icon(Icons.Filled.RotateRight, contentDescription = "Rotate Right") }
            IconButton(onClick = onFullscreenToggle) { 
                Icon(Icons.Filled.Fullscreen, contentDescription = "Fullscreen") 
            }
            IconButton(onClick = onSettingsClick) { Icon(Icons.Filled.FormatSize, contentDescription = "Settings") }
            IconButton(onClick = onMenuClick) { Icon(Icons.Filled.Menu, contentDescription = "Menu") }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = OpenBooksTheme.colorScheme.surfaceContainer.copy(alpha = 0.9f)
        )
    )
}