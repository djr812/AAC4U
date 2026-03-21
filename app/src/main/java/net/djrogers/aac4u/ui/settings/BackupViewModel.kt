package net.djrogers.aac4u.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.djrogers.aac4u.data.backup.BackupData
import net.djrogers.aac4u.data.backup.BackupManager
import net.djrogers.aac4u.data.backup.ProfileBackup
import net.djrogers.aac4u.domain.model.UserProfile
import net.djrogers.aac4u.domain.repository.ProfileRepository
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class BackupUiState(
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val importSuccess: Boolean = false,
    val error: String? = null,

    // Export dialog
    val showExportDialog: Boolean = false,
    val exportPassword: String = "",
    val exportConfirmPassword: String = "",
    val exportType: String = "all",
    val selectedProfileId: Long? = null,

    // Import dialog
    val showImportDialog: Boolean = false,
    val importPassword: String = "",
    val importUri: Uri? = null,
    val importPreview: BackupData? = null,
    val showImportOptions: Boolean = false,
    val selectedImportProfile: ProfileBackup? = null,
    val importMode: String = "new",
    val replaceProfileId: Long? = null,

    // Available profiles for replace target
    val profiles: List<UserProfile> = emptyList()
)

@HiltViewModel
class BackupViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupManager: BackupManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _state = MutableStateFlow(BackupUiState())
    val state: StateFlow<BackupUiState> = _state.asStateFlow()

    init {
        loadProfiles()
    }

    private fun loadProfiles() {
        viewModelScope.launch {
            profileRepository.getAllProfiles().collect { profiles ->
                val visible = profiles.filter { it.name != "Default" }
                _state.update { it.copy(profiles = visible) }
            }
        }
    }

    // ── Export ──

    fun showExportAllDialog() {
        _state.update {
            it.copy(
                showExportDialog = true,
                exportType = "all",
                exportPassword = "",
                exportConfirmPassword = "",
                error = null,
                exportSuccess = false
            )
        }
    }

    fun showExportProfileDialog(profileId: Long) {
        _state.update {
            it.copy(
                showExportDialog = true,
                exportType = "single",
                selectedProfileId = profileId,
                exportPassword = "",
                exportConfirmPassword = "",
                error = null,
                exportSuccess = false
            )
        }
    }

    fun updateExportPassword(password: String) {
        _state.update { it.copy(exportPassword = password, error = null) }
    }

    fun updateExportConfirmPassword(password: String) {
        _state.update { it.copy(exportConfirmPassword = password, error = null) }
    }

    fun executeExport() {
        val currentState = _state.value

        if (currentState.exportPassword.length < 4) {
            _state.update { it.copy(error = "Password must be at least 4 characters") }
            return
        }
        if (currentState.exportPassword != currentState.exportConfirmPassword) {
            _state.update { it.copy(error = "Passwords do not match") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isExporting = true, error = null) }
            try {
                val zipBytes = if (currentState.exportType == "all") {
                    backupManager.exportAllProfiles(currentState.exportPassword)
                } else {
                    val profileId = currentState.selectedProfileId
                        ?: throw IllegalStateException("No profile selected")
                    backupManager.exportProfile(profileId, currentState.exportPassword)
                }

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val filename = "aac4u_backup_$timestamp.zip"
                val file = File(context.cacheDir, filename)
                file.writeBytes(zipBytes)

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/zip"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "AAC4U Backup - $filename")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                context.startActivity(Intent.createChooser(shareIntent, "Share Backup").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })

                _state.update {
                    it.copy(
                        isExporting = false,
                        exportSuccess = true,
                        showExportDialog = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isExporting = false,
                        error = "Export failed: ${e.message}"
                    )
                }
            }
        }
    }

    // ── Import ──

    /**
     * Called from file picker result — sets the URI and opens the dialog.
     */
    fun startImport(uri: Uri) {
        _state.update {
            it.copy(
                showImportDialog = true,
                importUri = uri,
                importPassword = "",
                importPreview = null,
                showImportOptions = false,
                selectedImportProfile = null,
                importMode = "new",
                replaceProfileId = null,
                error = null,
                importSuccess = false
            )
        }
    }

    fun updateImportPassword(password: String) {
        _state.update { it.copy(importPassword = password, error = null) }
    }

    fun decryptAndPreview() {
        val currentState = _state.value
        val uri = currentState.importUri

        if (uri == null) {
            _state.update { it.copy(error = "No backup file selected") }
            return
        }

        if (currentState.importPassword.isEmpty()) {
            _state.update { it.copy(error = "Please enter a password") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, error = null) }
            try {
                val backupData = backupManager.readBackup(uri, currentState.importPassword)
                _state.update {
                    it.copy(
                        isImporting = false,
                        importPreview = backupData,
                        showImportOptions = true
                    )
                }
            } catch (e: Exception) {
                val message = if (e.message?.contains("Incorrect password") == true ||
                    e.message?.contains("AEADBadTagException") == true) {
                    "Incorrect password"
                } else {
                    "Failed to read backup: ${e.message}"
                }
                _state.update { it.copy(isImporting = false, error = message) }
            }
        }
    }

    fun selectImportProfile(profile: ProfileBackup) {
        _state.update { it.copy(selectedImportProfile = profile) }
    }

    fun setImportMode(mode: String) {
        _state.update { it.copy(importMode = mode) }
    }

    fun setReplaceProfileId(profileId: Long) {
        _state.update { it.copy(replaceProfileId = profileId) }
    }

    fun executeImport() {
        val currentState = _state.value
        val preview = currentState.importPreview ?: return

        viewModelScope.launch {
            _state.update { it.copy(isImporting = true, error = null) }
            try {
                if (currentState.selectedImportProfile != null) {
                    val profile = currentState.selectedImportProfile
                    if (currentState.importMode == "replace" && currentState.replaceProfileId != null) {
                        backupManager.importReplaceProfile(currentState.replaceProfileId, profile)
                    } else {
                        backupManager.importAsNewProfile(profile)
                    }
                } else {
                    backupManager.importAllAsNew(preview)
                }

                _state.update {
                    it.copy(
                        isImporting = false,
                        importSuccess = true,
                        showImportDialog = false,
                        showImportOptions = false
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isImporting = false,
                        error = "Import failed: ${e.message}"
                    )
                }
            }
        }
    }

    fun dismissExportDialog() {
        _state.update { it.copy(showExportDialog = false, error = null) }
    }

    fun dismissImportDialog() {
        _state.update {
            it.copy(
                showImportDialog = false,
                showImportOptions = false,
                importUri = null,
                importPreview = null,
                error = null
            )
        }
    }

    fun clearSuccessState() {
        _state.update { it.copy(exportSuccess = false, importSuccess = false) }
    }
}