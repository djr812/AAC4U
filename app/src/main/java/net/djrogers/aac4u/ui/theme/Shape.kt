package net.djrogers.aac4u.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Rounded corners for AAC buttons and cards.
 * Larger radius = friendlier, more approachable feel.
 * Not too round — buttons need clear boundaries for motor-impaired users.
 */
val AAC4UShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
)
