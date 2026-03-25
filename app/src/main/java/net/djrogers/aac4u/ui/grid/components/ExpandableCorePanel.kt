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
import androidx.compose.ui.draw.shadow
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

    val groupedButtons = remember(coreButtons) {
        buildGroupedButtons(coreButtons, groups)
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            groups.forEachIndexed { index, group ->
                val isGroupExpanded = expandedGroupIndex == index
                val symbolButton = coreButtons.find {
                    it.label.equals(group.symbolWord, ignoreCase = true)
                }

                CoreFolderButton(
                    group = group,
                    symbolButton = symbolButton,
                    isExpanded = isGroupExpanded,
                    isEditMode = isEditMode,
                    highContrast = highContrast,
                    largeText = largeText,
                    onTap = {
                        if (!isEditMode) {
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
                val buttonsForGroup = groupedButtons[expandedGroupIndex] ?: emptyList()

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
                            items = buttonsForGroup,
                            key = { it.first.id }
                        ) { (button, groupColor) ->
                            CoreWordGridButton(
                                button = button,
                                groupColor = groupColor,
                                isEditMode = isEditMode,
                                highContrast = highContrast,
                                largeText = largeText,
                                onTap = {
                                    if (isEditMode) onButtonEdit(button)
                                    else {
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

private fun buildGroupedButtons(
    coreButtons: List<AACButton>,
    groups: List<CoreWordGroup>
): Map<Int, List<Pair<AACButton, Color>>> {
    val result = mutableMapOf<Int, MutableList<Pair<AACButton, Color>>>()
    val assignedIds = mutableSetOf<Long>()

    groups.forEachIndexed { index, group ->
        val buttonsForGroup = mutableListOf<Pair<AACButton, Color>>()
        for (word in group.words) {
            val button = coreButtons.find { it.label.equals(word, ignoreCase = true) }
            if (button != null) {
                buttonsForGroup.add(button to group.color)
                assignedIds.add(button.id)
            }
        }
        result[index] = buttonsForGroup
    }

    for (button in coreButtons) {
        if (button.id in assignedIds) continue
        val matchedGroup = CoreWordGroups.groupForButton(button.label, button.backgroundColor)
        if (matchedGroup != null) {
            val groupIndex = groups.indexOf(matchedGroup)
            if (groupIndex >= 0) {
                result.getOrPut(groupIndex) { mutableListOf() }.add(button to matchedGroup.color)
                assignedIds.add(button.id)
            }
        }
    }

    return result
}

@Composable
private fun CoreFolderButton(
    group: CoreWordGroup,
    symbolButton: AACButton?,
    isExpanded: Boolean,
    isEditMode: Boolean,
    highContrast: Boolean,
    largeText: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val hasImage = symbolButton?.imagePath != null

    val effectiveColor = if (highContrast) {
        group.color.copy(
            red = (group.color.red * 0.6f).coerceIn(0f, 1f),
            green = (group.color.green * 0.6f).coerceIn(0f, 1f),
            blue = (group.color.blue * 0.6f).coerceIn(0f, 1f)
        )
    } else {
        if (isExpanded) group.color else group.color.copy(alpha = 0.75f)
    }

    val textColor = if (highContrast) Color.White else Color(0xFF212121)
    val folderShape = remember { FolderShape(tabWidthFraction = 0.4f, tabHeightFraction = 0.14f, cornerRadius = 12f) }

    Box(
        modifier = modifier
            .aspectRatio(0.8f)
            .shadow(
                elevation = if (isExpanded) 4.dp else 1.dp,
                shape = folderShape,
                ambientColor = Color(0x40000000),
                spotColor = Color(0x30000000)
            )
            .clip(folderShape)
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onTap()
            }
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = effectiveColor,
            shape = folderShape
        ) {}

        // Content — extra top padding to clear the tab area, generous side padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 12.dp, start = 5.dp, end = 5.dp, bottom = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Symbol image — constrained to avoid clipping
            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(symbolButton?.imagePath)
                        .crossfade(false)
                        .build(),
                    contentDescription = group.label,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 2.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = group.symbolWord,
                        fontSize = if (largeText) 14.sp else 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Category label
            Text(
                text = group.label,
                fontSize = if (largeText) 10.sp else 8.sp,
                fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Expand indicator
        Text(
            text = if (isExpanded) "▲" else "▼",
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold,
            color = if (highContrast) Color.White.copy(alpha = 0.8f) else Color(0xFF546E7A),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(3.dp)
        )
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
            modifier = Modifier.fillMaxSize().padding(3.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasImage) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(button.imagePath).crossfade(false).build(),
                    contentDescription = null, contentScale = ContentScale.Fit,
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 2.dp, vertical = 1.dp)
                )
                Text(
                    text = button.label,
                    fontSize = if (largeText) 13.sp else 10.sp,
                    fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = textColor, textAlign = TextAlign.Center,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = button.label,
                    fontSize = if (largeText) 16.sp else 13.sp,
                    fontWeight = if (highContrast) FontWeight.ExtraBold else FontWeight.Bold,
                    color = textColor, textAlign = TextAlign.Center,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
