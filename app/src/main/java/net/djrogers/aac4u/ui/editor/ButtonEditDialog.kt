package net.djrogers.aac4u.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

private val colorSwatches = listOf(
    null to "Default",
    "#FFCDD2" to "Pink",
    "#F8BBD0" to "Rose",
    "#E1BEE7" to "Lavender",
    "#BBDEFB" to "Sky Blue",
    "#B2EBF2" to "Cyan",
    "#C8E6C9" to "Green",
    "#DCEDC8" to "Lime",
    "#FFF9C4" to "Yellow",
    "#FFE0B2" to "Peach",
    "#FFCCBC" to "Coral",
    "#D7CCC8" to "Warm Grey"
)

private val wordTypeDescriptions = mapOf(
    "Pronoun" to "I, me, you, he, she, we, they",
    "Verb" to "go, want, eat, play, help",
    "Adjective" to "big, happy, good, fast, red",
    "Helper" to "is, am, are, can, will, have",
    "Preposition" to "in, on, to, with, under",
    "Question" to "what, where, who, when, why",
    "Social" to "yes, no, please, hello, sorry",
    "Determiner" to "this, that, the, some, all",
    "Adverb" to "not, now, here, there, again",
    "Conjunction" to "and, but, or, because"
)

@Composable
fun ButtonEditDialog(
    state: EditDialogState,
    onLabelChanged: (String) -> Unit,
    onPhraseChanged: (String) -> Unit,
    onColorChanged: (String?) -> Unit,
    onSave: () -> Unit,
    onToggleVisibility: () -> Unit,
    onShowDeleteConfirmation: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onDismiss: () -> Unit,
    onWordTypeChanged: (String?) -> Unit = {}
) {
    if (!state.isVisible) return

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
                .heightIn(max = 560.dp)
                .padding(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // ── Header ──
                Text(
                    text = when {
                        state.isCoreWord && state.isNewButton -> "Add Core Word"
                        state.isNewButton -> "Add New Button"
                        else -> "Edit Button"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)
                )

                // ── Scrollable Content ──
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Label field
                    OutlinedTextField(
                        value = state.editedLabel,
                        onValueChange = onLabelChanged,
                        label = { Text(if (state.isCoreWord) "Word" else "Button Label") },
                        placeholder = { Text(if (state.isCoreWord) "e.g. because, around, myself" else "e.g. apple, happy, I want") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        isError = state.duplicateWarning != null
                    )

                    // Duplicate warning
                    if (state.duplicateWarning != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("⚠️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = state.duplicateWarning,
                                    fontSize = 13.sp,
                                    color = Color(0xFFE65100),
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phrase field
                    OutlinedTextField(
                        value = state.editedPhrase,
                        onValueChange = onPhraseChanged,
                        label = { Text("Phrase to Speak") },
                        placeholder = { Text("Leave empty to use the label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        supportingText = {
                            Text(
                                text = "What TTS will say when this button is tapped",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (state.isCoreWord) {
                        // ── Word Type Picker ──
                        Text(
                            text = "Word Type",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF616161)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Choose the type so the word appears in the correct group.",
                            fontSize = 12.sp,
                            color = Color(0xFF9E9E9E)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        CoreWordTypeColors.allTypes.forEach { type ->
                            val isSelected = state.selectedWordType == type
                            val hexColor = CoreWordTypeColors.getHexColor(type) ?: "#E0E0E0"
                            val bgColor = Color(android.graphics.Color.parseColor(hexColor))
                            val description = wordTypeDescriptions[type] ?: ""

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onWordTypeChanged(type) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) bgColor else bgColor.copy(alpha = 0.3f),
                                border = if (isSelected) {
                                    androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF42A5F5))
                                } else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) Color(0xFF42A5F5) else Color(0xFFE0E0E0)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = type,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                            color = Color(0xFF212121)
                                        )
                                        Text(
                                            text = description,
                                            fontSize = 11.sp,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // ── Colour Picker ──
                        Text(
                            text = "Button Colour",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF616161)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val rows = colorSwatches.chunked(4)
                        rows.forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                row.forEach { (hex, label) ->
                                    val isSelected = state.editedColor == hex
                                    val swatchColor = if (hex != null) {
                                        Color(android.graphics.Color.parseColor(hex))
                                    } else {
                                        Color(0xFFE0E0E0)
                                    }

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(vertical = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(swatchColor)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color(0xFF42A5F5) else Color(0xFFBDBDBD),
                                                    shape = CircleShape
                                                )
                                                .clickable { onColorChanged(hex) }
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = label, fontSize = 10.sp, color = Color(0xFF9E9E9E))
                                    }
                                }
                                repeat(4 - row.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    // Hide/Show and Delete — existing buttons only
                    if (!state.isNewButton) {
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onToggleVisibility,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                val isVisible = state.button?.isVisible ?: true
                                Text(if (isVisible) "👁 Hide" else "👁 Show", fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = onShowDeleteConfirmation,
                                modifier = Modifier.weight(1f).height(40.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350))
                            ) {
                                Text("🗑 Delete", fontSize = 13.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── Action Buttons (pinned bottom) ──
                HorizontalDivider(color = Color(0xFFE0E0E0))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = Color(0xFF757575))
                    }

                    val canSave = state.editedLabel.isNotBlank() &&
                            state.duplicateWarning == null &&
                            (!state.isCoreWord || !state.isNewButton || state.selectedWordType != null)

                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f).height(44.dp),
                        enabled = canSave,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        Text(
                            text = if (state.isNewButton) "Add" else "Save",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // ── Delete Confirmation ──
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete Button") },
            text = { Text("Are you sure you want to delete \"${state.button?.label}\"? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                ) { Text("Delete", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text("Cancel") }
            }
        )
    }
}
