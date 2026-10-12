package com.openbooks.feature.library.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openbooks.feature.library.ui.screen.BookDetailScreen
import com.openbooks.feature.library.ui.screen.LibraryScreen
import com.openbooks.feature.library.ui.screen.ReaderScreen
import com.openbooks.feature.library.ui.viewmodel.LibraryViewModel
import com.openbooks.feature.reader.ui.screen.ReaderScreen as FeatureReaderScreen

class LibraryActivity : ComponentActivity() {
    private val libraryViewModel: LibraryViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenBooksTheme {
                val navController = rememberNavController()
                NavHost(navController, "library") {
                    composable("library") {
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onBookClick = { book ->
                                navController.navigate("reader/$book.id")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            }
                        )
                    }
                    composable(
                        route = "reader/{bookId}",
                        arguments = listOf(navArgument("bookId") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val bookId = backStackEntry.getString()!!
                        FeatureReaderScreen(bookId = bookId)
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                }
            }
        }
    }
}