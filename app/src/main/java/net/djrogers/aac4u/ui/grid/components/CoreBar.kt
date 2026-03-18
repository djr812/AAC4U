package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import net.djrogers.aac4u.domain.model.AACButton as AACButtonModel

/**
 * Horizontal scrolling row of core vocabulary buttons.
 * Always visible regardless of which category is selected.
 *
 * Core words: I, want, go, more, help, stop, yes, no, like, don't like, etc.
 * Also reused for the prediction row at the bottom.
 */
@Composable
fun CoreBar(
    buttons: List<AACButtonModel>,
    onButtonTapped: (AACButtonModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.heightIn(min = 64.dp, max = 80.dp),
        contentPadding = PaddingValues(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(
            items = buttons,
            key = { it.id }
        ) { button ->
            AACButton(
                button = button,
                showLabel = true,
                onTap = { onButtonTapped(button) },
                modifier = Modifier
                    .width(80.dp)
                    .fillMaxHeight()
            )
        }
    }
}
