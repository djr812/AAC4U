package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.djrogers.aac4u.domain.model.AACButton

@Composable
fun SentenceBar(
    sentenceParts: List<String>,
    predictedWords: List<AACButton> = emptyList(),
    isSpeaking: Boolean,
    highContrast: Boolean = false,
    largeText: Boolean = false,
    selectedWordIndex: Int? = null,
    onSpeak: () -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onStop: () -> Unit,
    onPredictionTapped: (AACButton) -> Unit = {},
    onSuffixApplied: (String) -> Unit = {},
    onKeyboardTapped: () -> Unit = {},
    onWordTapped: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(sentenceParts.size, predictedWords.size) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val barColor = if (highContrast) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
    val wordChipColor = if (highContrast) Color(0xFF1565C0) else Color(0xFF90CAF9)
    val selectedChipColor = if (highContrast) Color(0xFFFF6F00) else Color(0xFFFFB74D)
    val wordTextColor = if (highContrast) Color.White else Color(0xFF0D47A1)
    val selectedTextColor = if (highContrast) Color.White else Color(0xFF212121)
    val suffixBarColor = if (highContrast) Color(0xFF0D47A1).copy(alpha = 0.8f) else Color(0xFFBBDEFB)
    val placeholderColor = if (highContrast) Color(0xFF90CAF9) else Color(0xFF90A4AE)

    val wordFontSize = if (largeText) 19.sp else 15.sp
    val predictionFontSize = if (largeText) 17.sp else 14.sp
    val placeholderFontSize = if (largeText) 17.sp else 14.sp
    val suffixFontSize = if (largeText) 15.sp else 12.sp

    Column(modifier = modifier) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (largeText) 56.dp else 48.dp),
            shape = RoundedCornerShape(
                topStart = 12.dp, topEnd = 12.dp,
                bottomStart = if (sentenceParts.isNotEmpty()) 0.dp else 12.dp,
                bottomEnd = if (sentenceParts.isNotEmpty()) 0.dp else 12.dp
            ),
            color = barColor,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(scrollState)
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    sentenceParts.forEachIndexed { index, part ->
                        val isSelected = selectedWordIndex == index

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) selectedChipColor else wordChipColor,
                            shadowElevation = if (isSelected) 2.dp else 0.5.dp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = if (highContrast) Color.White else Color(0xFFE65100),
                                            shape = RoundedCornerShape(6.dp)
                                        )
                                    } else Modifier
                                )
                                .clickable { onWordTapped(index) }
                        ) {
                            Text(
                                text = part,
                                fontSize = wordFontSize,
                                fontWeight = if (isSelected) FontWeight.ExtraBold
                                    else if (highContrast) FontWeight.ExtraBold
                                    else FontWeight.SemiBold,
                                color = if (isSelected) selectedTextColor else wordTextColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Predictions (only when no word is selected)
                    if (sentenceParts.isNotEmpty() && predictedWords.isNotEmpty() && selectedWordIndex == null) {
                        predictedWords.take(3).forEach { prediction ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (highContrast) Color(0xFF424242) else Color(0xFFE0E0E0).copy(alpha = 0.6f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { onPredictionTapped(prediction) }
                            ) {
                                Text(
                                    text = prediction.label,
                                    fontSize = predictionFontSize,
                                    fontWeight = FontWeight.Normal,
                                    fontStyle = FontStyle.Italic,
                                    color = if (highContrast) Color(0xFFBDBDBD) else Color(0xFF757575),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (sentenceParts.isEmpty()) {
                        Text(
                            text = "Tap words to build a sentence...",
                            fontSize = placeholderFontSize,
                            color = placeholderColor,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onKeyboardTapped, modifier = Modifier.size(36.dp)) {
                        Text("⌨", fontSize = 18.sp)
                    }

                    if (sentenceParts.isNotEmpty()) {
                        IconButton(onClick = onBackspace, modifier = Modifier.size(36.dp)) {
                            Text("⌫", fontSize = 18.sp)
                        }
                        IconButton(onClick = onClear, modifier = Modifier.size(36.dp)) {
                            Text("✕", fontSize = 16.sp, color = Color(0xFFEF5350))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { if (isSpeaking) onStop() else onSpeak() },
                        color = if (isSpeaking) Color(0xFFEF5350) else Color(0xFF43A047),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (isSpeaking) "⏹" else "▶",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Suffix bar
        if (sentenceParts.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
                color = suffixBarColor
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuffixButton("+s", Color(0xFF1565C0), suffixFontSize) { onSuffixApplied("s") }
                    SuffixButton("+ed", Color(0xFF6A1B9A), suffixFontSize) { onSuffixApplied("ed") }
                    SuffixButton("+ing", Color(0xFF2E7D32), suffixFontSize) { onSuffixApplied("ing") }
                    SuffixButton("+er", Color(0xFFE65100), suffixFontSize) { onSuffixApplied("er") }
                    SuffixButton("+est", Color(0xFFC62828), suffixFontSize) { onSuffixApplied("est") }
                    SuffixButton("+n't", Color(0xFF37474F), suffixFontSize) { onSuffixApplied("nt") }
                }
            }
        }
    }
}

@Composable
private fun SuffixButton(
    label: String,
    color: Color,
    fontSize: androidx.compose.ui.unit.TextUnit = 12.sp,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable(onClick = onClick),
        color = color,
        shape = RoundedCornerShape(6.dp)
    ) {
        Text(
            text = label,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
