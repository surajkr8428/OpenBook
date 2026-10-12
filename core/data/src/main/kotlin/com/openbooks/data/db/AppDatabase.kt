package com.openbooks.core.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.openbooks.domain.model.Book
import com.openbooks.domain.model.BookFormat
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Collection
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingPosition
import com.openbooks.domain.model.ReadingSession
import com.openbooks.domain.model.ReaderSettings
import com.openbooks.domain.model.Shelf
import com.openbooks.data.db.converters.Converters

@Database(
    entities = [
        Book::class,
        Collection::class,
        Shelf::class,
        ReadingPosition::class,
        Bookmark::class,
        Note::class,
        ReadingSession::class,
        ReaderSettings::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun collectionDao(): CollectionDao
    abstract fun shelfDao(): ShelfDao
    abstract fun readingPositionDao(): ReadingPositionDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun noteDao(): NoteDao
    abstract fun readingSessionDao(): ReadingSessionDao
    abstract fun readerSettingsDao(): ReaderSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "openbooks.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}