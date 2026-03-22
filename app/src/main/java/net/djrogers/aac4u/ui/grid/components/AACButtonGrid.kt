package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import net.djrogers.aac4u.domain.model.AACButton as AACButtonModel

@Composable
fun AACButtonGrid(
    buttons: List<AACButtonModel>,
    columns: Int,
    showLabels: Boolean,
    isEditMode: Boolean,
    categoryColor: Color,
    onButtonTapped: (AACButtonModel) -> Unit,
    onButtonLongPressed: (AACButtonModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns.coerceIn(2, 10)),
        modifier = modifier,
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(
            items = buttons,
            key = { it.id },
            contentType = { "aac_button" } // Helps Compose reuse compositions
        ) { button ->
            AACButton(
                button = button,
                showLabel = showLabels,
                categoryColor = categoryColor,
                isEditMode = isEditMode,
                onTap = { onButtonTapped(button) },
                onLongPress = { onButtonLongPressed(button) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
        }
    }
}
