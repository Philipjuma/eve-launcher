package com.haven.evelauncher.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.haven.evelauncher.ui.theme.EveTypography

@Composable
fun EveGreeting(
    greeting: String,
    modifier: Modifier = Modifier,
    color: Color = Color.White
) {
    Text(
        text = greeting,
        style = EveTypography.Greeting,
        color = color.copy(alpha = 0.8f),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 24.dp)
    )
}
