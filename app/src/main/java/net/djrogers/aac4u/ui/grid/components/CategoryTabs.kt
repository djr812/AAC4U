package net.djrogers.aac4u.ui.grid.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.djrogers.aac4u.domain.model.Category
import net.djrogers.aac4u.ui.theme.AACColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryTabs(
    categories: List<Category>,
    selectedCategory: Category?,
    isEditMode: Boolean = false,
    onCategorySelected: (Category) -> Unit,
    onCategoryEdit: (Category) -> Unit = {},
    onCategoryMoveUp: (Category) -> Unit = {},
    onCategoryMoveDown: (Category) -> Unit = {},
    onAddCategory: () -> Unit = {},
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
        divider = {}
    ) {
        categories.forEachIndexed { index, category ->
            val categoryColor = AACColors.forCategory(category.name)
            val isSelected = index == selectedIndex

            Tab(
                selected = isSelected,
                onClick = {},
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isSelected) categoryColor else categoryColor.copy(alpha = 0.3f)
                        )
                        .combinedClickable(
                            onClick = {
                                if (isEditMode) {
                                    onCategoryEdit(category)
                                } else {
                                    onCategorySelected(category)
                                }
                            },
                            onLongClick = {
                                if (!isEditMode) {
                                    onCategoryEdit(category)
                                }
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reorder arrows in edit mode
                    if (isEditMode && index > 0) {
                        Text(
                            text = "◀",
                            fontSize = 12.sp,
                            color = AACColors.textOn(categoryColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCategoryMoveUp(category) }
                                .padding(horizontal = 4.dp)
                        )
                    }

                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = AACColors.textOn(categoryColor),
                        fontSize = 14.sp
                    )

                    // Edit icon in edit mode
                    if (isEditMode) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "✏",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCategoryEdit(category) }
                                .padding(horizontal = 4.dp)
                        )
                    }

                    // Reorder arrow right in edit mode
                    if (isEditMode && index < categories.size - 1) {
                        Text(
                            text = "▶",
                            fontSize = 12.sp,
                            color = AACColors.textOn(categoryColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { onCategoryMoveDown(category) }
                                .padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Add category tab in edit mode
        if (isEditMode) {
            Tab(
                selected = false,
                onClick = onAddCategory,
                modifier = Modifier.padding(horizontal = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .padding(horizontal = 2.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0E0E0))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "＋ Add",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF616161)
                    )
                }
            }
        }
    }
}