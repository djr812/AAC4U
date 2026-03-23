package net.djrogers.aac4u.ui.grid.components

import android.view.SoundEffectConstants
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import net.djrogers.aac4u.domain.model.AACButton

@Composable
fun ExpandableCorePanel(
    coreButtons: List<AACButton>,
    isEditMode: Boolean,
    isExpanded: Boolean,
    highContrast: Boolean = false,
    largeText: Boolean = false,
    reducedAnimations: Boolean = false,
    onToggleExpand: () -> Unit,
    onButtonTapped: (AACButton) -> Unit,
    onButtonEdit: (AACButton) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedGroupIndex by remember { mutableStateOf(-1) }
    val groups = CoreWordGroups.ALL_GROUPS

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            groups.forEachIndexed { index, group ->
                val triggerWord = group.words.first()
                val triggerButton = coreButtons.find {
                    it.label.equals(triggerWord, ignoreCase = true)
                }
                val isGroupExpanded = expandedGroupIndex == index

                CoreTriggerButton(
                    word = triggerWord,
                    button = triggerButton,
                    groupColor = group.color,
                    isExpanded = isGroupExpanded,
                    isEditMode = isEditMode,
                    highContrast = highContrast,
                    largeText = largeText,
                    onTap = {
                        if (isEditMode && triggerButton != null) {
                            onButtonEdit(triggerButton)
                        } else {
                            expandedGroupIndex = if (isGroupExpanded) -1 else index
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        AnimatedVisibility(
            visible = expandedGroupIndex >= 0,
            enter = if (reducedAnimations) EnterTransition.None
                    else expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = if (reducedAnimations) ExitTransition.None
                   else shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
        ) {
            if (expandedGroupIndex >= 0 && expandedGroupIndex < groups.size) {
                val group = groups[expandedGroupIndex]

                val groupButtons = group.words.mapNotNull { word ->
                    val button = coreButtons.find {
                        it.label.equals(word, ignoreCase = true)
                    }
                    if (button != null) button to group.color else null
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 3.dp, start = 2.dp, end = 2.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (highContrast) Color(0xFF212121) else Color(0xFFF5F5F5),
                    shadowElevation = 2.dp
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(10),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        items(
                            items = groupButtons,
                            key = { it.first.id }
                        ) { (button, groupColor) ->
                            CoreWordGridButton(
                                button = button,
                                groupColor = groupColor,
                                isEditMode = isEditMode,
                                highContrast = highContrast,
                                largeText = largeText,
                                onTap = {
                                    if (isEditMode) {
                                        onButtonEdit(button)
                                    } else {
                                        onButtonTapped(button)
                                        expandedGroupIndex = -1
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoreTriggerButton(
    word: String,
    button: AACButton?,
    groupColor: Color,
    isExpanded: Boolean,
    isEditMode: Boolean,
    highContrast: Boolean,
    largeText: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val hasImage = button?.imagePath != null

    val effectiveColor = if (highContrast) {
        groupColor.copy(
            red = (groupColor.red * 0.6f).coerceIn(0f, 1f),
            green = (groupColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (groupColor.blue * 0.6f).coerceIn(0f, 1f)
        )
    } else {
        if (isExpanded) groupColor else groupColor.copy(alpha = 0.7f)
    }

    val textColor = if (highContrast) Color.White else Color(0xFF212121)

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onTap()
            },
        color = effectiveColor,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = if (isExpanded) 3.dp else 1.dp,
        border = if (highContrast) BorderStroke(2.dp, Color.White) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (hasImage) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(button?.imagePath)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 1.dp)
                    )
                    Text(
                        text = word,
                        fontSize = if (largeText) 12.sp else 9.sp,
                        fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.SemiBold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = word,
                        fontSize = if (largeText) 15.sp else 12.sp,
                        fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = if (isExpanded) "▲" else "▼",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = if (highContrast) Color.White.copy(alpha = 0.8f) else Color(0xFF546E7A),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
            )
        }
    }
}

@Composable
private fun CoreWordGridButton(
    button: AACButton,
    groupColor: Color,
    isEditMode: Boolean,
    highContrast: Boolean,
    largeText: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val hasImage = button.imagePath != null

    val effectiveColor = if (highContrast) {
        groupColor.copy(
            red = (groupColor.red * 0.6f).coerceIn(0f, 1f),
            green = (groupColor.green * 0.6f).coerceIn(0f, 1f),
            blue = (groupColor.blue * 0.6f).coerceIn(0f, 1f)
        )
    } else groupColor

    val textColor = if (highContrast) Color.White else Color(0xFF212121)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onTap()
            },
        color = effectiveColor,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp,
        border = if (highContrast) BorderStroke(2.dp, Color.White) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(button.imagePath)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp, vertical = 1.dp)
                )
                Text(
                    text = button.label,
                    fontSize = if (largeText) 13.sp else 10.sp,
                    fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = button.label,
                    fontSize = if (largeText) 16.sp else 13.sp,
                    fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}