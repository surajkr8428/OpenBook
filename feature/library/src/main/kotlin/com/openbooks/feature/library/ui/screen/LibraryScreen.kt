package com.openbooks.feature.library.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.Crossfade
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.feature.library.ui.viewmodel.LibraryViewModel
import com.openbooks.feature.library.ui.theme.OpenBooksTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filed.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filed.ViewList
import androidx.compose.material.icons.filed.GridView
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.layout.Offset
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.openbooks.feature.library.ui.viewmodel.LibraryUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onBookClick: (Book) -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var viewMode by remember { mutableStateOf(ViewMode.GRID) }
    var showSortMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top
    ) {
        TopAppBar(
            title = { Text("OpenBooks", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OpenBooksTheme.colorScheme.surfaceContainer
            ),
            actions = {
                IconButton(onClick = { viewMode = ViewMode.GRID }) {
                    Icon(Icons.Filled.GridView, contentDescription = "Grid View")
                }
                IconButton(onClick = { viewMode = ViewMode.LIST }) {
                    Icon(Icons.Filled.ViewList, contentDescription = "List View")
                }
                IconButton(onClick = { showSortMenu = true }) {
                    Icon(Icons.Filled.Search, contentDescription = "Sort & Filter")
                }
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
            }
        )

        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Sort by Title") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Sort by Author") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Sort by Recent") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Sort by Progress") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Filter: All") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Filter: EPUB") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Filter: PDF") }
            DropdownMenuItem(onClick = { showSortMenu = false }) { Text("Filter: Comics") }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    // Loading indicator
                }
                uiState.books.isEmpty() -> {
                    // Empty state
                    Text("No books found. Add books to get started.", modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center))
                }
                else -> {
                    if (viewMode == ViewMode.GRID) {
                        BookGrid(books = uiState.books, onBookClick = onBookClick)
                    } else {
                        BookList(books = uiState.books, onBookClick = onBookClick)
                    }
                }
            }
        }

        NavigationBar {
            val items = listOf(
                NavigationItem("Library", Icons.Filled.Book, "library"),
                NavigationItem("Collections", Icons.Filled.Folder, "collections"),
                NavigationItem("Search", Icons.Filled.Search, "search")
            )
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = index == 0,
                    onClick = { /* Handle navigation */ },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) }
                )
            }
        }
    }
}

@Composable
fun BookGrid(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(books) { book ->
            BookGridItem(book = book, onClick = { onBookClick(book) })
        }
    }
}

@Composable
fun BookGridItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = onClick)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(
            model = book.coverPath ?: "https://via.placeholder.com/200x300",
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(120.dp, 180.dp)
                .clip(androidx.compose.ui.graphics.RoundedCornerShape(8.dp)),
            placeholder = Crossfade(true),
            error = Crossfade(true),
            fallback = Crossfade(true)
        )
        
        Text(
            text = book.title,
            maxLines = 2,
            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            fontSize = 12.sp
        )
        
        Text(
            text = book.authors.joinToString(", "),
            maxLines = 1,
            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@Composable
fun BookList(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(books) { book ->
            BookListItem(book = book, onClick = { onBookClick(book) })
        }
    }
}

@Composable
fun BookListItem(
    book: Book,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .pointerInput(Unit) { detectTapGestures(onTap = onClick) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = book.coverPath ?: "https://via.placeholder.com/60x90",
            contentDescription = book.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp, 90.dp)
                .clip(androidx.compose.ui.graphics.RoundedCornerShape(4.dp)),
            placeholder = Crossfade(true),
            error = Crossfade(true)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = book.title,
                maxLines = 2,
                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = book.authors.joinToString(", "),
                maxLines = 1,
                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
            
            Row {
                BookFormatChip(format = book.format)
                if (book.readingProgress > 0) {
                    Text(
                        text = "${(book.readingProgress * 100).toInt()}% read",
                        fontSize = 10.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        Text(
            text = formatProgress(book.readingProgress),
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
fun BookFormatChip(format: BookFormat, modifier: Modifier = Modifier) {
    val (label, color) = when (format) {
        BookFormat.EPUB -> "EPUB" to androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer
        BookFormat.PDF -> "PDF" to androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer
        BookFormat.CBZ, BookFormat.CBR -> "Comic" to androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
        else -> format.name to androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainerHighest
    }
    
    androidx.compose.material3.Chip(
        onClick = {},
        modifier = modifier.padding(top = 4.dp),
        colors = androidx.compose.material3.ChipDefaults.chipColors(containerColor = color)
    ) {
        Text(text = label, fontSize = 10.sp)
    }
}

private fun formatProgress(progress: Float): String {
    return "${(progress * 100).toInt()}%"
}

enum class ViewMode {
    GRID, LIST
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.material.icons.filled.ImageVector,
    val route: String
)