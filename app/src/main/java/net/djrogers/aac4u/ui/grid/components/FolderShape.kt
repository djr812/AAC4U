package net.djrogers.aac4u.ui.grid.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A folder-shaped outline with a tab on the top-left, similar to
 * a classic Windows/macOS file folder icon.
 *
 * Shape:
 *   ┌──────┐
 *   │      └──────────────────┐
 *   │                         │
 *   │                         │
 *   │                         │
 *   └─────────────────────────┘
 *
 * @param tabWidthFraction How wide the tab is relative to total width (0.0-1.0)
 * @param tabHeightFraction How tall the tab is relative to total height (0.0-1.0)
 * @param cornerRadius Radius for rounded corners in pixels
 */
class FolderShape(
    private val tabWidthFraction: Float = 0.4f,
    private val tabHeightFraction: Float = 0.15f,
    private val cornerRadius: Float = 16f
) : Shape {

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val tabW = w * tabWidthFraction
        val tabH = h * tabHeightFraction
        val r = cornerRadius.coerceAtMost(tabH * 0.8f)

        val path = Path().apply {
            // Start at top-left corner (after radius)
            moveTo(r, 0f)

            // Top of tab — straight across to tab corner
            lineTo(tabW - r, 0f)

            // Tab corner — diagonal or curved down to body top
            quadraticBezierTo(tabW, 0f, tabW + r * 0.5f, tabH * 0.5f)
            quadraticBezierTo(tabW + r, tabH, tabW + r * 1.5f, tabH)

            // Body top — across to top-right corner
            lineTo(w - r, tabH)

            // Top-right corner
            quadraticBezierTo(w, tabH, w, tabH + r)

            // Right side down
            lineTo(w, h - r)

            // Bottom-right corner
            quadraticBezierTo(w, h, w - r, h)

            // Bottom across
            lineTo(r, h)

            // Bottom-left corner
            quadraticBezierTo(0f, h, 0f, h - r)

            // Left side up
            lineTo(0f, r)

            // Top-left corner
            quadraticBezierTo(0f, 0f, r, 0f)

            close()
        }

        return Outline.Generic(path)
    }
}
