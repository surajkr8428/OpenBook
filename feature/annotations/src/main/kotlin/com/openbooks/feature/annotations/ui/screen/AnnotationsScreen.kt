package com.openbooks.feature.annotations.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Note
import com.openbooks.feature.annotations.AnnotationsExporter
import com.openbooks.feature.annotations.ui.viewmodel.AnnotationsViewModel
import com.openbooks.app.ui.theme.OpenBooksTheme
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnnotationsScreen(
    book: Book,
    viewModel: AnnotationsViewModel = viewModel(),
    exporter: AnnotationsExporter
) {
    var showFilterMenu by remember { mutableStateOf(false) }
    var filterType by remember { mutableStateOf(FilterType.ALL) }
    
    val bookmarks by viewModel.bookmarks.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    
    val filteredItems = remember(bookmarks, notes, filterType) {
        when (filterType) {
            FilterType.ALL -> {
                val items = mutableListOf<AnnotationItem>()
                items.addAll(bookmarks.map { AnnotationItem.BookmarkItem(it) })
                items.addAll(notes.map { AnnotationItem.NoteItem(it) })
                items.sortedByDescending { it.timestamp }
            }
            FilterType.BOOKMARKS -> bookmarks.map { AnnotationItem.BookmarkItem(it) }.sortedByDescending { it.timestamp }
            FilterType.NOTES -> notes.map { AnnotationItem.NoteItem(it) }.sortedByDescending { it.timestamp }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Annotations", fontWeight = FontWeight.Bold) },
            navigationIcon = { 
                androidx.compose.material3.IconButton(onClick = { /* Navigate back */ }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { showFilterMenu = true }) {
                    Icon(Icons.Filled.FilterList, contentDescription = "Filter")
                }
                IconButton(onClick = { exporter.shareAnnotations(book.id) }) {
                    Icon(Icons.Filled.Share, contentDescription = "Export & Share")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OpenBooksTheme.colorScheme.surfaceContainer
            )
        )

        DropdownMenu(
            expanded = showFilterMenu,
            onDismissRequest = { showFilterMenu = false }
        ) {
            DropdownMenuItem(
                onClick = { filterType = FilterType.ALL; showFilterMenu = false }
            ) {
                Row {
                    if (filterType == FilterType.ALL) Icon(Icons.Filled.CheckCircle, contentDescription = "Selected")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("All")
                }
            }
            DropdownMenuItem(
                onClick = { filterType = FilterType.BOOKMARKS; showFilterMenu = false }
            ) {
                Row {
                    if (filterType == FilterType.BOOKMARKS) Icon(Icons.Filled.CheckCircle, contentDescription = "Selected")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Bookmarks (${bookmarks.size})")
                }
            }
            DropdownMenuItem(
                onClick = { filterType = FilterType.NOTES; showFilterMenu = false }
            ) {
                Row {
                    if (filterType == FilterType.NOTES) Icon(Icons.Filled.CheckCircle, contentDescription = "Selected")
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Text("Notes (${notes.size})")
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (filteredItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = if (filterType == FilterType.BOOKMARKS) Icons.Filled.Bookmark else Icons.Filled.Edit,
                        contentDescription = "No annotations",
                        tint = OpenBooksTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    Text(
                        text = when (filterType) {
                            FilterType.BOOKMARKS -> "No bookmarks yet. Tap the bookmark icon while reading to add one."
                            FilterType.NOTES -> "No notes yet. Select text while reading to add a note."
                            else -> "No annotations yet. Add bookmarks or notes while reading."
                        },
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = OpenBooksTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredItems) { item ->
                        AnnotationCard(item = item, onDelete = { viewModel.deleteAnnotation(item) })
                    }
                }
            }
        }
    }
}

@Composable
fun AnnotationCard(
    item: AnnotationItem,
    onDelete: (AnnotationItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = when (item) {
                            is AnnotationItem.BookmarkItem -> Icons.Filled.Bookmark
                            is AnnotationItem.NoteItem -> Icons.Filled.Edit
                        },
                        contentDescription = when (item) {
                            is AnnotationItem.BookmarkItem -> "Bookmark"
                            is AnnotationItem.NoteItem -> "Note"
                        },
                        tint = OpenBooksTheme.colorScheme.primary
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            text = when (item) {
                                is AnnotationItem.BookmarkItem -> "Bookmark - Page ${item.bookmark.pageNumber}"
                                is AnnotationItem.NoteItem -> "Note - Page ${item.note.pageNumber}"
                            },
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = item.timestamp.toString(),
                            fontSize = 12.sp,
                            color = OpenBooksTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                androidx.compose.material3.IconButton(onClick = { onDelete(item) }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = OpenBooksTheme.colorScheme.error)
                }
            }
            
            when (item) {
                is AnnotationItem.NoteItem -> {
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(start = 36.dp)) {
                        Text(
                            text = "\"${item.note.selectedText}\"",
                            fontSize = 14.sp,
                            fontStyle = androidx.compose.ui.text.style.FontStyle.Italic,
                            color = OpenBooksTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.note.noteText.isNotBlank()) {
                            androidx.compose.foundation.layout.Spacer(Modifier.height(4.dp))
                            Text(
                                text = item.note.noteText,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                is AnnotationItem.BookmarkItem -> {
                    if (item.bookmark.note.isNotNullOrBlank()) {
                        androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                        androidx.compose.foundation.layout.Column(modifier = Modifier.padding(start = 36.dp)) {
                            Text(
                                text = item.bookmark.note!!,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

sealed class AnnotationItem {
    data class BookmarkItem(val bookmark: Bookmark) : AnnotationItem() {
        val timestamp = bookmark.timestamp
        val pageNumber = bookmark.pageNumber
    }
    
    data class NoteItem(val note: Note) : AnnotationItem() {
        val timestamp = note.timestamp
        val pageNumber = note.pageNumber
    }
}

enum class FilterType {
    ALL, BOOKMARKS, NOTES
}