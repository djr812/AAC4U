package net.djrogers.aac4u.ui.grid

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.ui.grid.components.AACButtonGrid
import net.djrogers.aac4u.ui.grid.components.CategoryTabs
import net.djrogers.aac4u.ui.grid.components.CoreBar
import net.djrogers.aac4u.ui.grid.components.SentenceBar
import net.djrogers.aac4u.ui.theme.AACColors

/**
 * The main communication grid screen.
 *
 * Layout (tablet, portrait):
 * ┌──────────────────────────────┐
 * │  Sentence Bar          [▶]  │  ← Built sentence + speak button
 * ├──────────────────────────────┤
 * │  Core Vocabulary Bar        │  ← Blue-grey, always visible
 * ├──────────────────────────────┤
 * │ Feelings│Actions│Food│...   │  ← Coloured category tabs
 * ├──────────────────────────────┤
 * │  ┌────┐ ┌────┐ ┌────┐      │
 * │  │happy│ │sad │ │angry│     │  ← Pastel-coloured fringe buttons
 * │  └────┘ └────┘ └────┘      │
 * │  ┌────┐ ┌────┐ ┌────┐      │
 * │  │tired│ │sick│ │hurt │     │
 * │  └────┘ └────┘ └────┘      │
 * ├──────────────────────────────┤
 * │  Predicted: [more] [juice]  │  ← Prediction row
 * └──────────────────────────────┘
 */
@Composable
fun GridScreen(
    windowSizeClass: WindowSizeClass,
    onNavigateToSettings: () -> Unit,
    onNavigateToProfiles: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToQuickPhrases: () -> Unit,
    viewModel: GridViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        // Determine current category colour for the button grid
        val currentCategoryColor = uiState.currentCategory?.let {
            AACColors.forCategory(it.name)
        } ?: AACColors.forCategory("")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // ── Sentence Bar ──
            SentenceBar(
                sentenceParts = uiState.sentenceParts,
                isSpeaking = uiState.isSpeaking,
                onSpeak = viewModel::speakSentence,
                onBackspace = viewModel::removeLastPart,
                onClear = viewModel::clearSentence,
                onStop = viewModel::stopSpeaking,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Core Vocabulary Bar (distinct blue-grey style) ──
            if (uiState.coreButtons.isNotEmpty()) {
                CoreBar(
                    buttons = uiState.coreButtons,
                    onButtonTapped = viewModel::onButtonTapped,
                    isCore = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ── Category Tabs (coloured) ──
            if (uiState.categories.isNotEmpty()) {
                CategoryTabs(
                    categories = uiState.categories,
                    selectedCategory = uiState.currentCategory,
                    onCategorySelected = viewModel::selectCategory,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
            }

            // ── Main Button Grid (category-coloured) ──
            AACButtonGrid(
                buttons = uiState.buttons,
                columns = uiState.gridColumns,
                showLabels = uiState.showLabels,
                isEditMode = uiState.isEditMode,
                categoryColor = currentCategoryColor,
                onButtonTapped = viewModel::onButtonTapped,
                onButtonLongPressed = { /* Phase 2: edit dialog */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            // ── Prediction Row ──
            if (uiState.predictedButtons.isNotEmpty()) {
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
