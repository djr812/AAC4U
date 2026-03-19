package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.ui.theme.AACColors

/**
 * Horizontal scrollable tabs for switching between fringe vocabulary categories.
 * Each tab is coloured with its category's pastel colour.
 */
@Composable
fun CategoryTabs(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIndex = categories.indexOf(selectedCategory).coerceAtLeast(0)

    ScrollableTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier.fillMaxWidth(),
        edgePadding = 4.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        indicator = {},
        divider = {} // Remove default divider for cleaner look
    ) {
        categories.forEachIndexed { index, category ->
            val categoryColor = AACColors.forCategory(category.name)
            val isSelected = index == selectedIndex

            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) categoryColor else categoryColor.copy(alpha = 0.3f)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = AACColors.textOn(categoryColor)
                    )
                }
            }
        }
    }
}
