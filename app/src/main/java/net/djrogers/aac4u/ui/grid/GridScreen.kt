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

/**
 * The main communication grid screen.
 *
 * Layout (tablet, portrait):
 * ┌──────────────────────────────┐
 * │  Sentence Bar          [▶]  │  ← Built sentence + speak button
 * ├──────────────────────────────┤
 * │  Core Vocabulary Bar        │  ← Always visible (want, go, more...)
 * ├──────────────────────────────┤
 * │ Food | People | Feelings |  │  ← Category tabs
 * ├──────────────────────────────┤
 * │  ┌────┐ ┌────┐ ┌────┐      │
 * │  │ 🍎 │ │ 🍌 │ │ 🥪 │ ... │  ← Fringe vocabulary grid
 * │  │apple│ │bana│ │sand│      │
 * │  └────┘ └────┘ └────┘      │
 * │  ┌────┐ ┌────┐ ┌────┐      │
 * │  │ 🥛 │ │ 💧 │ │ 🍕 │ ... │
 * │  │milk │ │water│ │pizza│     │
 * │  └────┘ └────┘ └────┘      │
 * ├──────────────────────────────┤
 * │  Predicted: [juice] [more]  │  ← Predictions (optional)
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
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

            Spacer(modifier = Modifier.height(4.dp))

            // ── Core Vocabulary Bar ──
            if (uiState.coreButtons.isNotEmpty()) {
                CoreBar(
                    buttons = uiState.coreButtons,
                    onButtonTapped = viewModel::onButtonTapped,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Category Tabs ──
            if (uiState.categories.isNotEmpty()) {
                CategoryTabs(
                    categories = uiState.categories,
                    selectedCategory = uiState.currentCategory,
                    onCategorySelected = viewModel::selectCategory,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // ── Main Button Grid ──
            AACButtonGrid(
                buttons = uiState.buttons,
                columns = uiState.gridColumns,
                showLabels = uiState.showLabels,
                isEditMode = uiState.isEditMode,
                onButtonTapped = viewModel::onButtonTapped,
                onButtonLongPressed = { /* Open edit dialog — Phase 2 */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Take remaining vertical space
            )

            // ── Prediction Row ──
            if (uiState.predictedButtons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                CoreBar(
                    buttons = uiState.predictedButtons,
                    onButtonTapped = viewModel::onButtonTapped,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
