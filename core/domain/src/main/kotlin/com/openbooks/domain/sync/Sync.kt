package com.openbooks.domain.sync

import com.openbooks.domain.model.Book
import com.openbooks.domain.model.Bookmark
import com.openbooks.domain.model.Collection
import com.openbooks.domain.model.Note
import com.openbooks.domain.model.ReadingPosition
import com.openbooks.domain.model.Shelf
import com.openbooks.domain.model.SyncConfig
import kotlinx.coroutines.flow.Flow

interface SyncService {
    val isSyncing: Boolean
    val lastSyncTime: Long?
    val syncStatus: SyncStatus
    
    suspend fun initialize()
    suspend fun signIn(email: String, password: String)
    suspend fun signInWithGoogle()
    suspend fun signOut()
    suspend fun syncAll()
    suspend fun syncBook(bookId: String)
    suspend fun syncPositions()
    suspend fun syncBookmarks()
    suspend fun syncNotes()
    suspend fun syncCollections()
    suspend fun syncShelves()
    suspend fun uploadBook(filePath: String, metadata: BookMetadata)
    suspend fun downloadBook(bookId: String): String
    suspend fun deleteRemoteBook(bookId: String)
    fun observeSyncStatus(): Flow<SyncStatus>
    fun observeSyncProgress(): Flow<SyncProgress>
}

interface SyncRepository {
    suspend fun getConfig(): SyncConfig
    suspend fun saveConfig(config: SyncConfig)
    suspend fun getPendingChanges(): List<SyncChange>
    suspend fun markSynced(changeIds: List<String>)
    suspend fun clearPendingChanges()
    fun observePendingChanges(): Flow<List<SyncChange>>
}

@Serializable
data class BookMetadata(
    val title: String,
    val authors: List<String>,
    val format: String,
    val fileSize: Long,
    val pageCount: Int,
    val coverHash: String?
)

@Serializable
enum class SyncStatus {
    IDLE, SYNCING, SUCCESS, ERROR, CONFLICT, OFFLINE
}

@Serializable
data class SyncProgress(
    val totalItems: Int,
    val completedItems: Int,
    val currentItem: String?,
    val bytesTransferred: Long,
    val totalBytes: Long
)

@Serializable
data class SyncChange(
    val id: String,
    val type: SyncChangeType,
    val entityType: SyncEntityType,
    val entityId: String,
    val data: String,
    val timestamp: Long,
    val retryCount: Int = 0
)

@Serializable
enum class SyncChangeType {
    CREATE, UPDATE, DELETE
}

@Serializable
enum class SyncEntityType {
    BOOK, POSITION, BOOKMARK, NOTE, COLLECTION, SHELF, SETTINGS
}

@Serializable
data class ConflictResolution(
    val localVersion: Any,
    val remoteVersion: Any,
    val resolution: ConflictResolutionStrategy
)

@Serializable
enum class ConflictResolutionStrategy {
    LOCAL_WINS, REMOTE_WINS, MERGE, MANUAL
}