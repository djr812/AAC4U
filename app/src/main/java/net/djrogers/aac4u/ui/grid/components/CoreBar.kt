package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import net.djrogers.aac4u.domain.model.AACButton as AACButtonModel
import net.djrogers.aac4u.ui.theme.AACColors

/**
 * Horizontal scrolling row of core vocabulary buttons.
 * Always visible regardless of which category is selected.
 *
 * Visually distinct from fringe buttons:
 * - Blue-grey pastel background strip behind the row
 * - Buttons use the Core colour (AACColors.Core)
 * - Slightly smaller than grid buttons to save vertical space
 *
 * Core words: I, you, want, go, more, help, yes, no, etc.
 */
@Composable
fun CoreBar(
    buttons: List<AACButtonModel>,
    onButtonTapped: (AACButtonModel) -> Unit,
    isCore: Boolean = false,
    modifier: Modifier = Modifier
) {
    val barBackground = if (isCore) {
        AACColors.Core.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(barBackground)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .heightIn(min = 60.dp, max = 72.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(
                items = buttons,
                key = { it.id }
            ) { button ->
                AACButton(
                    button = button,
                    showLabel = true,
                    categoryColor = if (isCore) AACColors.Core else AACColors.forCategory(""),
                    onTap = { onButtonTapped(button) },
                    modifier = Modifier
                        .width(72.dp)
                        .fillMaxHeight()
                )
            }
        }
    }
}
