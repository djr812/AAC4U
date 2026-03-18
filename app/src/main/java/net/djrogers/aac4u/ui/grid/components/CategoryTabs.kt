package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import net.djrogers.aac4u.domain.model.Category
import androidx.compose.ui.unit.dp

/**
 * Horizontal scrollable tabs for switching between fringe vocabulary categories.
 * Categories: Food, People, Feelings, Places, Actions, Things, etc.
 */
@Composable
fun CategoryTabs(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    ScrollableTabRow(
        selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
        modifier = modifier.fillMaxWidth(),
        edgePadding = 8.dp
    ) {
        categories.forEach { category ->
            Tab(
                selected = category.id == selectedCategory?.id,
                onClick = { onCategorySelected(category) },
                text = {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            )
        }
    }
}
