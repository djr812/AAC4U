package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.djrogers.aac4u.domain.model.AACButton as AACButtonModel

/**
 * The main communication grid displaying AAC buttons.
 *
 * Grid scales from 3×3 (large buttons, motor impairment) to 6×10 (compact, more vocabulary).
 * Button aspect ratio is roughly 1:1 to maintain consistent touch targets.
 */
@Composable
fun AACButtonGrid(
    buttons: List<AACButtonModel>,
    columns: Int,
    showLabels: Boolean,
    isEditMode: Boolean,
    onButtonTapped: (AACButtonModel) -> Unit,
    onButtonLongPressed: (AACButtonModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns.coerceIn(2, 10)),
        modifier = modifier,
        contentPadding = PaddingValues(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = buttons,
            key = { it.id }
        ) { button ->
            AACButton(
                button = button,
                showLabel = showLabels,
                onTap = { onButtonTapped(button) },
                onLongPress = { onButtonLongPressed(button) },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f) // Keep buttons square
            )
        }
    }
}
