package com.openbooks.app.di

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.openbooks.core.data.db.AppDatabase
import com.openbooks.core.data.repository.BookRepositoryImpl
import com.openbooks.core.data.repository.BookmarkRepositoryImpl
import com.openbooks.core.data.repository.CollectionRepositoryImpl
import com.openbooks.core.data.repository.NoteRepositoryImpl
import com.openbooks.core.data.repository.ReadingPositionRepositoryImpl
import com.openbooks.core.data.repository.ReadingSessionRepositoryImpl
import com.openbooks.core.data.repository.ReaderSettingsRepositoryImpl
import com.openbooks.core.data.repository.ShelfRepositoryImpl
import com.openbooks.core.domain.parser.BookParser
import com.openbooks.core.domain.repository.BookRepository
import com.openbooks.core.domain.repository.BookmarkRepository
import com.openbooks.core.domain.repository.CollectionRepository
import com.openbooks.core.domain.repository.NoteRepository
import com.openbooks.core.domain.repository.ReadingPositionRepository
import com.openbooks.core.domain.repository.ReadingSessionRepository
import com.openbooks.core.domain.repository.ReaderSettingsRepository
import com.openbooks.core.domain.repository.ShelfRepository
import com.openbooks.core.domain.sync.SyncRepository
import com.openbooks.core.domain.sync.SyncService
import com.openbooks.core.domain.tts.TTSEngine
import com.openbooks.core.domain.tts.TTSRepository
import com.openbooks.core.domain.usecase.*
import com.openbooks.core.parser.epub.EpubParserImpl
import com.openbooks.core.parser.pdf.PdfParserImpl
import com.openbooks.core.parser.comics.ComicsParserImpl
import com.openbooks.core.sync.FirebaseSyncRepository
import com.openbooks.core.sync.FirebaseSyncService
import com.openbooks.core.tts.AndroidTTSEngine
import com.openbooks.core.tts.TTSRepositoryImpl
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import dagger.hilt.components.ViewModelComponent
import dagger.hilt.InstallIn
import dagger.hilt.Module
import dagger.hilt.Provides
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.scopes.ApplicationScoped
import dagger.hilt.android.scopes.FragmentScoped
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.Companion.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
}

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideBookRepository(impl: BookRepositoryImpl): BookRepository = impl

    @Provides
    @Singleton
    fun provideCollectionRepository(impl: CollectionRepositoryImpl): CollectionRepository = impl

    @Provides
    @Singleton
    fun provideShelfRepository(impl: ShelfRepositoryImpl): ShelfRepository = impl

    @Provides
    @Singleton
    fun provideReadingPositionRepository(impl: ReadingPositionRepositoryImpl): ReadingPositionRepository = impl

    @Provides
    @Singleton
    fun provideBookmarkRepository(impl: BookmarkRepositoryImpl): BookmarkRepository = impl

    @Provides
    @Singleton
    fun provideNoteRepository(impl: NoteRepositoryImpl): NoteRepository = impl

    @Provides
    @Singleton
    fun provideReadingSessionRepository(impl: ReadingSessionRepositoryImpl): ReadingSessionRepository = impl

    @Provides
    @Singleton
    fun provideReaderSettingsRepository(impl: ReaderSettingsRepositoryImpl): ReaderSettingsRepository = impl
}

@Module
@InstallIn(SingletonComponent::class)
object ParserModule {

    @Provides
    @Singleton
    fun provideEpubParser(): BookParser = EpubParserImpl()

    @Provides
    @Singleton
    fun providePdfParser(): BookParser = PdfParserImpl()

    @Provides
    @Singleton
    fun provideComicsParser(): BookParser = ComicsParserImpl()
}

@Module
@InstallIn(SingletonComponent::class)
object TTSModule {

    @Provides
    @Singleton
    fun provideTTSEngine(@ApplicationContext context: Context): TTSEngine = AndroidTTSEngine(context)

    @Provides
    @Singleton
    fun provideTTSRepository(@ApplicationContext context: Context): TTSRepository = TTSRepositoryImpl(context)
}

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncService(
        @ApplicationContext context: Context,
        syncRepository: SyncRepository
    ): SyncService = FirebaseSyncService(context, kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO), syncRepository)

    @Provides
    @Singleton
    fun provideSyncRepository(@ApplicationContext context: Context): SyncRepository = FirebaseSyncRepository(context)
}

@Module
@InstallIn(ViewModelComponent::class)
object UseCaseModule {

    @Provides
    @ViewModelScoped
    fun provideGetAllBooksUseCase(repo: BookRepository): GetAllBooksUseCase = GetAllBooksUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSearchBooksUseCase(repo: BookRepository): SearchBooksUseCase = SearchBooksUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetBookByIdUseCase(repo: BookRepository): GetBookByIdUseCase = GetBookByIdUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideAddBookUseCase(repo: BookRepository, parser: BookParser): AddBookUseCase = AddBookUseCase(repo, parser)

    @Provides
    @ViewModelScoped
    fun provideUpdateBookUseCase(repo: BookRepository): UpdateBookUseCase = UpdateBookUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideDeleteBookUseCase(repo: BookRepository): DeleteBookUseCase = DeleteBookUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetRecentBooksUseCase(repo: BookRepository): GetRecentBooksUseCase = GetRecentBooksUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetFavoriteBooksUseCase(repo: BookRepository): GetFavoriteBooksUseCase = GetFavoriteBooksUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetReadingPositionUseCase(repo: ReadingPositionRepository): GetReadingPositionUseCase = GetReadingPositionUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSaveReadingPositionUseCase(repo: ReadingPositionRepository): SaveReadingPositionUseCase = SaveReadingPositionUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetBookmarksUseCase(repo: BookmarkRepository): GetBookmarksUseCase = GetBookmarksUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideAddBookmarkUseCase(repo: BookmarkRepository): AddBookmarkUseCase = AddBookmarkUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideRemoveBookmarkUseCase(repo: BookmarkRepository): RemoveBookmarkUseCase = RemoveBookmarkUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetNotesUseCase(repo: NoteRepository): GetNotesUseCase = GetNotesUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideAddNoteUseCase(repo: NoteRepository): AddNoteUseCase = AddNoteUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetCollectionsUseCase(repo: CollectionRepository): GetCollectionsUseCase = GetCollectionsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideCreateCollectionUseCase(repo: CollectionRepository): CreateCollectionUseCase = CreateCollectionUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideAddBookToCollectionUseCase(repo: CollectionRepository): AddBookToCollectionUseCase = AddBookToCollectionUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetShelvesUseCase(repo: ShelfRepository): GetShelvesUseCase = GetShelvesUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideGetReaderSettingsUseCase(repo: ReaderSettingsRepository): GetReaderSettingsUseCase = GetReaderSettingsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSaveReaderSettingsUseCase(repo: ReaderSettingsRepository): SaveReaderSettingsUseCase = SaveReaderSettingsUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideRecordReadingSessionUseCase(repo: ReadingSessionRepository): RecordReadingSessionUseCase = RecordReadingSessionUseCase(repo)

    @Provides
    @ViewModelScoped
    fun provideSearchInBookUseCase(
        repo: BookRepository,
        epubParser: BookParser,
        pdfParser: BookParser,
        comicsParser: BookParser
    ): SearchInBookUseCase = SearchInBookUseCase(repo, epubParser, pdfParser, comicsParser)
}