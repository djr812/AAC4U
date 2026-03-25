package net.djrogers.aac4u.ui.quickphrases

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickPhrasesScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuickPhrasesViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    // Delete confirmation
    if (state.showDeleteConfirmation && state.deletingPhrase != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDialog,
            title = { Text("Delete Phrase") },
            text = { Text("Delete \"${state.deletingPhrase?.label}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::confirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDialog) { Text("Cancel") }
            }
        )
    }

    // Add/Edit dialog
    if (state.showAddDialog) {
        QuickPhraseEditDialog(
            isNew = state.editingPhrase == null,
            label = state.editLabel,
            phrase = state.editPhraseText,
            groupName = state.editGroupName,
            onLabelChanged = viewModel::updateEditLabel,
            onPhraseChanged = viewModel::updateEditPhrase,
            onGroupChanged = viewModel::updateEditGroup,
            onSave = viewModel::savePhrase,
            onDismiss = viewModel::dismissDialog
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quick Phrases") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Hint
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFE3F2FD).copy(alpha = 0.5f)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💡", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Tap any phrase to speak it immediately. Long-press to edit.",
                            fontSize = 13.sp, color = Color(0xFF616161), lineHeight = 18.sp
                        )
                    }
                }
            }

            // Groups
            state.groups.forEachIndexed { index, group ->
                val isExpanded = state.expandedGroupIndex == index
                val phraseCount = group.phrases.size

                // Group header
                item(key = "group_$index") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.expandGroup(if (isExpanded) -1 else index) },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isExpanded) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                        shadowElevation = if (isExpanded) 2.dp else 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(group.emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = group.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF212121)
                                )
                                if (phraseCount > 0) {
                                    Text(
                                        text = "$phraseCount phrase${if (phraseCount != 1) "s" else ""}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                            Text(
                                text = if (isExpanded) "▲" else "▼",
                                fontSize = 14.sp,
                                color = Color(0xFF757575)
                            )
                        }
                    }
                }

                // Expanded phrases
                if (isExpanded) {
                    if (group.phrases.isEmpty()) {
                        item(key = "empty_$index") {
                            Text(
                                text = "No phrases in this group yet.",
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E),
                                modifier = Modifier.padding(start = 48.dp, top = 4.dp, bottom = 4.dp)
                            )
                        }
                    }

                    group.phrases.forEachIndexed { phraseIndex, phrase ->
                        item(key = "phrase_${group.name}_${phrase.id}") {
                            QuickPhraseCard(
                                phrase = phrase,
                                isSpeaking = state.speakingPhraseId == phrase.id,
                                onSpeak = { viewModel.speakPhrase(phrase) },
                                onStop = { viewModel.stopSpeaking() },
                                onEdit = { viewModel.showEditDialog(phrase) },
                                onDelete = { viewModel.showDeleteConfirmation(phrase) }
                            )
                        }
                    }

                    // Add button for this group
                    item(key = "add_$index") {
                        OutlinedButton(
                            onClick = { viewModel.showAddDialog(group.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 36.dp)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("＋ Add to ${group.name}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Bottom spacer
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun QuickPhraseCard(
    phrase: net.djrogers.aac4u.domain.model.AACButton,
    isSpeaking: Boolean,
    onSpeak: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp)
            .clip(RoundedCornerShape(10.dp))
            .combinedClickable(
                onClick = { if (isSpeaking) onStop() else onSpeak() },
                onLongClick = onEdit
            ),
        shape = RoundedCornerShape(10.dp),
        color = if (isSpeaking) Color(0xFFE8F5E9) else Color.White,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play/stop button
            Surface(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { if (isSpeaking) onStop() else onSpeak() },
                color = if (isSpeaking) Color(0xFFEF5350) else Color(0xFF43A047),
                shape = RoundedCornerShape(6.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isSpeaking) "⏹" else "▶",
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Phrase text
            Text(
                text = phrase.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Edit button
            IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                Text("✏", fontSize = 13.sp)
            }

            // Delete button
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                Text("🗑", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun QuickPhraseEditDialog(
    isNew: Boolean,
    label: String,
    phrase: String,
    groupName: String,
    onLabelChanged: (String) -> Unit,
    onPhraseChanged: (String) -> Unit,
    onGroupChanged: (String) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .widthIn(min = 320.dp, max = 460.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (isNew) "Add Quick Phrase" else "Edit Quick Phrase",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = label,
                    onValueChange = onLabelChanged,
                    label = { Text("Phrase") },
                    placeholder = { Text("e.g. I need a break") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = false,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = phrase,
                    onValueChange = onPhraseChanged,
                    label = { Text("What to speak (optional)") },
                    placeholder = { Text("Leave empty to use the phrase above") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = false,
                    maxLines = 2,
                    supportingText = {
                        Text("What TTS will say — useful if the spoken version differs", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Group selector
                Text("Group", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                Spacer(modifier = Modifier.height(8.dp))

                val groups = QuickPhrasesViewModel.DEFAULT_GROUPS

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    groups.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { (name, emoji) ->
                                val isSelected = groupName == name
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onGroupChanged(name) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF43A047)) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(emoji, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            name, fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF616161)
                                        )
                                    }
                                }
                            }
                            repeat(3 - row.size) { Spacer(modifier = Modifier.weight(1f)) }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) { Text("Cancel", fontSize = 14.sp, color = Color(0xFF757575)) }

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f).height(44.dp),
                        enabled = label.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        Text(if (isNew) "Add" else "Save", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}
