package com.openbooks.feature.reader.ui.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.LaunchedEffect
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.TOCEntry
import com.openbooks.feature.reader.ui.viewmodel.ReaderViewModel
import com.openbooks.feature.annotations.AnnotationsExporter
import com.openbooks.app.ui.theme.OpenBooksTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderMenuBottomSheet(
    book: Book,
    viewModel: ReaderViewModel,
    exporter: AnnotationsExporter,
    toc: List<TOCEntry>,
    bookmarks: List<Bookmark>,
    notes: List<Note>,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(MenuTab.CONTENTS) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchResults by remember { mutableStateOf(false) }
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .padding(16.dp)
    ) {
        // Tab row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuTab.values().forEach { tab ->
                Button(
                    onClick = { selectedTab = tab },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == tab) 
                            OpenBooksTheme.colorScheme.primary 
                            else OpenBooksTheme.colorScheme.surfaceContainerHighest
                    )
                ) {
                    Text(tab.label, fontSize = 12.sp)
                }
            }
        }
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        
        // Tab content
        when (selectedTab) {
            MenuTab.CONTENTS -> TableOfContentsView(toc = toc, onItemClick = { entry ->
                viewModel.goToLocation(entry.href)
                onDismiss()
            })
            MenuTab.BOOKMARKS -> BookmarksView(bookmarks = bookmarks, onItemClick = { bookmark ->
                viewModel.goToLocation(bookmark.location)
                onDismiss()
            }, onDelete = { viewModel.deleteBookmark(it.id) })
            MenuTab.NOTES -> NotesView(notes = notes, onItemClick = { note ->
                viewModel.goToLocation(note.location)
                onDismiss()
            }, onDelete = { viewModel.deleteNote(it.id) })
            MenuTab.SEARCH -> SearchInBookView(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                results = if (showSearchResults) searchResults else emptyList(),
                onSearch = { viewModel.searchInBook(searchQuery); showSearchResults = true },
                onResultClick = { result ->
                    viewModel.goToLocation(result.cfi ?: "page://${result.pageNumber}")
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun TableOfContentsView(
    toc: List<TOCEntry>,
    onItemClick: (TOCEntry) -> Unit,
    level: Int = 0
) {
    LazyColumn {
        items(toc) { entry ->
            TOCEntryItem(entry = entry, level = level, onClick = onItemClick)
        }
    }
}

@Composable
fun TOCEntryItem(
    entry: TOCEntry,
    level: Int,
    onClick: (TOCEntry) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (16 + level * 16).dp, top = 8.dp, bottom = 8.dp, end = 16.dp)
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectTapGestures(onTap = { onClick(entry) })
            }
    ) {
        androidx.compose.material3.Text(
            text = entry.title,
            maxLines = 1,
            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
    
    if (entry.children.isNotEmpty()) {
        TableOfContentsView(toc = entry.children, onItemClick = onClick, level = level + 1)
    }
}

@Composable
fun BookmarksView(
    bookmarks: List<Bookmark>,
    onItemClick: (Bookmark) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (bookmarks.isEmpty()) {
        EmptyStateView(
            icon = androidx.compose.material.icons.filled.Bookmark,
            title = "No Bookmarks",
            message = "Tap the bookmark icon while reading to save your place"
        )
    } else {
        LazyColumn {
            items(bookmarks) { bookmark ->
                BookmarkItem(
                    bookmark = bookmark,
                    onClick = { onItemClick(bookmark) },
                    onDelete = { onDelete(bookmark.id) }
                )
            }
        }
    }
}

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectTapGestures(onTap = onClick)
            }
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column {
                androidx.compose.material3.Text(
                    text = "Page ${bookmark.pageNumber} • Chapter ${bookmark.chapterIndex + 1}",
                    fontWeight = FontWeight.Medium
                )
                if (bookmark.note != null) {
                    androidx.compose.material3.Text(
                        text = bookmark.note!!,
                        fontSize = 12.sp,
                        color = OpenBooksTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
                    )
                }
                androidx.compose.material3.Text(
                    text = bookmark.timestamp.toString(),
                    fontSize = 10.sp,
                    color = OpenBooksTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            androidx.compose.material3.IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = OpenBooksTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun NotesView(
    notes: List<Note>,
    onItemClick: (Note) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (notes.isEmpty()) {
        EmptyStateView(
            icon = androidx.compose.material.icons.filled.Edit,
            title = "No Notes",
            message = "Select text while reading to add highlights and notes"
        )
    } else {
        LazyColumn {
            items(notes) { note ->
                NoteItem(
                    note = note,
                    onClick = { onItemClick(note) },
                    onDelete = { onDelete(note.id) }
                )
            }
        }
    }
}

@Composable
fun NoteItem(
    note: Note,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectTapGestures(onTap = onClick)
            }
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                androidx.compose.material3.Text(
                    text = "Page ${note.pageNumber} • Chapter ${note.chapterIndex + 1}",
                    fontWeight = FontWeight.Medium
                )
                androidx.compose.material3.IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = OpenBooksTheme.colorScheme.error)
                }
            }
            
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            
            androidx.compose.material3.Text(
                text = "\"${note.selectedText}\"",
                fontSize = 14.sp,
                fontStyle = androidx.compose.ui.text.style.FontStyle.Italic,
                color = OpenBooksTheme.colorScheme.onSurfaceVariant
            )
            
            if (note.noteText.isNotBlank()) {
                androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    text = note.noteText,
                    fontSize = 14.sp
                )
            }
            
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            androidx.compose.material3.Text(
                text = note.timestamp.toString(),
                fontSize = 10.sp,
                color = OpenBooksTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SearchInBookView(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<com.openbooks.domain.parser.SearchResult>,
    onSearch: () -> Unit,
    onResultClick: (com.openbooks.domain.parser.SearchResult) -> Unit
) {
    Column {
        androidx.compose.material3.TextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Search in book...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Filled.Close, contentDescription = "Clear")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        
        Button(onClick = onSearch, modifier = Modifier.fillMaxWidth()) {
            Text("Search")
        }
        
        if (results.isNotEmpty()) {
            androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
            Text("Results (${results.size})", fontWeight = FontWeight.Medium)
            androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
            
            LazyColumn {
                items(results) { result ->
                    SearchResultItem(result = result, onClick = { onResultClick(result) })
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: com.openbooks.domain.parser.SearchResult,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .pointerInput(Unit) {
                androidx.compose.foundation.gestures.detectTapGestures(onTap = onClick)
            }
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.material3.Text(
                text = "Page ${result.pageNumber} • Chapter ${result.chapterIndex + 1}",
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = OpenBooksTheme.colorScheme.primary
            )
            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
            androidx.compose.material3.Text(
                text = result.context,
                maxLines = 3,
                overflow = androidx.compose.ui.text.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EmptyStateView(
    icon: androidx.compose.material.icons.filled.ImageVector,
    title: String,
    message: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = OpenBooksTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
        Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = OpenBooksTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.TextAlign.Center
        )
    }
}

enum class MenuTab {
    CONTENTS("Contents", Icons.Filled.List),
    BOOKMARKS("Bookmarks", Icons.Filled.Bookmark),
    NOTES("Notes", Icons.Filled.Edit),
    SEARCH("Search", Icons.Filled.Search);
    
    val label: String
    val icon: androidx.compose.material.icons.filled.ImageVector
}