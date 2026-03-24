package net.djrogers.aac4u.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import net.djrogers.aac4u.domain.model.PhraseHistoryEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateBack: () -> Unit,
    onLoadPhrase: (String) -> Unit = {},
    viewModel: HistoryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.showClearConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::hideClearConfirmation,
            title = { Text("Clear All History") },
            text = { Text("This will permanently delete all speech history for this profile. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = viewModel::clearAllHistory,
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF5350))
                ) { Text("Clear All", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideClearConfirmation) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Speech History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.entries.isNotEmpty()) {
                        TextButton(onClick = viewModel::showClearConfirmation) {
                            Text("Clear All", fontSize = 13.sp, color = Color(0xFFEF5350))
                        }
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
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::updateSearchQuery,
                label = { Text("Search history") },
                placeholder = { Text("Type to filter...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                leadingIcon = { Text("🔍", fontSize = 16.sp) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Text("✕", fontSize = 14.sp, color = Color(0xFF757575))
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Hint
            Text(
                text = "Tap a phrase to load it into the sentence bar for editing or speaking.",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📜", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "No matching phrases" else "No speech history yet",
                            fontSize = 18.sp, fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (state.searchQuery.isNotBlank()) "Try a different search term."
                            else "Spoken phrases will appear here. Tap any phrase to load it into the sentence bar.",
                            fontSize = 14.sp, color = Color(0xFF757575), textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Text(
                    text = "${state.entries.size} phrase${if (state.entries.size != 1) "s" else ""}",
                    fontSize = 12.sp, color = Color(0xFF9E9E9E),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items = state.entries, key = { it.id }) { entry ->
                        HistoryEntryCard(
                            entry = entry,
                            onLoadPhrase = {
                                onLoadPhrase(entry.fullPhrase)
                                onNavigateBack()
                            },
                            onDelete = { viewModel.deleteEntry(entry) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEntryCard(
    entry: PhraseHistoryEntry,
    onLoadPhrase: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onLoadPhrase() },
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFFAFAFA),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Load into sentence icon
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF1565C0),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("↩", fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.fullPhrase,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF212121),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = formatTimestamp(entry.timestamp),
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (showDeleteConfirm) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { showDeleteConfirm = false },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) { Text("Keep", fontSize = 12.sp, color = Color(0xFF757575)) }
                    TextButton(
                        onClick = { showDeleteConfirm = false; onDelete() },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) { Text("Delete", fontSize = 12.sp, color = Color(0xFFEF5350), fontWeight = FontWeight.Bold) }
                }
            } else {
                IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(32.dp)) {
                    Text("🗑", fontSize = 14.sp)
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        else -> SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
