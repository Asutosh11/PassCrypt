package com.lightdarktools.passcrypt.presentation.ui.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

object CategoryColors {
    /**
     * Generates a consistent, distinct color based on the category name.
     * Uses HSL to ensure vibrant but readable colors.
     */
    fun getCategoryColor(category: String): Color {
        // Trim and lowercase to ensure "Work" and "work" match
        val normalized = category.trim().lowercase()
        
        // Use a more spread out hash for better variety
        val hash = normalized.hashCode()
        val hue = (abs(hash) % 360).toFloat()
        
        // We use a high saturation (0.7f) and moderate lightness (0.45f)
        // for good visibility in both themes.
        return Color.hsl(hue, 0.7f, 0.45f)
    }
}
