package com.openbooks.data.repository

import com.openbooks.data.db.AppDatabase
import com.openbooks.data.db.ShelfDao
import com.openbooks.data.db.ShelfCollectionCrossRefDao
import com.openbooks.domain.model.Shelf
import com.openbooks.domain.repository.ShelfRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ShelfRepositoryImpl @Inject constructor(
    private val database: AppDatabase
) : ShelfRepository {
    private val shelfDao: ShelfDao = database.shelfDao()
    private val crossRefDao: ShelfCollectionCrossRefDao = database.shelfCollectionCrossRefDao()

    override suspend fun getAllShelves(): List<Shelf> = shelfDao.getAllSync()

    override suspend fun getShelfById(id: String): Shelf? = shelfDao.getByIdSync(id)

    override suspend fun insertShelf(shelf: Shelf) = shelfDao.insert(shelf)

    override suspend fun updateShelf(shelf: Shelf) = shelfDao.update(shelf)

    override suspend fun deleteShelf(id: String) = shelfDao.deleteById(id)

    override fun observeAllShelves(): Flow<List<Shelf>> = shelfDao.getAll()

    override fun observeShelfById(id: String): Flow<Shelf?> = shelfDao.getById(id)
}