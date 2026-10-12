package com.openbooks.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.openbooks.domain.model.Shelf
import kotlinx.coroutines.flow.Flow

@Dao
interface ShelfDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shelf: Shelf)

    @Update
    suspend fun update(shelf: Shelf)

    @Delete
    suspend fun delete(shelf: Shelf)

    @Query("DELETE FROM shelves WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM shelves ORDER BY sortOrder")
    fun getAll(): Flow<List<Shelf>>

    @Query("SELECT * FROM shelves ORDER BY sortOrder")
    suspend fun getAllSync(): List<Shelf>

    @Query("SELECT * FROM shelves WHERE id = :id")
    fun getById(id: String): Flow<Shelf?>

    @Query("SELECT * FROM shelves WHERE id = :id")
    suspend fun getByIdSync(id: String): Shelf?

    @Query("SELECT * FROM shelves WHERE isDefault = 1")
    suspend fun getDefaultShelf(): Shelf?
}

@Dao
interface ShelfCollectionCrossRefDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addCollectionToShelf(shelfId: String, collectionId: String)

    @Delete
    suspend fun removeCollectionFromShelf(crossRef: ShelfCollectionCrossRef)

    @Query("DELETE FROM shelf_collections WHERE shelfId = :shelfId AND collectionId = :collectionId")
    suspend fun removeCollectionFromShelf(shelfId: String, collectionId: String)

    @Query("SELECT collectionId FROM shelf_collections WHERE shelfId = :shelfId")
    suspend fun getCollectionIdsForShelf(shelfId: String): List<String>

    @Query("SELECT shelfId FROM shelf_collections WHERE collectionId = :collectionId")
    suspend fun getShelfIdsForCollection(collectionId: String): List<String>
}

data class ShelfCollectionCrossRef(
    val shelfId: String,
    val collectionId: String
)