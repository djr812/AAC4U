package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Displays the sentence being built from button taps.
 * Shows each word as a chip, with speak/backspace/clear actions.
 *
 * ┌──────────────────────────────────────────┐
 * │ [I] [want] [more] [juice]  [⌫] [✕] [▶] │
 * └──────────────────────────────────────────┘
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
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .padding(horizontal = 8.dp, vertical = 4.dp),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                } else {
                    items(sentenceParts) { part ->
                        SuggestionChip(
                            onClick = { /* Could allow removing individual words */ },
                            label = {
                                Text(
                                    text = part,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
            }

            // ── Action buttons ──
            if (sentenceParts.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))

                // Backspace
                IconButton(
                    onClick = onBackspace,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("⌫", style = MaterialTheme.typography.titleLarge)
                }

                // Clear all
                IconButton(
                    onClick = onClear,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text("✕", style = MaterialTheme.typography.titleLarge)
                }
            }

            // Speak / Stop button
            FilledIconButton(
                onClick = if (isSpeaking) onStop else onSpeak,
                modifier = Modifier.size(56.dp),
                enabled = sentenceParts.isNotEmpty() || isSpeaking
            ) {
                Text(
                    text = if (isSpeaking) "■" else "▶",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}
