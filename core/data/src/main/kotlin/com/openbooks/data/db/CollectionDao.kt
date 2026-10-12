package com.openbooks.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.openbooks.domain.model.Collection
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: Collection)

    @Update
    suspend fun update(collection: Collection)

    @Delete
    suspend fun delete(collection: Collection)

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM collections ORDER BY dateCreated DESC")
    fun getAll(): Flow<List<Collection>>

    @Query("SELECT * FROM collections ORDER BY dateCreated DESC")
    suspend fun getAllSync(): List<Collection>

    @Query("SELECT * FROM collections WHERE id = :id")
    fun getById(id: String): Flow<Collection?>

    @Query("SELECT * FROM collections WHERE id = :id")
    suspend fun getByIdSync(id: String): Collection?

    @Query("SELECT * FROM collections WHERE isSystem = 1")
    suspend fun getSystemCollections(): List<Collection>
}

@Dao
interface CollectionBookCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookToCollection(collectionId: String, bookId: String)

    @Delete
    suspend fun removeBookFromCollection(crossRef: CollectionBookCrossRef)

    @Query("DELETE FROM collection_books WHERE collectionId = :collectionId AND bookId = :bookId")
    suspend fun removeBookFromCollection(collectionId: String, bookId: String)

    @Query("SELECT bookId FROM collection_books WHERE collectionId = :collectionId")
    suspend fun getBookIdsForCollection(collectionId: String): List<String>

    @Query("SELECT collectionId FROM collection_books WHERE bookId = :bookId")
    suspend fun getCollectionIdsForBook(bookId: String): List<String>
}

data class CollectionBookCrossRef(
    val collectionId: String,
    val bookId: String
)