package com.openbooks.feature.reader.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.material3.Card
import androidx.material3.Icon
import androidx.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.TableOfContents
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.LaunchedEffect
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.TOCEntry
import com.openbooks.feature.reader.ui.reader.ReaderMenuBottomSheet
import com.openbooks.feature.reader.ui.reader.StatisticsScreen
import com.openbooks.feature.reader.ui.viewmodel.ReaderViewModel
import com.openbooks.feature.annotations.AnnotationsExporter
import com.openbooks.app.ui.theme.OpenBooksTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedReaderScreen(
    book: Book,
    viewModel: ReaderViewModel = viewModel(),
    exporter: AnnotationsExporter
) {
    var showMenu by remember { mutableStateOf(false) }
    var showStatistics by remember { mutableStateOf(false) }
    
    val toc by viewModel.tableOfContents.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val readingSessions by viewModel.readingSessions.collectAsStateWithLifecycle()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Main reading area (would be EpubReaderScreen, PdfReaderScreen, or ComicsReaderScreen)
        Box(modifier = Modifier.fillMaxSize()) {
            // Reader content goes here
        }
        
        // Floating action button for menu
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            androidx.compose.material3.FloatingActionButton(
                onClick = { showMenu = true },
                containerColor = OpenBooksTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }
    }
    
    // Bottom sheet menu
    if (showMenu) {
        ReaderMenuBottomSheet(
            book = book,
            viewModel = viewModel,
            exporter = exporter,
            toc = toc,
            bookmarks = bookmarks,
            notes = notes,
            onDismiss = { showMenu = false }
        )
    }
    
    // Statistics dialog
    if (showStatistics) {
        StatisticsScreen(
            book = book,
            sessions = readingSessions,
            onDismiss = { showStatistics = false }
        )
    }
}