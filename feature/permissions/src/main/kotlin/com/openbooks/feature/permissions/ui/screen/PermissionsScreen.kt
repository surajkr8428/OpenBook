package com.openbooks.feature.permissions.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import com.openbooks.feature.permissions.ui.viewmodel.PermissionsViewModel
import com.openbooks.app.ui.theme.OpenBooksTheme

@Composable
fun PermissionsScreen(viewModel: PermissionsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Permissions Required",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
            }
            
            item {
                Text(
                    text = "OpenBooks needs these permissions to provide the best reading experience.",
                    fontSize = 16.sp,
                    color = OpenBooksTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.TextAlign.Center
                )
            }
            
            items(uiState.permissions) { permission ->
                PermissionRow(
                    permission = permission,
                    onRequest = { viewModel.requestPermission(permission.type) }
                )
            }
            
            item {
                androidx.compose.foundation.layout.Spacer(Modifier.height(24.dp))
                
                if (uiState.allGranted) {
                    Button(
                        onClick = { viewModel.onAllGranted() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = OpenBooksTheme.colorScheme.primary
                        )
                    ) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Button(
                        onClick = { viewModel.requestAllPermissions() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = OpenBooksTheme.colorScheme.primary
                        )
                    ) {
                        Text("Grant All Permissions", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                    
                    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
                    
                    Button(
                        onClick = { viewModel.skipPermissions() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text("Skip for Now", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionRow(
    permission: PermissionItem,
    onRequest: () -> Unit
) {
    val isGranted = permission.isGranted
    
    androidx.compose.material3.Card(
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = if (isGranted) 
                OpenBooksTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else OpenBooksTheme.colorScheme.surfaceContainerHighest
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = permission.icon,
                contentDescription = permission.title,
                tint = if (isGranted) OpenBooksTheme.colorScheme.primary else OpenBooksTheme.colorScheme.onSurfaceVariant
            )
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = permission.title,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = permission.description,
                    fontSize = 12.sp,
                    color = OpenBooksTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (isGranted) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Granted",
                    tint = OpenBooksTheme.colorScheme.primary
                )
            } else {
                Button(onClick = onRequest) {
                    Text("Grant")
                }
            }
        }
    }
}

data class PermissionItem(
    val type: PermissionType,
    val title: String,
    val description: String,
    val icon: androidx.compose.material.icons.filled.ImageVector,
    val isGranted: Boolean
)

enum class PermissionType {
    STORAGE_READ,
    STORAGE_WRITE,
    MEDIA_IMAGES,
    MEDIA_AUDIO,
    NOTIFICATIONS,
    FOREGROUND_SERVICE
}