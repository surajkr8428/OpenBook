package com.openbooks.app

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import com.openbooks.app.ui.theme.OpenBooksTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var showCategoryMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Book Store", fontWeight = FontWeight.Bold) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OpenBooksTheme.colorScheme.surfaceContainer
            ),
            actions = {
                IconButton(onClick = { showCategoryMenu = true }) {
                    Icon(Icons.Filled.Category, contentDescription = "Categories")
                }
            }
        )

        DropdownMenu(
            expanded = showCategoryMenu,
            onDismissRequest = { showCategoryMenu = false }
        ) {
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("All Categories") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Fiction") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Non-Fiction") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Science Fiction") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Fantasy") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Mystery") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Romance") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Biography") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("History") }
            DropdownMenuItem(onClick = { showCategoryMenu = false }) { Text("Self-Help") }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            // Featured section
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text("Featured Free Books", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    androidx.compose.foundation.layout.Spacer(Modifier.height(16.dp))
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(20) { index ->
                            StoreBookItem(
                                title = "Free Book ${index + 1}",
                                author = "Author Name",
                                coverUrl = "https://via.placeholder.com/200x300",
                                isFree = true
                            )
                        }
                    }
                }
            }
        }

        NavigationBar {
            val items = listOf(
                NavigationItem("Library", Icons.Filled.Book, 0),
                NavigationItem("Collections", Icons.Filled.Folder, 1),
                NavigationItem("Store", Icons.Filled.Store, 2),
                NavigationItem("Settings", Icons.Filled.Settings, 3)
            )
            items.forEachIndexed { index, item ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    label = { Text(item.label) }
                )
            }
        }
    }
}

@Composable
fun StoreBookItem(
    title: String,
    author: String,
    coverUrl: String,
    isFree: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.Image(
            painter = coil3.compose.rememberAsyncImagePainter(coverUrl),
            contentDescription = title,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier
                .size(120.dp, 180.dp)
                .clip(androidx.compose.ui.graphics.RoundedCornerShape(8.dp))
        )
        
        Text(
            text = title,
            maxLines = 2,
            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            fontSize = 12.sp
        )
        
        Text(
            text = author,
            maxLines = 1,
            overflow = androidx.compose.ui.text.TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
        
        if (isFree) {
            androidx.compose.material3.Text(
                text = "FREE",
                color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.material.icons.filled.ImageVector,
    val tabIndex: Int
)