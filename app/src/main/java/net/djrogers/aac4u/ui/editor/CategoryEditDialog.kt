package net.djrogers.aac4u.ui.editor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun CategoryEditDialog(
    state: CategoryDialogState,
    onNameChanged: (String) -> Unit,
    onSave: () -> Unit,
    onToggleVisibility: () -> Unit,
    onShowDeleteConfirmation: () -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!state.isVisible) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.widthIn(min = 300.dp, max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Header
                Text(
                    text = if (state.isNewCategory) "Add Category" else "Edit Category",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Name field
                OutlinedTextField(
                    value = state.editedName,
                    onValueChange = onNameChanged,
                    label = { Text("Category Name") },
                    placeholder = { Text("e.g. Animals, School, Emotions") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                // Hide/Show and Delete — only for existing categories
                if (!state.isNewCategory) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onToggleVisibility,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            val isVisible = state.category?.isVisible ?: true
                            Text(
                                text = if (isVisible) "👁 Hide" else "👁 Show",
                                fontSize = 13.sp
                            )
                        }

                        OutlinedButton(
                            onClick = onShowDeleteConfirmation,
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF5350)
                            )
                        ) {
                            Text("🗑 Delete", fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                HorizontalDivider(color = Color(0xFFE0E0E0))

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = Color(0xFF757575))
                    }

                    Button(
                        onClick = onSave,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = state.editedName.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        Text(
                            text = if (state.isNewCategory) "Add" else "Save",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    // Delete confirmation
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text("Delete Category") },
            text = {
                Text("Are you sure you want to delete \"${state.category?.name}\"? All buttons in this category will be permanently deleted.")
            },
            confirmButton = {
                TextButton(
                    onClick = onConfirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) {
                    Text("Cancel")
                }
            }
        )
    }
}
