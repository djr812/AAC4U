package net.djrogers.aac4u.ui.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.data.backup.ProfileBackup

@Composable
fun BackupSettingsContent(
    viewModel: BackupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val importFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.startImport(uri)
        }
    }

    // Export dialog
    if (state.showExportDialog) {
        ExportDialog(
            password = state.exportPassword,
            confirmPassword = state.exportConfirmPassword,
            isExporting = state.isExporting,
            error = state.error,
            onPasswordChanged = viewModel::updateExportPassword,
            onConfirmPasswordChanged = viewModel::updateExportConfirmPassword,
            onExport = viewModel::executeExport,
            onDismiss = viewModel::dismissExportDialog
        )
    }

    // Import dialog
    if (state.showImportDialog) {
        ImportDialog(
            state = state,
            onPasswordChanged = viewModel::updateImportPassword,
            onDecrypt = viewModel::decryptAndPreview,
            onSelectProfile = viewModel::selectImportProfile,
            onSetImportMode = viewModel::setImportMode,
            onSetReplaceProfileId = viewModel::setReplaceProfileId,
            onImport = viewModel::executeImport,
            onDismiss = viewModel::dismissImportDialog
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "💾", fontSize = 48.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Backup & Restore",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Export your profiles to a password-protected backup file, or import from a previous backup.",
            fontSize = 14.sp,
            color = Color(0xFF757575),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = viewModel::showExportAllDialog,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
        ) {
            Text("📤 Export All Profiles", fontSize = 15.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = { importFilePicker.launch(arrayOf("application/zip", "*/*")) },
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(48.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("📥 Import from Backup", fontSize = 15.sp)
        }

        if (state.exportSuccess) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✓ Backup exported successfully",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF43A047)
            )
        }
        if (state.importSuccess) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "✓ Import completed successfully",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF43A047)
            )
        }
    }
}

@Composable
private fun ExportDialog(
    password: String,
    confirmPassword: String,
    isExporting: Boolean,
    error: String?,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onExport: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier.widthIn(min = 300.dp, max = 400.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Export Backup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Set a password to protect this backup. You'll need this password to import it later.",
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChanged,
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = onConfirmPasswordChanged,
                    label = { Text("Confirm Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                if (error != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = error, fontSize = 13.sp, color = Color(0xFFEF5350))
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = onExport,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = !isExporting && password.isNotEmpty(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Export", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportDialog(
    state: BackupUiState,
    onPasswordChanged: (String) -> Unit,
    onDecrypt: () -> Unit,
    onSelectProfile: (ProfileBackup) -> Unit,
    onSetImportMode: (String) -> Unit,
    onSetReplaceProfileId: (Long) -> Unit,
    onImport: () -> Unit,
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
                .widthIn(min = 300.dp, max = 460.dp)
                .heightIn(max = 500.dp)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Import Backup",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!state.showImportOptions) {
                    // Step 1: Enter password to decrypt
                    Text(
                        text = "Enter the password used to create this backup.",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = state.importPassword,
                        onValueChange = onPasswordChanged,
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.error, fontSize = 13.sp, color = Color(0xFFEF5350))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

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
                            Text("Cancel", fontSize = 14.sp)
                        }

                        Button(
                            onClick = onDecrypt,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            enabled = !state.isImporting && state.importPassword.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            if (state.isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Unlock", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                } else {
                    // Step 2: Preview and choose import options
                    val preview = state.importPreview ?: return@Column

                    Text(
                        text = "Found ${preview.profiles.size} profile(s) in backup:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        preview.profiles.forEach { profile ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = profile.avatar, fontSize = 24.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.name,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "${profile.categories.size} categories, ${profile.categories.sumOf { it.buttons.size }} buttons",
                                            fontSize = 12.sp,
                                            color = Color(0xFF9E9E9E)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Import as:",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF616161)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = state.importMode == "new",
                                onClick = { onSetImportMode("new") }
                            )
                            Text("New profile(s)", fontSize = 14.sp)
                        }

                        if (state.profiles.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = state.importMode == "replace",
                                    onClick = { onSetImportMode("replace") }
                                )
                                Text("Replace existing profile", fontSize = 14.sp)
                            }

                            if (state.importMode == "replace") {
                                Spacer(modifier = Modifier.height(4.dp))
                                state.profiles.forEach { profile ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 32.dp)
                                    ) {
                                        RadioButton(
                                            selected = state.replaceProfileId == profile.id,
                                            onClick = { onSetReplaceProfileId(profile.id) }
                                        )
                                        Text(
                                            text = "${profile.avatar} ${profile.name}",
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (state.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = state.error, fontSize = 13.sp, color = Color(0xFFEF5350))
                    }

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
                            Text("Cancel", fontSize = 14.sp)
                        }

                        Button(
                            onClick = onImport,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            enabled = !state.isImporting &&
                                    (state.importMode == "new" || state.replaceProfileId != null),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            if (state.isImporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Import", fontSize = 14.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}