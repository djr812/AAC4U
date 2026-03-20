package net.djrogers.aac4u.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.ui.editor.ButtonEditDialog
import net.djrogers.aac4u.ui.editor.EditorViewModel
import net.djrogers.aac4u.ui.grid.components.AACButtonGrid
import net.djrogers.aac4u.ui.grid.components.CategoryTabs
import net.djrogers.aac4u.ui.grid.components.CoreBar
import net.djrogers.aac4u.ui.grid.components.SentenceBar
import net.djrogers.aac4u.ui.theme.AACColors

@Composable
fun GridScreen(
    windowSizeClass: WindowSizeClass,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: GridViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by editorViewModel.editState.collectAsStateWithLifecycle()

    // Edit dialog
    ButtonEditDialog(
        state = editState,
        onLabelChanged = editorViewModel::updateLabel,
        onPhraseChanged = editorViewModel::updatePhrase,
        onColorChanged = editorViewModel::updateColor,
        onSave = editorViewModel::saveButton,
        onToggleVisibility = editorViewModel::toggleVisibility,
        onShowDeleteConfirmation = editorViewModel::showDeleteConfirmation,
        onConfirmDelete = editorViewModel::deleteButton,
        onCancelDelete = editorViewModel::hideDeleteConfirmation,
        onDismiss = editorViewModel::dismissDialog
    )

    Scaffold(
        modifier = Modifier.fillMaxSize()
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

        val currentCategoryColor = uiState.currentCategory?.let {
            AACColors.forCategory(it.name)
        } ?: AACColors.forCategory("")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // ── Top Row: Hamburger + Edit Mode Banner + Sentence Bar ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hamburger menu button
                IconButton(
                    onClick = onOpenDrawer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = "☰",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF616161)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                if (isEditMode) {
                    // Edit mode banner
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✏️ Edit Mode — tap a button to edit",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE65100)
                            )
                            TextButton(
                                onClick = onToggleEditMode,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF43A047)
                                )
                            }
                        }
                    }
                } else {
                    // Normal sentence bar
                    SentenceBar(
                        sentenceParts = uiState.sentenceParts,
                        isSpeaking = uiState.isSpeaking,
                        onSpeak = viewModel::speakSentence,
                        onBackspace = viewModel::removeLastPart,
                        onClear = viewModel::clearSentence,
                        onStop = viewModel::stopSpeaking,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // ── Core Vocabulary Bar ──
            if (uiState.coreButtons.isNotEmpty()) {
                CoreBar(
                    buttons = uiState.coreButtons,
                    onButtonTapped = { button ->
                        if (isEditMode) {
                            editorViewModel.editButton(button)
                        } else {
                            viewModel.onButtonTapped(button)
                        }
                    },
                    isCore = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ── Category Tabs ──
            if (uiState.categories.isNotEmpty()) {
                CategoryTabs(
                    categories = uiState.categories,
                    selectedCategory = uiState.currentCategory,
                    onCategorySelected = viewModel::selectCategory,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ── Main Button Grid ──
            AACButtonGrid(
                buttons = uiState.buttons,
                columns = uiState.gridColumns,
                showLabels = uiState.showLabels,
                isEditMode = isEditMode,
                categoryColor = currentCategoryColor,
                onButtonTapped = { button ->
                    if (isEditMode) {
                        editorViewModel.editButton(button)
                    } else {
                        viewModel.onButtonTapped(button)
                    }
                },
                onButtonLongPressed = { button ->
                    if (!isEditMode) {
                        editorViewModel.editButton(button)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // ── Add Button (edit mode only) ──
            if (isEditMode && uiState.currentCategory != null) {
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = {
                        uiState.currentCategory?.let { category ->
                            editorViewModel.addNewButton(category.id)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "＋ Add New Button",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Prediction Row (hidden in edit mode) ──
            if (!isEditMode && uiState.predictedButtons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                CoreBar(
                    buttons = uiState.predictedButtons,
                    onButtonTapped = viewModel::onButtonTapped,
                    isCore = false,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
