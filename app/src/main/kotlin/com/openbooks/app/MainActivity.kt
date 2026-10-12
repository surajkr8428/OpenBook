package com.openbooks.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.openbooks.feature.library.ui.LibraryActivity
import com.openbooks.feature.library.ui.viewmodel.LibraryViewModel
import com.openbooks.feature.reader.ui.screen.ReaderScreen
import com.openbooks.feature.settings.ui.SettingsScreen
import com.openbooks.feature.store.ui.StoreScreen
import com.openbooks.app.ui.theme.OpenBooksTheme

class MainActivity : ComponentActivity() {
    private val libraryViewModel: LibraryViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            OpenBooksTheme {
                val navController = rememberNavController()
                NavHost(navController, "library") {
                    composable("library") {
                        LibraryScreen(
                            viewModel = libraryViewModel,
                            onBookClick = { book ->
                                navController.navigate("reader/${book.id}")
                            },
                            onSettingsClick = {
                                navController.navigate("settings")
                            },
                            onStoreClick = {
                                navController.navigate("store")
                            }
                        )
                    }
                    composable(
                        route = "reader/{bookId}",
                        arguments = listOf(androidx.navigation.navArgument("bookId") { type = androidx.navigation.NavType.StringType })
                    ) { backStackEntry ->
                        val bookId = backStackEntry.getString()!!
                        ReaderScreen(bookId = bookId)
                    }
                    composable("settings") {
                        SettingsScreen()
                    }
                    composable("store") {
                        StoreScreen()
                    }
                }
            }
        }
    }
}