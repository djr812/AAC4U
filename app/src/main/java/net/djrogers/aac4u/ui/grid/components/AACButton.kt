package net.djrogers.aac4u.ui.grid.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.djrogers.aac4u.domain.model.AACButton as AACButtonModel

/**
 * A single AAC communication button.
 *
 * Displays a symbol image with a text label below.
 * Designed for large touch targets and clear visual feedback.
 *
 * Accessibility:
 * - Full contentDescription for TalkBack
 * - Role.Button for switch access
 * - High contrast border support
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AACButton(
    button: AACButtonModel,
    showLabel: Boolean = true,
    onTap: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val backgroundColor by animateColorAsState(
        targetValue = if (isPressed) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            button.backgroundColor?.let { Color(android.graphics.Color.parseColor(it)) }
                ?: MaterialTheme.colorScheme.surfaceVariant
        },
        label = "buttonColor"
    )

    Surface(
        modifier = modifier
            .semantics {
                contentDescription = button.phrase
                role = Role.Button
            }
            .combinedClickable(
                onClick = {
                    isPressed = true
                    onTap()
                    // Reset press state after brief delay for visual feedback
                },
                onLongClick = onLongPress
            ),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Symbol Image ──
            if (button.imagePath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(button.imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = null, // Handled by semantics above
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(4.dp)
                )
            } else {
                // Placeholder — show the label text large when no image
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = button.label,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // ── Text Label ──
            if (showLabel && button.imagePath != null) {
                Text(
                    text = button.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp)
                )
            }
        }
    }
}
