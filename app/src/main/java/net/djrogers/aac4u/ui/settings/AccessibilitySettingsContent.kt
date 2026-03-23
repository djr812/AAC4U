package net.djrogers.aac4u.ui.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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

@Composable
fun AccessibilitySettingsContent(
    viewModel: AccessibilitySettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Accessibility",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "These settings are saved per profile.",
            fontSize = 14.sp,
            color = Color(0xFF757575)
        )

        // ── High Contrast ──
        AccessibilityToggleCard(
            title = "High Contrast",
            description = "Bolder colours, thicker borders, and stronger text contrast for improved visibility.",
            enabled = state.previewHighContrast,
            onToggle = viewModel::setHighContrast
        )

        // ── Large Text ──
        AccessibilityToggleCard(
            title = "Large Text",
            description = "Increases font sizes across the grid, sentence bar, and menus for easier reading.",
            enabled = state.previewLargeText,
            onToggle = viewModel::setLargeText
        )

        // ── Reduced Animations ──
        AccessibilityToggleCard(
            title = "Reduced Animations",
            description = "Disables button press animations, panel transitions, and other motion effects. Helps users with motion sensitivity.",
            enabled = state.previewReducedAnimations,
            onToggle = viewModel::setReducedAnimations
        )

        // ── Preview ──
        Text(
            text = "Preview",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 8.dp)
        )

        AccessibilityPreview(
            highContrast = state.previewHighContrast,
            largeText = state.previewLargeText
        )

        // ── Save / Discard ──
        if (state.hasUnsavedChanges) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::discardChanges,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Discard")
                }
                Button(
                    onClick = viewModel::saveSettings,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF43A047))
                ) {
                    Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (state.showSavedToast) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFC8E6C9)
            ) {
                Text(
                    text = "✓ Accessibility settings saved",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AccessibilityToggleCard(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (enabled) Color(0xFFE8F5E9) else Color(0xFFF5F5F5),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    lineHeight = 18.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Switch(
                checked = enabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF43A047)
                )
            )
        }
    }
}

@Composable
private fun AccessibilityPreview(
    highContrast: Boolean,
    largeText: Boolean
) {
    val bgColor = if (highContrast) Color(0xFF212121) else Color(0xFFF5F5F5)
    val textColor = if (highContrast) Color.White else Color(0xFF212121)
    val buttonColor = if (highContrast) Color(0xFF1B5E20) else Color(0xFFC8E6C9)
    val buttonTextColor = if (highContrast) Color.White else Color(0xFF212121)
    val borderWidth = if (highContrast) 3.dp else 1.dp
    val borderColor = if (highContrast) Color.White else Color(0xFFBDBDBD)
    val fontSize = if (largeText) 18.sp else 13.sp

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Fake sentence bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = if (highContrast) Color(0xFF0D47A1) else Color(0xFFE3F2FD)
            ) {
                Text(
                    text = "I want more please",
                    fontSize = fontSize,
                    fontWeight = if (highContrast) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (highContrast) Color.White else Color(0xFF0D47A1),
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Fake button grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("happy", "sad", "hungry", "tired").forEach { word ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .border(borderWidth, borderColor, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        color = buttonColor
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = word,
                                fontSize = fontSize,
                                fontWeight = if (highContrast) FontWeight.Bold else FontWeight.SemiBold,
                                color = buttonTextColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
