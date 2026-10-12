package com.openbooks.feature.settings.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemHeadline
import androidx.compose.material3.ListItemSecondaryText
import androidx.compose.material3.ListItemTrailing
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import com.openbooks.feature.settings.ui.viewmodel.SettingsViewModel
import com.openbooks.feature.settings.ui.theme.OpenBooksTheme
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showFontMenu by remember { mutableStateOf(false) }
    var showThemeMenu by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings", fontWeight = FontWeight.Bold) },
            navigationIcon = { 
                androidx.compose.material3.IconButton(onClick = { /* Navigate back */ }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = OpenBooksTheme.colorScheme.surfaceContainer
            )
        )

        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reading Settings Section
            item {
                Text("Reading", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                SettingsRow(
                    title = "Font Size",
                    subtitle = "${(uiState.fontSize * 100).toInt()}%",
                    trailing = {
                        Slider(
                            value = uiState.fontSize,
                            onValueChange = { viewModel.updateFontSize(it) },
                            valueRange = 0.5f..3.0f,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Font Family",
                    subtitle = uiState.fontFamily,
                    trailing = {
                        androidx.compose.material3.Text(text = uiState.fontFamily)
                    },
                    onClick = { showFontMenu = true }
                )
            }
            
            item {
                SettingsRow(
                    title = "Theme",
                    subtitle = uiState.theme.name,
                    trailing = {
                        androidx.compose.material3.Text(text = uiState.theme.name)
                    },
                    onClick = { showThemeMenu = true }
                )
            }
            
            item {
                SettingsRow(
                    title = "Line Height",
                    subtitle = "${(uiState.lineHeight * 100).toInt()}%",
                    trailing = {
                        Slider(
                            value = uiState.lineHeight,
                            onValueChange = { viewModel.updateLineHeight(it) },
                            valueRange = 1.0f..3.0f,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Margins",
                    subtitle = "${(uiState.margin * 100).toInt()}%",
                    trailing = {
                        Slider(
                            value = uiState.margin,
                            onValueChange = { viewModel.updateMargin(it) },
                            valueRange = 0.0f..2.0f,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Keep Screen On",
                    trailing = {
                        Switch(
                            checked = uiState.keepScreenOn,
                            onCheckedChange = { viewModel.updateKeepScreenOn(it) }
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Page Turn Animation",
                    subtitle = uiState.pageTurnAnimation.name,
                    trailing = {
                        androidx.compose.material3.Text(text = uiState.pageTurnAnimation.name)
                    },
                    onClick = { /* Show animation picker */ }
                )
            }

            item { Divider() }
            
            // TTS Settings Section
            item {
                Text("Text-to-Speech", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                SettingsRow(
                    title = "Default Language",
                    subtitle = uiState.ttsLanguage,
                    trailing = { androidx.compose.material3.Text(uiState.ttsLanguage) }
                )
            }
            
            item {
                SettingsRow(
                    title = "Speech Rate",
                    subtitle = "${uiState.ttsSpeechRate}x",
                    trailing = {
                        Slider(
                            value = uiState.ttsSpeechRate,
                            onValueChange = { viewModel.updateTTSSpeechRate(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Pitch",
                    subtitle = "${uiState.ttsPitch}x",
                    trailing = {
                        Slider(
                            value = uiState.ttsPitch,
                            onValueChange = { viewModel.updateTTSPitch(it) },
                            valueRange = 0.5f..2.0f,
                            modifier = Modifier.width(120.dp)
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Auto-scroll while reading",
                    trailing = {
                        Switch(
                            checked = uiState.ttsAutoScroll,
                            onCheckedChange = { viewModel.updateTTSAutoScroll(it) }
                        )
                    }
                )
            }
            
            item { Divider() }
            
            // Sync Settings Section
            item {
                Text("Cloud Sync", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                SettingsRow(
                    title = "Enable Sync",
                    trailing = {
                        Switch(
                            checked = uiState.syncEnabled,
                            onCheckedChange = { viewModel.updateSyncEnabled(it) }
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Sync on Wi-Fi Only",
                    trailing = {
                        Switch(
                            checked = uiState.syncOnWifiOnly,
                            onCheckedChange = { viewModel.updateSyncOnWifiOnly(it) }
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Sync Interval",
                    subtitle = "${uiState.syncIntervalMinutes} minutes",
                    trailing = { androidx.compose.material3.Text("${uiState.syncIntervalMinutes} min") }
                )
            }
            
            item {
                SettingsRow(
                    title = "Sync Reading Positions",
                    trailing = {
                        Switch(
                            checked = uiState.syncPositions,
                            onCheckedChange = { viewModel.updateSyncPositions(it) }
                        )
                    }
                )
            }
            
            item {
                SettingsRow(
                    title = "Sync Bookmarks & Notes",
                    trailing = {
                        Switch(
                            checked = uiState.syncBookmarks,
                            onCheckedChange = { viewModel.updateSyncBookmarks(it) }
                        )
                    }
                )
            }
            
            item { Divider() }
            
            // Storage Section
            item {
                Text("Storage", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                SettingsRow(
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    trailing = { Icon(Icons.Filled.Storage, contentDescription = "Storage") },
                    onClick = { viewModel.clearCache() }
                )
            }
            
            item {
                SettingsRow(
                    title = "Manage Downloads",
                    subtitle = "View and delete downloaded books",
                    trailing = { Icon(Icons.Filled.Storage, contentDescription = "Downloads") }
                )
            }
            
            item { Divider() }
            
            // About Section
            item {
                Text("About", style = androidx.compose.material3.MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            
            item {
                SettingsRow(
                    title = "Version",
                    subtitle = "1.0.0",
                    trailing = { androidx.compose.material3.Text("1.0.0") }
                )
            }
            
            item {
                SettingsRow(
                    title = "Open Source Licenses",
                    trailing = { Icon(Icons.Filled.Info, contentDescription = "Licenses") }
                )
            }
            
            item {
                SettingsRow(
                    title = "Privacy Policy",
                    trailing = { Icon(Icons.Filled.Info, contentDescription = "Privacy") }
                )
            }
        }
    }
}

@Composable
fun SettingsRow(
    title: String,
    subtitle: String = "",
    trailing: @Composable () -> Unit,
    onClick: (() -> Unit)? = null
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    ListItem(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        headlineContent = { Text(text = title) },
        supportingContent = { if (subtitle.isNotBlank()) Text(text = subtitle, style = androidx.compose.material3.MaterialTheme.typography.bodySmall) },
        trailingContent = trailing,
        interactionSource = interactionSource
    )
}