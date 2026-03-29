package net.djrogers.aac4u.ui.grid

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.ui.editor.ButtonEditDialog
import net.djrogers.aac4u.ui.editor.CategoryEditDialog
import net.djrogers.aac4u.ui.editor.CategoryEditorViewModel
import net.djrogers.aac4u.ui.editor.EditorViewModel
import net.djrogers.aac4u.ui.grid.components.AACButtonGrid
import net.djrogers.aac4u.ui.grid.components.CategoryTabs
import net.djrogers.aac4u.ui.grid.components.ExpandableCorePanel
import net.djrogers.aac4u.ui.grid.components.KeyboardInputDialog
import net.djrogers.aac4u.ui.grid.components.SentenceBar
import net.djrogers.aac4u.ui.grid.components.WordFinderDialog
import net.djrogers.aac4u.ui.theme.AACColors

@Composable
fun GridScreen(
    windowSizeClass: WindowSizeClass,
    isEditMode: Boolean,
    onToggleEditMode: () -> Unit,
    onOpenDrawer: () -> Unit,
    viewModel: GridViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel(),
    categoryEditorViewModel: CategoryEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by editorViewModel.editState.collectAsStateWithLifecycle()
    val categoryDialogState by categoryEditorViewModel.dialogState.collectAsStateWithLifecycle()

    val wordFinderResults by viewModel.wordFinderResults.collectAsStateWithLifecycle()
    val wordFinderQuery by viewModel.wordFinderQuery.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    var isCoreExpanded by remember { mutableStateOf(false) }
    var showKeyboardDialog by remember { mutableStateOf(false) }
    var showWordFinder by remember { mutableStateOf(false) }

    val hc = uiState.highContrastEnabled
    val lt = uiState.largeTextEnabled
    val ra = uiState.reducedAnimationsEnabled

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
        onDismiss = editorViewModel::dismissDialog,
        onWordTypeChanged = editorViewModel::updateWordType
    )

    CategoryEditDialog(
        state = categoryDialogState,
        onNameChanged = categoryEditorViewModel::updateName,
        onSave = categoryEditorViewModel::saveCategory,
        onToggleVisibility = categoryEditorViewModel::toggleVisibility,
        onShowDeleteConfirmation = categoryEditorViewModel::showDeleteConfirmation,
        onConfirmDelete = categoryEditorViewModel::deleteCategory,
        onCancelDelete = categoryEditorViewModel::hideDeleteConfirmation,
        onDismiss = categoryEditorViewModel::dismissDialog,
        onColorSelected = categoryEditorViewModel::selectColor
    )

    KeyboardInputDialog(
        isVisible = showKeyboardDialog,
        onAddWord = { viewModel.addTypedWord(it) },
        onAddSentence = { viewModel.addTypedSentence(it) },
        onDismiss = { showKeyboardDialog = false }
    )

    WordFinderDialog(
        isVisible = showWordFinder,
        results = wordFinderResults,
        searchQuery = wordFinderQuery,
        isSearching = isSearching,
        onQueryChanged = viewModel::updateWordFinderQuery,
        onResultTapped = { result ->
            showWordFinder = false
            viewModel.clearWordFinder()
            viewModel.navigateToWord(result)
        },
        onDismiss = {
            showWordFinder = false
            viewModel.clearWordFinder()
        }
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = if (hc) Color(0xFF121212) else MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val currentCategoryColor = uiState.currentCategory?.let { AACColors.forCategory(it.name) } ?: AACColors.forCategory("")

        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer, modifier = Modifier.size(48.dp)) {
                    Text("☰", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (hc) Color.White else Color(0xFF616161))
                }
                Spacer(modifier = Modifier.width(4.dp))

                if (isEditMode) {
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF3E0)).padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("✏️ Edit Mode — tap buttons or categories to edit", fontSize = if (lt) 15.sp else 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE65100))
                            TextButton(onClick = onToggleEditMode, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                Text("Done", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43A047))
                            }
                        }
                    }
                } else {
                    SentenceBar(
                        sentenceParts = uiState.sentenceParts, predictedWords = uiState.predictedButtons,
                        isSpeaking = uiState.isSpeaking, highContrast = hc, largeText = lt,
                        selectedWordIndex = uiState.selectedWordIndex,
                        onSpeak = viewModel::speakSentence, onBackspace = viewModel::removeLastPart,
                        onClear = viewModel::clearSentence, onStop = viewModel::stopSpeaking,
                        onPredictionTapped = { viewModel.onPredictionAccepted(it) },
                        onSuffixApplied = { viewModel.applySuffix(it) },
                        onKeyboardTapped = { showKeyboardDialog = true },
                        onSearchTapped = { showWordFinder = true },
                        onWordTapped = { viewModel.toggleWordSelection(it) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            val hasContent = uiState.categories.isNotEmpty() || uiState.coreButtons.isNotEmpty()

            if (!hasContent && !isEditMode) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("📋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("This profile is empty", fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = if (hc) Color.White else MaterialTheme.colorScheme.onSurface)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Use Edit Mode to add categories and buttons, or switch to a profile that has vocabulary set up.", fontSize = 15.sp, color = if (hc) Color(0xFFBDBDBD) else Color(0xFF757575), textAlign = TextAlign.Center, lineHeight = 22.sp)
                    }
                }
            } else {
                if (uiState.coreButtons.isNotEmpty()) {
                    ExpandableCorePanel(
                        coreButtons = uiState.coreButtons, isEditMode = isEditMode, isExpanded = isCoreExpanded,
                        highContrast = hc, largeText = lt, reducedAnimations = ra,
                        highlightedButtonId = uiState.highlightedButtonId,
                        requestedGroupIndex = uiState.requestedCoreGroupIndex,
                        onRequestedGroupConsumed = { viewModel.clearCoreGroupRequest() },
                        onToggleExpand = { isCoreExpanded = !isCoreExpanded },
                        onButtonTapped = { viewModel.onButtonTapped(it); isCoreExpanded = false },
                        onButtonEdit = { editorViewModel.editButton(it) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                if (uiState.categories.isNotEmpty() || isEditMode) {
                    CategoryTabs(
                        categories = uiState.categories, selectedCategory = uiState.currentCategory,
                        isEditMode = isEditMode, onCategorySelected = viewModel::selectCategory,
                        onCategoryEdit = { categoryEditorViewModel.showEditDialog(it) },
                        onCategoryMoveUp = { categoryEditorViewModel.moveCategoryUp(it, uiState.categories) },
                        onCategoryMoveDown = { categoryEditorViewModel.moveCategoryDown(it, uiState.categories) },
                        onAddCategory = { categoryEditorViewModel.showAddDialog() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                AACButtonGrid(
                    buttons = uiState.buttons, columns = uiState.gridColumns, showLabels = uiState.showLabels,
                    isEditMode = isEditMode, categoryColor = currentCategoryColor,
                    highContrast = hc, largeText = lt, reducedAnimations = ra,
                    highlightedButtonId = uiState.highlightedButtonId,
                    onButtonTapped = { if (isEditMode) editorViewModel.editButton(it) else viewModel.onButtonTapped(it) },
                    onButtonLongPressed = { if (!isEditMode) editorViewModel.editButton(it) },
                    modifier = Modifier.fillMaxWidth().weight(1f)
                )

                if (isEditMode && uiState.currentCategory != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = { uiState.currentCategory?.let { editorViewModel.addNewButton(it.id) } },
                        modifier = Modifier.fillMaxWidth().height(44.dp), shape = RoundedCornerShape(8.dp)
                    ) { Text("＋ Add New Button", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                }
            }
        }
    }
}
