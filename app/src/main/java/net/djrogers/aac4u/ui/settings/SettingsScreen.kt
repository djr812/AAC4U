package net.djrogers.aac4u.ui.settings

import android.util.Log
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class SettingsTab(val label: String, val emoji: String) {
    VOICE("Voice", "🔊"),
    GRID("Grid", "📐"),
    ACCESSIBILITY("Access", "♿"),
    BACKUP("Backup", "💾")
}

private sealed class VoiceListItem {
    data class Header(val text: String, val color: Color) : VoiceListItem()
    data class VoiceEntry(
        val voiceName: String,
        val displayName: String,
        val isOffline: Boolean
    ) : VoiceListItem()
    data object DefaultEntry : VoiceListItem()
}

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDiscardDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(SettingsTab.VOICE) }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved TTS settings. Discard changes?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.discardChanges()
                    showDiscardDialog = false
                    onNavigateBack()
                }) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep Editing") }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                shadowElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (state.hasUnsavedChanges) showDiscardDialog = true
                            else onNavigateBack()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF616161)
                        )
                    }

                    SettingsTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTab = tab }
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                    else Color.Transparent
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = tab.emoji, fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = tab.label,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                SettingsTab.VOICE -> VoiceSettingsContent(state = state, viewModel = viewModel)
                SettingsTab.GRID -> StubContent("📐", "Grid Layout", "Column count, button size, label visibility, and grid spacing settings will be available here.")
                SettingsTab.ACCESSIBILITY -> StubContent("♿", "Accessibility", "High contrast mode, input method selection (tap, dwell, switch, scanning), and timing adjustments will be available here.")
                SettingsTab.BACKUP -> StubContent("💾", "Backup & Restore", "Export your complete configuration as a backup file, or restore from a previous backup.")
            }
        }

        AnimatedVisibility(
            visible = state.showSavedToast,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF43A047),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = "✓  Settings saved",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceSettingsContent(
    state: TtsSettingsState,
    viewModel: SettingsViewModel
) {
    if (!state.isTtsReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Text-to-speech engine is initialising...", fontSize = 14.sp, color = Color(0xFFFF8F00))
        }
        return
    }

    val voiceListItems = remember(state.offlineVoices, state.onlineOnlyVoices) {
        val list = mutableListOf<VoiceListItem>()

        list.add(VoiceListItem.DefaultEntry)

        if (state.offlineVoices.isNotEmpty()) {
            list.add(VoiceListItem.Header("Offline (${state.offlineVoices.size})", Color(0xFF43A047)))
            state.offlineVoices.forEach { voice ->
                list.add(VoiceListItem.VoiceEntry(voice.name, voice.displayName, true))
            }
        }

        if (state.onlineOnlyVoices.isNotEmpty()) {
            list.add(VoiceListItem.Header("Online Only (${state.onlineOnlyVoices.size})", Color(0xFFFF8F00)))
            state.onlineOnlyVoices.forEach { voice ->
                list.add(VoiceListItem.VoiceEntry(voice.name, voice.displayName, false))
            }
        }

        list.toList()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── LEFT: Voice List ──
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFFAFAFA)),
            contentPadding = PaddingValues(6.dp)
        ) {
            items(count = voiceListItems.size) { index ->
                when (val item = voiceListItems[index]) {
                    is VoiceListItem.DefaultEntry -> {
                        VoiceRow(
                            label = "System Default",
                            isOffline = true,
                            isSelected = state.previewVoiceName == null,
                            showBadge = false,
                            onClick = { viewModel.setPreviewVoice(null) }
                        )
                    }
                    is VoiceListItem.Header -> {
                        SectionLabel(item.text, item.color)
                    }
                    is VoiceListItem.VoiceEntry -> {
                        VoiceRow(
                            label = item.displayName,
                            isOffline = item.isOffline,
                            isSelected = state.previewVoiceName == item.voiceName,
                            showBadge = true,
                            onClick = { viewModel.setPreviewVoice(item.voiceName) }
                        )
                    }
                }
            }
        }

        // ── RIGHT: Controls ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Sliders Section ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SliderSetting(
                        label = "Speed",
                        value = state.previewRate,
                        valueLabel = "${state.previewRate}x",
                        min = 0.5f, max = 2.0f, steps = 15,
                        onValueChange = viewModel::setPreviewRate
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    SliderSetting(
                        label = "Pitch",
                        value = state.previewPitch,
                        valueLabel = "${state.previewPitch}x",
                        min = 0.5f, max = 2.0f, steps = 15,
                        onValueChange = viewModel::setPreviewPitch
                    )
                }
            }

            // ── Test / Save / Discard Section ──
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Test button
                    OutlinedButton(
                        onClick = if (state.isSpeaking) viewModel::stopTest else viewModel::testVoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (state.isSpeaking) "⏹ Stop" else "▶ Test Voice",
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Save / Discard row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.hasUnsavedChanges) {
                            OutlinedButton(
                                onClick = viewModel::discardChanges,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF5350)
                                )
                            ) {
                                Text("Discard", fontSize = 14.sp)
                            }
                        }

                        Button(
                            onClick = viewModel::saveSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            enabled = state.hasUnsavedChanges,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF43A047),
                                disabledContainerColor = Color(0xFFBDBDBD)
                            )
                        ) {
                            Text("Save", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StubContent(emoji: String, title: String, description: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description, fontSize = 15.sp, color = Color(0xFF757575),
                lineHeight = 22.sp, textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Coming soon", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun VoiceRow(
    label: String,
    isOffline: Boolean,
    isSelected: Boolean,
    showBadge: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color(0xFF42A5F5) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (isSelected) Color(0xFF42A5F5) else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = Color(0xFF2E7D32),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (showBadge) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOffline) "offline" else "online",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (isOffline) Color(0xFF43A047) else Color(0xFFFF8F00),
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isOffline) Color(0xFFE8F5E9) else Color(0xFFFFF3E0))
                    .padding(horizontal = 5.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: Color) {
    Text(
        text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color,
        modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp)
    )
}

@Composable
private fun SliderSetting(
    label: String, value: Float, valueLabel: String,
    min: Float, max: Float, steps: Int, onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF616161))
            Text(text = valueLabel, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value, onValueChange = onValueChange,
            valueRange = min..max, steps = steps, modifier = Modifier.fillMaxWidth()
        )
    }
}