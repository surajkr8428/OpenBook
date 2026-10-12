package com.openbooks.feature.settings.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.openbooks.feature.settings.ui.screen.SettingsScreen
import com.openbooks.feature.settings.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    SettingsScreen(viewModel = viewModel)
}