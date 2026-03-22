package net.djrogers.aac4u.ui.grid.components

import android.view.SoundEffectConstants
import androidx.compose.animation.*
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
    onToggleExpand: () -> Unit,
    onButtonTapped: (AACButton) -> Unit,
    onButtonEdit: (AACButton) -> Unit,
    modifier: Modifier = Modifier
) {
    var expandedGroupIndex by remember { mutableStateOf(-1) }
    val groups = CoreWordGroups.ALL_GROUPS

    Column(modifier = modifier) {
        // ── Trigger Row: 10 buttons across ──
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

        // ── Expanded Group Grid (10 columns to match) ──
        AnimatedVisibility(
            visible = expandedGroupIndex >= 0,
            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
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
                    color = Color(0xFFF5F5F5),
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
                                onTap = {
                                    if (isEditMode) onButtonEdit(button)
                                    else onButtonTapped(button)
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
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val hasImage = button?.imagePath != null

    Surface(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                view.playSoundEffect(SoundEffectConstants.CLICK)
                onTap()
            },
        color = if (isExpanded) groupColor else groupColor.copy(alpha = 0.7f),
        shape = RoundedCornerShape(6.dp),
        shadowElevation = if (isExpanded) 3.dp else 1.dp
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
                            .crossfade(true)
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
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = word,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121),
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
                color = Color(0xFF546E7A),
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
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current
    val hasImage = button.imagePath != null

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
        color = groupColor,
        shape = RoundedCornerShape(6.dp),
        shadowElevation = 1.dp
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
                        .crossfade(true)
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
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = button.label,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}