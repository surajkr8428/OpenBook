package com.openbooks.app

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.feature.reader.ui.reader.EpubReaderScreen
import com.openbooks.feature.reader.ui.reader.PdfReaderScreen
import com.openbooks.feature.reader.ui.reader.ComicsReaderScreen
import com.openbooks.feature.reader.ui.viewmodel.ReaderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    bookId: String,
    viewModel: ReaderViewModel = viewModel()
) {
    val book = viewModel.getBook(bookId)
    book?.let { book ->
        when (book.format) {
            BookFormat.EPUB -> EpubReaderScreen(book = book, viewModel = viewModel)
            BookFormat.PDF -> PdfReaderScreen(book = book, viewModel = viewModel)
            BookFormat.CBZ, BookFormat.CBR -> ComicsReaderScreen(book = book, viewModel = viewModel)
            else -> androidx.compose.material3.Text("Unsupported format: ${book.format}")
        }
    }
}