package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Displays the sentence being built from button taps.
 * Shows each word as a styled chip, with speak/backspace/clear actions.
 *
 * ┌──────────────────────────────────────────────────┐
 * │  I   want   more   juice       [⌫] [✕]  [▶]    │
 * └──────────────────────────────────────────────────┘
 */
@Composable
fun SentenceBar(
    sentenceParts: List<String>,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 2.dp,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Sentence words ──
            LazyRow(
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        contentDescription = sentenceParts.joinToString(" ")
                    },
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (sentenceParts.isEmpty()) {
                    item {
                        Text(
                            text = "Tap buttons to build a sentence",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF9E9E9E),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    items(sentenceParts) { part ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE3F2FD)) // Light blue chip
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = part,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF37474F),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // ── Action buttons ──
            if (sentenceParts.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))

                // Backspace
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        "⌫",
                        fontSize = 22.sp,
                        color = Color(0xFF757575)
                    )
                }

                // Clear all
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        "✕",
                        fontSize = 20.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Speak / Stop button — large, prominent, always visible
            Button(
                onClick = if (isSpeaking) onStop else onSpeak,
                modifier = Modifier.size(52.dp),
                enabled = sentenceParts.isNotEmpty() || isSpeaking,
                shape = CircleShape,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSpeaking) Color(0xFFEF5350) else Color(0xFF43A047),
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFBDBDBD),
                    disabledContentColor = Color.White
                )
            ) {
                Text(
                    text = if (isSpeaking) "■" else "▶",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
