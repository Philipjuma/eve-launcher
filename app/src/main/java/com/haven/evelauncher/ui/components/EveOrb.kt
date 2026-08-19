package com.haven.evelauncher.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EveOrb(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        EveGlassSurface(
            modifier = Modifier.size(40.dp),
            cornerRadius = 20.dp
        ) {
            // Inner core glow or refraction placeholder
        }
    }
}
