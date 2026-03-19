package net.djrogers.aac4u.ui.settings

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

private enum class SettingsTab(val label: String, val emoji: String) {
    VOICE("Voice", "🔊"),
    GRID("Grid", "📐"),
    ACCESSIBILITY("Accessibility", "♿"),
    BACKUP("Backup", "💾")
}

@OptIn(ExperimentalMaterial3Api::class)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings — ${selectedTab.label}")
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.hasUnsavedChanges) showDiscardDialog = true
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // ── Tabs ──
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = {},
                    divider = { HorizontalDivider(color = Color(0xFFE0E0E0)) }
                ) {
                    SettingsTab.entries.forEach { tab ->
                        val isSelected = tab == selectedTab
                        Tab(
                            selected = isSelected,
                            onClick = { selectedTab = tab }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 4.dp, vertical = 10.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(text = tab.emoji, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else Color(0xFF757575)
                                )
                            }
                        }
                    }
                }

                // ── Tab Content ──
                when (selectedTab) {
                    SettingsTab.VOICE -> VoiceSettingsContent(
                        state = state,
                        viewModel = viewModel
                    )
                    SettingsTab.GRID -> StubContent(
                        emoji = "📐",
                        title = "Grid Layout",
                        description = "Column count, button size, label visibility, and grid spacing settings will be available here."
                    )
                    SettingsTab.ACCESSIBILITY -> StubContent(
                        emoji = "♿",
                        title = "Accessibility",
                        description = "High contrast mode, input method selection (tap, dwell, switch, scanning), and timing adjustments will be available here."
                    )
                    SettingsTab.BACKUP -> StubContent(
                        emoji = "💾",
                        title = "Backup & Restore",
                        description = "Export your complete configuration as a backup file, or restore from a previous backup."
                    )
                }
            }

            // ── Toast ──
            AnimatedVisibility(
                visible = state.showSavedToast,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
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
}

@Composable
private fun VoiceSettingsContent(
    state: TtsSettingsState,
    viewModel: SettingsViewModel
) {
    if (!state.isTtsReady) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Text-to-speech engine is initialising...", fontSize = 14.sp, color = Color(0xFFFF8F00))
        }
        return
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
            contentPadding = PaddingValues(6.dp),
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            item(key = "__default__") {
                VoiceRow(
                    friendlyName = "System Default",
                    isOffline = true,
                    isSelected = state.previewVoiceName == null,
                    showBadge = false,
                    onClick = { viewModel.setPreviewVoice(null) }
                )
            }

            if (state.offlineVoices.isNotEmpty()) {
                item {
                    SectionLabel(
                        text = "Offline (${state.offlineVoices.size})",
                        color = Color(0xFF43A047)
                    )
                }
                items(items = state.offlineVoices, key = { it.name }) { voice ->
                    VoiceRow(
                        friendlyName = voice.friendlyName,
                        isOffline = true,
                        isSelected = state.previewVoiceName == voice.name,
                        showBadge = true,
                        onClick = { viewModel.setPreviewVoice(voice.name) }
                    )
                }
            }

            if (state.onlineOnlyVoices.isNotEmpty()) {
                item {
                    SectionLabel(
                        text = "Online Only (${state.onlineOnlyVoices.size})",
                        color = Color(0xFFFF8F00)
                    )
                }
                items(items = state.onlineOnlyVoices, key = { it.name }) { voice ->
                    VoiceRow(
                        friendlyName = voice.friendlyName,
                        isOffline = false,
                        isSelected = state.previewVoiceName == voice.name,
                        showBadge = true,
                        onClick = { viewModel.setPreviewVoice(voice.name) }
                    )
                }
            }
        }

        // ── RIGHT: Controls ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Speed
                    SliderSetting(
                        label = "Speed",
                        value = state.previewRate,
                        valueLabel = "${state.previewRate}x",
                        min = 0.5f,
                        max = 2.0f,
                        steps = 15,
                        onValueChange = viewModel::setPreviewRate
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Pitch
                    SliderSetting(
                        label = "Pitch",
                        value = state.previewPitch,
                        valueLabel = "${state.previewPitch}x",
                        min = 0.5f,
                        max = 2.0f,
                        steps = 15,
                        onValueChange = viewModel::setPreviewPitch
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Test
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

                    Spacer(modifier = Modifier.height(12.dp))

                    // Save / Discard — directly under sliders
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
                            Text("Save Settings", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StubContent(
    emoji: String,
    title: String,
    description: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontSize = 15.sp,
                color = Color(0xFF757575),
                lineHeight = 22.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Coming soon",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Shared Components ──

@Composable
private fun VoiceRow(
    friendlyName: String,
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
            text = friendlyName,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
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
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier.padding(start = 8.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
private fun SliderSetting(
    label: String,
    value: Float,
    valueLabel: String,
    min: Float,
    max: Float,
    steps: Int,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF616161)
            )
            Text(
                text = valueLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = min..max,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
