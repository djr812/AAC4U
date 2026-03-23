package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun KeyboardInputDialog(
    isVisible: Boolean,
    onAddWord: (String) -> Unit,
    onAddSentence: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!isVisible) return

    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    // Auto-focus the text field when dialog opens
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
                .widthIn(min = 320.dp, max = 460.dp)
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Type Words",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Type a word to add to the sentence, or a full sentence to speak.",
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("Type here") },
                    placeholder = { Text("e.g. swimming, birthday party") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = false,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Cancel
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp, color = Color(0xFF757575))
                    }

                    // Add as words (splits by spaces, adds each word separately)
                    Button(
                        onClick = {
                            val trimmed = text.trim()
                            if (trimmed.isNotBlank()) {
                                // Check if it looks like multiple words
                                if (trimmed.contains(" ")) {
                                    onAddSentence(trimmed)
                                } else {
                                    onAddWord(trimmed)
                                }
                                text = ""
                                onDismiss()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        enabled = text.isNotBlank(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF43A047),
                            disabledContainerColor = Color(0xFFBDBDBD)
                        )
                    ) {
                        Text(
                            text = if (text.trim().contains(" ")) "Add Words" else "Add Word",
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
