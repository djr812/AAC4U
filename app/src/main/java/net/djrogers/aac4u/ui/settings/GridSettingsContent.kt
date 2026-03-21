package net.djrogers.aac4u.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import net.djrogers.aac4u.domain.model.LabelPosition

@Composable
fun GridSettingsContent(
    viewModel: GridSettingsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── LEFT: Mini Grid Preview ──
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFFFAFAFA),
            shadowElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Preview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF616161),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    MiniGridPreview(
                        columns = state.previewColumns,
                        showLabels = state.previewLabelPosition != LabelPosition.HIDDEN,
                        labelBelow = state.previewLabelPosition == LabelPosition.BELOW
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${state.previewColumns} columns",
                    fontSize = 13.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        // ── RIGHT: Controls (scrollable) ──
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Column count
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Columns",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF616161)
                        )
                        Text(
                            text = "${state.previewColumns}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "2", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                        Slider(
                            value = state.previewColumns.toFloat(),
                            onValueChange = { viewModel.setPreviewColumns(it.toInt()) },
                            valueRange = 2f..10f,
                            steps = 7,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(text = "10", fontSize = 12.sp, color = Color(0xFFBDBDBD))
                    }

                    Text(
                        text = when (state.previewColumns) {
                            in 2..3 -> "Large buttons — easier to tap"
                            in 4..5 -> "Standard layout"
                            in 6..7 -> "Compact — more buttons visible"
                            else -> "Very compact — advanced users"
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            // Label position
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Button Labels",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LabelPosition.entries.forEach { position ->
                        val isSelected = state.previewLabelPosition == position
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setPreviewLabelPosition(position) }
                                .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF42A5F5) else Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = when (position) {
                                    LabelPosition.ABOVE -> "Above image"
                                    LabelPosition.BELOW -> "Below image"
                                    LabelPosition.HIDDEN -> "Hidden (image only)"
                                },
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            // Orientation lock
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Screen Orientation",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OrientationLock.entries.forEach { lock ->
                        val isSelected = state.previewOrientationLock == lock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setPreviewOrientationLock(lock) }
                                .background(if (isSelected) Color(0xFFE3F2FD) else Color.Transparent)
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color(0xFF42A5F5) else Color(0xFFE0E0E0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = lock.label,
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                                Text(
                                    text = lock.description,
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }
                }
            }

            // Save / Discard
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFFAFAFA),
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (state.hasUnsavedChanges) {
                            OutlinedButton(
                                onClick = viewModel::discardChanges,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFEF5350)
                                )
                            ) {
                                Text("Discard", fontSize = 14.sp)
                            }
                        }

                        Button(
                            onClick = viewModel::saveSettings,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            enabled = state.hasUnsavedChanges,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF43A047),
                                disabledContainerColor = Color(0xFFBDBDBD)
                            )
                        ) {
                            Text("Save", fontSize = 14.sp, color = Color.White)
                        }
                    }
                }
            }

            // Bottom spacer for scroll padding
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun MiniGridPreview(
    columns: Int,
    showLabels: Boolean,
    labelBelow: Boolean
) {
    val rows = 4.coerceAtMost(6)
    val totalButtons = columns * rows

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(rows) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(columns) { col ->
                    val index = row * columns + col
                    if (index < totalButtons) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    listOf(
                                        Color(0xFFFFCDD2),
                                        Color(0xFFC8E6C9),
                                        Color(0xFFBBDEFB),
                                        Color(0xFFFFE0B2),
                                        Color(0xFFE1BEE7),
                                        Color(0xFFFFF9C4),
                                        Color(0xFFB2EBF2),
                                        Color(0xFFDCEDC8)
                                    )[index % 8]
                                )
                                .border(
                                    width = 0.5.dp,
                                    color = Color(0x20000000),
                                    shape = RoundedCornerShape(4.dp)
                                ),
                            contentAlignment = if (labelBelow) Alignment.TopCenter else Alignment.Center
                        ) {
                            if (showLabels) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = if (labelBelow) Arrangement.Bottom else Arrangement.Center,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(1.dp)
                                ) {
                                    if (!labelBelow) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .height(2.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(Color(0x40000000))
                                        )
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    if (labelBelow) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(0.6f)
                                                .height(2.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(Color(0x40000000))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}