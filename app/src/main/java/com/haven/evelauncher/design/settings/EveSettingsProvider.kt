package com.haven.evelauncher.design.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import com.haven.evelauncher.data.repository.EveSettings
import com.haven.evelauncher.ui.HomeViewModel

val LocalEveSettings = staticCompositionLocalOf { EveSettings() }

@Composable
fun EveSettingsProvider(
    viewModel: HomeViewModel,
    content: @Composable () -> Unit
) {
    val settings by viewModel.settings.collectAsState()
    CompositionLocalProvider(LocalEveSettings provides settings) {
        content()
    }
}
