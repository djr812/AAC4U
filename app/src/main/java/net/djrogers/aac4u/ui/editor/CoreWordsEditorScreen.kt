package net.djrogers.aac4u.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.domain.model.AACButton
import net.djrogers.aac4u.ui.grid.components.CoreWordGroups
import net.djrogers.aac4u.ui.theme.AACColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoreWordsEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: CoreWordsEditorViewModel = hiltViewModel(),
    editorViewModel: EditorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val editState by editorViewModel.editState.collectAsStateWithLifecycle()

    // Button edit dialog
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Core Words") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Description
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFCFD8DC).copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "💡", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Core words are always visible at the top of the communication grid. These are high-frequency words like \"I\", \"want\", \"go\", \"more\", \"help\".",
                        fontSize = 13.sp,
                        color = Color(0xFF616161),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.coreButtons.isEmpty() && state.coreCategoryId == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📋", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No core words yet",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create a core vocabulary category to get started.",
                            fontSize = 14.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = viewModel::createCoreCategory,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                        ) {
                            Text("Create Core Words", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            } else {
                // Grid of core words — colour-coded by group
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = state.coreButtons,
                        key = { it.id }
                    ) { button ->
                        CoreWordButton(
                            button = button,
                            onClick = { editorViewModel.editButton(button) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Add button — uses addNewCoreWord instead of addNewButton
                OutlinedButton(
                    onClick = {
                        state.coreCategoryId?.let { categoryId ->
                            editorViewModel.addNewCoreWord(categoryId)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(8.dp),
                    enabled = state.coreCategoryId != null
                ) {
                    Text(
                        text = "＋ Add Core Word",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun CoreWordButton(
    button: AACButton,
    onClick: () -> Unit
) {
    // Determine colour: use button's backgroundColor if set, otherwise look up from CoreWordGroups
    val buttonColor = if (button.backgroundColor != null) {
        try {
            Color(android.graphics.Color.parseColor(button.backgroundColor))
        } catch (e: Exception) {
            CoreWordGroups.colorForWord(button.label)
        }
    } else {
        CoreWordGroups.colorForWord(button.label)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        shape = RoundedCornerShape(8.dp),
        color = buttonColor,
        shadowElevation = 2.dp,
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = button.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF212121),
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                if (!button.isVisible) {
                    Text(
                        text = "hidden",
                        fontSize = 10.sp,
                        color = Color(0xFFEF5350)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xCCFF8F00)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✏", fontSize = 9.sp)
            }
        }
    }
}
