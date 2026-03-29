package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.delay
import net.djrogers.aac4u.domain.model.AACButton

data class WordFinderResult(
    val button: AACButton,
    val categoryName: String,
    val categoryId: Long,
    val isCoreWord: Boolean = false
)

@Composable
fun WordFinderDialog(
    isVisible: Boolean,
    results: List<WordFinderResult>,
    searchQuery: String,
    isSearching: Boolean,
    onQueryChanged: (String) -> Unit,
    onResultTapped: (WordFinderResult) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(100)
        focusRequester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = Modifier
                .widthIn(min = 340.dp, max = 500.dp)
                .heightIn(max = 480.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Find a Word",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Search across all categories. Tap a result to go to it.",
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChanged,
                    placeholder = { Text("Type to search...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Text("🔍", fontSize = 16.sp) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChanged("") }) {
                                Text("✕", fontSize = 14.sp, color = Color(0xFF757575))
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                when {
                    searchQuery.isBlank() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Start typing to find words...", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                        }
                    }
                    isSearching -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        }
                    }
                    results.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false).heightIn(min = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No words found for \"$searchQuery\"", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                        }
                    }
                    else -> {
                        Text(
                            text = "${results.size} result${if (results.size != 1) "s" else ""}",
                            fontSize = 12.sp, color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(items = results, key = { it.button.id }) { result ->
                                WordFinderResultCard(
                                    result = result,
                                    onTap = { onResultTapped(result) }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(40.dp),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Close", fontSize = 14.sp, color = Color(0xFF757575)) }
            }
        }
    }
}

@Composable
private fun WordFinderResultCard(
    result: WordFinderResult,
    onTap: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { onTap() },
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFAFAFA),
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (result.button.imagePath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(result.button.imagePath).crossfade(false).build(),
                    contentDescription = null, contentScale = ContentScale.Fit,
                    modifier = Modifier.size(36.dp).clip(RoundedCornerShape(6.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.button.label, fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold, color = Color(0xFF212121),
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (result.isCoreWord) "Core word · ${result.categoryName}" else "in ${result.categoryName}",
                    fontSize = 12.sp,
                    color = if (result.isCoreWord) Color(0xFF1565C0) else Color(0xFF757575)
                )
            }

            Text("→", fontSize = 16.sp, color = Color(0xFF43A047))
        }
    }
}
