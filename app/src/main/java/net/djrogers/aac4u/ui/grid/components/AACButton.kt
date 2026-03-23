package net.djrogers.aac4u.ui.grid.components

import android.view.SoundEffectConstants
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AACButton(
    button: AACButtonModel,
    showLabel: Boolean = true,
    categoryColor: Color = AACColors.forCategory(""),
    isEditMode: Boolean = false,
    highContrast: Boolean = false,
    largeText: Boolean = false,
    reducedAnimations: Boolean = false,
    onTap: () -> Unit,
    onLongPress: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    val scale by animateFloatAsState(
        targetValue = if (reducedAnimations) 1f else if (isPressed) 0.93f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "buttonScale"
    )

    val resolvedBgColor = remember(button.backgroundColor, categoryColor) {
        if (button.backgroundColor != null) {
            try { Color(android.graphics.Color.parseColor(button.backgroundColor)) }
            catch (e: Exception) { categoryColor }
        } else { categoryColor }
    }

    // High contrast: darken backgrounds, use white text
    val effectiveBgColor = if (highContrast) {
        resolvedBgColor.copy(
            red = (resolvedBgColor.red * 0.6f).coerceIn(0f, 1f),
            green = (resolvedBgColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (resolvedBgColor.blue * 0.6f).coerceIn(0f, 1f)
        )
    } else resolvedBgColor

    val textColor = if (highContrast) Color.White else remember(resolvedBgColor) { AACColors.textOn(resolvedBgColor) }
    val displayColor = if (isPressed && !reducedAnimations) AACColors.pressed(effectiveBgColor) else effectiveBgColor

    val borderColor = when {
        isEditMode -> Color(0xFFFF8F00)
        highContrast -> Color.White
        else -> Color(0x15000000)
    }
    val borderWidth = when {
        isEditMode -> 2.dp
        highContrast -> 3.dp
        else -> 1.dp
    }

    // Font sizes
    val labelFontSize = if (largeText) 16.sp else 12.sp
    val textOnlyFontSize = if (largeText) 20.sp else 16.sp
    val labelLineHeight = if (largeText) 18.sp else 14.sp
    val textOnlyLineHeight = if (largeText) 24.sp else 20.sp

    val hasImage = button.imagePath != null

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = if (isPressed && !reducedAnimations) 1.dp else 3.dp,
                shape = MaterialTheme.shapes.medium,
                ambientColor = Color(0x40000000),
                spotColor = Color(0x30000000)
            )
            .clip(MaterialTheme.shapes.medium)
            .background(displayColor)
            .border(width = borderWidth, color = borderColor, shape = MaterialTheme.shapes.medium)
            .semantics {
                contentDescription = button.phrase
                role = Role.Button
            }
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                    onTap()
                },
                onLongClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                }
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        if (hasImage) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(button.imagePath)
                        .crossfade(false)
                        .memoryCacheKey(button.imagePath)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )

                if (showLabel) {
                    Text(
                        text = button.label,
                        color = textColor,
                        fontSize = labelFontSize,
                        fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = labelLineHeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    )
                }
            }
        } else {
            Text(
                text = button.label,
                color = textColor,
                fontSize = textOnlyFontSize,
                fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = textOnlyLineHeight,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )
        }

        if (isEditMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCCFF8F00)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✏", fontSize = 11.sp)
            }
        }
    }
}
