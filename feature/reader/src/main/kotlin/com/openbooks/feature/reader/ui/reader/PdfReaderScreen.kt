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
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.TableOfContents
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material.icons.filled.CropRotate
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
fun PdfReaderScreen(
    book: Book,
    viewModel: ReaderViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var showToolbar by remember { mutableStateOf(true) }
    var showMenu by remember { mutableStateOf(false) }
    var showTOC by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var showTTS by remember { mutableStateOf(false) }
    
    val scale by remember { mutableStateOf(1f) }
    val offset by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val totalPages by viewModel.totalPages.collectAsStateWithLifecycle()
    val pageBitmap by viewModel.pageBitmap.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // PDF View area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(OpenBooksTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { showToolbar = !showToolbar },
                        onDoubleTap = { /* Reset zoom */ }
                    )
                    detectTransformGestures(
                        onGesture = { centroid, pan, zoom, rotation ->
                            // Handle pinch zoom and pan
                        }
                    )
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
                            }
                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, pan, zoom, rotation ->
                                    // Handle gestures
                                }
                            }
                    )
                }
            }
        }

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

        if (showToolbar) {
            ReadingProgressBar(
                progress = (currentPage + 1).toFloat() / totalPages,
                chapterTitle = "Page ${currentPage + 1} of $totalPages",
                onProgressChange = { viewModel.goToPage((it * totalPages).toInt()) }
            )
        }
    }
}