package com.openbooks.data.repository

import com.openbooks.data.db.AppDatabase
import com.openbooks.data.db.CollectionDao
import com.openbooks.data.db.CollectionBookCrossRefDao
import com.openbooks.domain.model.Collection
import com.openbooks.domain.repository.CollectionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : CollectionRepository {
    private val collectionDao: CollectionDao = database.collectionDao()
    private val crossRefDao: CollectionBookCrossRefDao = database.collectionBookCrossRefDao()

    override suspend fun getAllCollections(): List<Collection> = collectionDao.getAllSync()

    override suspend fun getCollectionById(id: String): Collection? = collectionDao.getByIdSync(id)

    override suspend fun insertCollection(collection: Collection) = collectionDao.insert(collection)

    override suspend fun updateCollection(collection: Collection) = collectionDao.update(collection)

    override suspend fun deleteCollection(id: String) = collectionDao.deleteById(id)

    override suspend fun addBookToCollection(collectionId: String, bookId: String) = 
        crossRefDao.addBookToCollection(collectionId, bookId)

    override suspend fun removeBookFromCollection(collectionId: String, bookId: String) = 
        crossRefDao.removeBookFromCollection(collectionId, bookId)

    override fun observeAllCollections(): Flow<List<Collection>> = collectionDao.getAll()

    override fun observeCollectionById(id: String): Flow<Collection?> = collectionDao.getById(id)
}