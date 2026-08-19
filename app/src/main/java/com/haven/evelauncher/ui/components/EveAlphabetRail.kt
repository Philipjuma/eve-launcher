package com.haven.evelauncher.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EveAlphabetRail(
    alphabet: List<Char>,
    onLetterSelected: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeLetter by remember { mutableStateOf<Char?>(null) }

    Box(
        modifier = modifier
            .width(44.dp)
            .fillMaxHeight()
            .padding(vertical = 80.dp)
            .pointerInput(alphabet) {
                detectTapGestures(
                    onPress = { offset ->
                        if (size.height > 0) {
                            val letterIndex = (offset.y / size.height * alphabet.size).toInt().coerceIn(0, alphabet.size - 1)
                            val letter = alphabet[letterIndex]
                            activeLetter = letter
                            onLetterSelected(letter)
                            tryAwaitRelease()
                            activeLetter = null
                        }
                    }
                )
            }
            .pointerInput(alphabet) {
                detectDragGestures(
                    onDragStart = { offset ->
                        if (size.height > 0) {
                            val letterIndex = (offset.y / size.height * alphabet.size).toInt().coerceIn(0, alphabet.size - 1)
                            activeLetter = alphabet[letterIndex]
                        }
                    },
                    onDrag = { change, _ ->
                        if (size.height > 0) {
                            val letterIndex = (change.position.y / size.height * alphabet.size).toInt().coerceIn(0, alphabet.size - 1)
                            val letter = alphabet[letterIndex]
                            if (activeLetter != letter) {
                                activeLetter = letter
                                onLetterSelected(letter)
                            }
                        }
                    },
                    onDragEnd = { activeLetter = null },
                    onDragCancel = { activeLetter = null }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { letter ->
                val isSelected = activeLetter == letter
                Text(
                    text = letter.toString(),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.4f),
                    fontSize = if (isSelected) 13.sp else 10.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall // Inherit Outfit
                )
            }
        }
    }
}
