package net.djrogers.aac4u.ui.grid.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.djrogers.aac4u.domain.model.AACButton as AACButtonModel
import net.djrogers.aac4u.ui.theme.AACColors

/**
 * A single AAC communication button with category-coloured background.
 *
 * Visual design:
 * - Soft pastel background colour based on category
 * - Rounded corners with subtle shadow
 * - Press animation (slight scale down + colour darken)
 * - Large readable text centred in the button
 * - Optional symbol image above the text label
 *
 * Accessibility:
 * - Full contentDescription for TalkBack
 * - Role.Button for switch access
 * - Minimum 48dp touch target (enforced by grid sizing)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AACButton(
    button: AACButtonModel,
    showLabel: Boolean = true,
    categoryColor: Color = AACColors.forCategory(""),
    onTap: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Animate scale on press for tactile feedback
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "buttonScale"
    )

    // Determine background colour
    val resolvedBgColor = if (button.backgroundColor != null) {
        try {
            Color(android.graphics.Color.parseColor(button.backgroundColor))
        } catch (e: Exception) {
            categoryColor
        }
    } else {
        categoryColor
    }

    val displayColor = if (isPressed) AACColors.pressed(resolvedBgColor) else resolvedBgColor
    val textColor = AACColors.textOn(resolvedBgColor)

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed) 1.dp else 3.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color(0x40000000),
                spotColor = Color(0x30000000)
            )
            .clip(MaterialTheme.shapes.medium)
            .background(displayColor)
            .border(
                width = 1.dp,
                color = Color(0x15000000), // Very subtle border
                shape = MaterialTheme.shapes.medium
            )
            .semantics {
                contentDescription = button.phrase
                role = Role.Button
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null, // We handle visual feedback via scale + colour
                onClick = onTap,
                onLongClick = onLongPress
            )
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Symbol Image (when available) ──
            if (button.imagePath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(button.imagePath)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(4.dp)
                )

                // Label below image
                if (showLabel) {
                    Text(
                        text = button.label,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                    )
                }
            } else {
                // ── Text-only button (no image) ──
                // Show the label large and centred
                Text(
                    text = button.label,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                )
            }
        }
    }
}
