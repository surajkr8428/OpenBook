package com.openbooks.feature.permissions.ui.viewmodel

import android.app.Activity
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor() : ViewModel() {
    
    private var launcher: ((Array<String>) -> Unit)? = null
    
    private val _uiState = MutableStateFlow(PermissionsUiState(
        permissions = getDefaultPermissions()
    ))
    
    val uiState = _uiState.asStateFlow()
    
    val allGranted: Boolean
        get() = _uiState.value.permissions.all { it.isGranted }
    
    fun setLauncher(launcher: (Array<String>) -> Unit) {
        this.launcher = launcher
    }
    
    fun requestPermission(type: PermissionType) {
        val permission = type.toManifestPermission()
        launcher?.invoke(arrayOf(permission))
    }
    
    fun requestAllPermissions() {
        val permissions = _uiState.value.permissions
            .filter { !it.isGranted }
            .map { it.type.toManifestPermission() }
            .toTypedArray()
        
        if (permissions.isNotEmpty()) {
            launcher?.invoke(permissions)
        }
    }
    
    fun onPermissionsResult(results: Map<String, Boolean>) {
        _uiState.update { state ->
            val updated = state.permissions.map { permission ->
                val manifestPerm = permission.type.toManifestPermission()
                if (results.containsKey(manifestPerm)) {
                    permission.copy(isGranted = results[manifestPerm]!!)
                } else {
                    permission
                }
            }
            state.copy(permissions = updated)
        }
    }
    
    fun skipPermissions() {
        // Navigate to main app without all permissions
    }
    
    fun onAllGranted() {
        // Navigate to main app
    }
    
    private fun getDefaultPermissions(): List<PermissionItem> {
        return listOf(
            PermissionItem(
                type = PermissionType.STORAGE_READ,
                title = "Read Files",
                description = "Access your books and documents",
                icon = androidx.compose.material.icons.filled.FolderOpen,
                isGranted = checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            ),
            PermissionItem(
                type = PermissionType.STORAGE_WRITE,
                title = "Write Files",
                description = "Save books, covers, and annotations",
                icon = androidx.compose.material.icons.filled.Storage,
                isGranted = checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ),
            PermissionItem(
                type = PermissionType.MEDIA_IMAGES,
                title = "Media Access",
                description = "Read book covers and comics images",
                icon = androidx.compose.material.icons.filled.FolderOpen,
                isGranted = checkPermission(Manifest.permission.READ_MEDIA_IMAGES)
            ),
            PermissionItem(
                type = PermissionType.MEDIA_AUDIO,
                title = "Audio Access",
                description = "Play audiobooks and TTS audio",
                icon = androidx.compose.material.icons.filled.Mic,
                isGranted = checkPermission(Manifest.permission.READ_MEDIA_AUDIO)
            ),
            PermissionItem(
                type = PermissionType.NOTIFICATIONS,
                title = "Notifications",
                description = "Reading reminders and sync updates",
                icon = androidx.compose.material.icons.filled.CloudSync,
                isGranted = checkPermission(Manifest.permission.POST_NOTIFICATIONS)
            ),
            PermissionItem(
                type = PermissionType.FOREGROUND_SERVICE,
                title = "Background Playback",
                description = "Continue TTS when app is backgrounded",
                icon = androidx.compose.material.icons.filled.Mic,
                isGranted = checkPermission(Manifest.permission.FOREGROUND_SERVICE)
            )
        )
    }
    
    private fun checkPermission(permission: String): Boolean {
        // This would check actual permission state
        // For now, return false to show all permissions
        return false
    }
}

data class PermissionsUiState(
    val permissions: List<PermissionItem>
)

import android.Manifest

enum class PermissionType {
    STORAGE_READ,
    STORAGE_WRITE,
    MEDIA_IMAGES,
    MEDIA_AUDIO,
    NOTIFICATIONS,
    FOREGROUND_SERVICE
}

fun PermissionType.toManifestPermission(): String = when (this) {
    STORAGE_READ -> Manifest.permission.READ_EXTERNAL_STORAGE
    STORAGE_WRITE -> Manifest.permission.WRITE_EXTERNAL_STORAGE
    MEDIA_IMAGES -> Manifest.permission.READ_MEDIA_IMAGES
    MEDIA_AUDIO -> Manifest.permission.READ_MEDIA_AUDIO
    NOTIFICATIONS -> Manifest.permission.POST_NOTIFICATIONS
    FOREGROUND_SERVICE -> Manifest.permission.FOREGROUND_SERVICE
}