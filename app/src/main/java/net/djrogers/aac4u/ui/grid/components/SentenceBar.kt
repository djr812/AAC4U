package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.djrogers.aac4u.domain.model.AACButton

@Composable
fun SentenceBar(
    sentenceParts: List<String>,
    predictedWords: List<AACButton> = emptyList(),
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onStop: () -> Unit,
    onPredictionTapped: (AACButton) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Auto-scroll to end when sentence changes
    LaunchedEffect(sentenceParts.size, predictedWords.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Surface(
        modifier = modifier
            .height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFE3F2FD),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sentence content + predictions
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Confirmed words
                sentenceParts.forEach { part ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF90CAF9),
                        shadowElevation = 0.5.dp
                    ) {
                        Text(
                            text = part,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0D47A1),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Inline predictions (greyed, italic)
                if (sentenceParts.isNotEmpty() && predictedWords.isNotEmpty()) {
                    predictedWords.take(3).forEach { prediction ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFE0E0E0).copy(alpha = 0.6f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { onPredictionTapped(prediction) }
                        ) {
                            Text(
                                text = prediction.label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF757575),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Placeholder when empty
                if (sentenceParts.isEmpty()) {
                    Text(
                        text = "Tap words to build a sentence...",
                        fontSize = 14.sp,
                        color = Color(0xFF90A4AE),
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Action buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sentenceParts.isNotEmpty()) {
                    // Backspace
                    IconButton(
                        onClick = onBackspace,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("⌫", fontSize = 18.sp)
                    }

                    // Clear
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("✕", fontSize = 16.sp, color = Color(0xFFEF5350))
                    }
                }

                // Speak / Stop
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            if (isSpeaking) onStop() else onSpeak()
                        },
                    color = if (isSpeaking) Color(0xFFEF5350) else Color(0xFF43A047),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isSpeaking) "⏹" else "▶",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
