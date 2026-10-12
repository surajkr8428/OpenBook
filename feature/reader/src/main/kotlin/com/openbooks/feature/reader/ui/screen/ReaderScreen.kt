package com.openbooks.feature.reader.ui.screen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openbooks.feature.reader.ui.reader.EpubReaderScreen
import com.openbooks.feature.reader.ui.reader.PdfReaderScreen
import com.openbooks.feature.reader.ui.reader.ComicsReaderScreen
import com.openbooks.feature.reader.ui.viewmodel.ReaderViewModel
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat

@OptIn(ExperimentalMaterial3Api::class)
class ReaderActivity : ComponentActivity() {
    private val readerViewModel: ReaderViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OpenBooksTheme {
                val bookId = intent.getStringExtra("book_id") ?: return@setContent
                val book = readerViewModel.getBook(bookId)
                if (book != null) {
                    ReaderScreen(book = book)
                }
            }
        }
    }
}

@Composable
fun ReaderScreen(book: Book) {
    when (book.format) {
        BookFormat.EPUB -> EpubReaderScreen(book = book)
        BookFormat.PDF -> PdfReaderScreen(book = book)
        BookFormat.CBZ, BookFormat.CBR -> ComicsReaderScreen(book = book)
        else -> Text("Unsupported format: ${book.format}")
    }
}