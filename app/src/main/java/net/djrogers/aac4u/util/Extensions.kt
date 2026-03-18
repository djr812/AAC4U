package net.djrogers.aac4u.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shared extension functions used across the app.
 */

/**
 * Format a timestamp to a human-readable date/time string.
 */
fun Long.toFormattedDateTime(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Format a timestamp to just the date.
 */
fun Long.toFormattedDate(): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(this))
}

/**
 * Truncate a string to a maximum length with ellipsis.
 */
fun String.truncate(maxLength: Int): String {
    return if (length <= maxLength) this else "${take(maxLength - 1)}…"
}
