package net.djrogers.aac4u.ui.profiles

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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.ui.settings.BackupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    backupViewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dialogState by viewModel.dialogState.collectAsStateWithLifecycle()
    val backupState by backupViewModel.state.collectAsStateWithLifecycle()

    ProfileEditDialog(
        state = dialogState,
        onNameChanged = viewModel::updateName,
        onAvatarChanged = viewModel::updateAvatar,
        onAgeRangeChanged = viewModel::updateAgeRange,
        onSave = viewModel::saveProfile,
        onShowDeleteConfirmation = viewModel::showDeleteConfirmation,
        onConfirmDelete = viewModel::deleteProfile,
        onCancelDelete = viewModel::hideDeleteConfirmation,
        onDismiss = viewModel::dismissDialog
    )

    // Export dialog (reused from backup)
    if (backupState.showExportDialog) {
        ExportProfileDialog(
            password = backupState.exportPassword,
            confirmPassword = backupState.exportConfirmPassword,
            isExporting = backupState.isExporting,
            error = backupState.error,
            onPasswordChanged = backupViewModel::updateExportPassword,
            onConfirmPasswordChanged = backupViewModel::updateExportConfirmPassword,
            onExport = backupViewModel::executeExport,
            onDismiss = backupViewModel::dismissExportDialog
        )
    }

    val visibleProfiles = uiState.profiles.filter { it.name != "Default" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profiles") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::showCreateDialog,
                containerColor = Color(0xFF43A047),
                contentColor = Color.White
            ) {
                Text("＋", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (visibleProfiles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(text = "👤", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No profiles yet",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Create a profile to get started. Each profile has its own vocabulary, settings, and customisations.",
                        fontSize = 15.sp,
                        color = Color(0xFF757575),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = viewModel::showCreateDialog,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                    ) {
                        Text("＋ Create Profile", fontSize = 15.sp, color = Color.White)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(
                    items = visibleProfiles,
                    key = { it.id }
                ) { profile ->
                    ProfileCard(
                        profile = profile,
                        isActive = profile.id == uiState.activeProfileId,
                        onSwitch = { viewModel.switchProfile(profile.id) },
                        onEdit = { viewModel.showEditDialog(profile) },
                        onExport = { backupViewModel.showExportProfileDialog(profile.id) }
                    )
                }

                if (backupState.exportSuccess) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "✓ Backup exported successfully",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF43A047),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap a profile to switch. Tap the edit button to rename or customise.",
                        fontSize = 13.sp,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: UserProfile,
    isActive: Boolean,
    onSwitch: () -> Unit,
    onEdit: () -> Unit,
    onExport: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onSwitch)
            .then(
                if (isActive) {
                    Modifier.border(
                        width = 2.dp,
                        color = Color(0xFF43A047),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Color(0xFFE8F5E9) else Color(0xFFFAFAFA),
        shadowElevation = if (isActive) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(if (isActive) Color(0xFFC8E6C9) else Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = profile.avatar, fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2E7D32)
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Age ${profile.ageRange.label}",
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE0E0E0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )

                    if (isActive) {
                        Text(
                            text = "Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF43A047),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFC8E6C9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Export button
            IconButton(
                onClick = onExport,
                modifier = Modifier.size(36.dp)
            ) {
                Text("📤", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Edit button
            OutlinedButton(
                onClick = onEdit,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Text("✏ Edit", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun ExportProfileDialog(
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
                    text = "Export Profile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Set a password to protect this backup.",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
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
                        modifier = Modifier.weight(1f).height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }
                    Button(
                        onClick = onExport,
                        modifier = Modifier.weight(1f).height(44.dp),
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
