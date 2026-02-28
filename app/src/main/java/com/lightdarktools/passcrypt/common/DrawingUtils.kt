package com.lightdarktools.passcrypt.common

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.random.Random

object SketchedDrawing {

    /**
     * Draws a clean, straight architectural line.
     */
    fun DrawScope.drawSketchedLine(
        start: Offset,
        end: Offset,
        color: Color,
        strokeWidth: Float,
        passes: Int = 1, // Ignored now
        wobble: Float = 0f, // Ignored now
        overshoot: Float = 0f // Ignored now
    ) {
        drawLine(
            color = color,
            start = start,
            end = end,
            strokeWidth = strokeWidth
        )
    }

    /**
     * Draws a clean rectangular border.
     */
    fun DrawScope.drawSketchedRect(
        color: Color,
        strokeWidth: Float,
        size: Size = this.size,
        wobble: Float = 0f,
        overshoot: Float = 0f
    ) {
        val w = size.width
        val h = size.height
        
        // Use drawLine for manual control if needed, or simple drawRect for border
        drawSketchedLine(Offset(0f, 0f), Offset(w, 0f), color, strokeWidth)
        drawSketchedLine(Offset(w, 0f), Offset(w, h), color, strokeWidth)
        drawSketchedLine(Offset(w, h), Offset(0f, h), color, strokeWidth)
        drawSketchedLine(Offset(0f, h), Offset(0f, 0f), color, strokeWidth)
    }

    /**
 * Draws a clean marker wash or highlight stroke.
 */
fun DrawScope.drawWatercolorWash(
    color: Color,
    size: Size = this.size,
    horizontal: Boolean = true
) {
    val alpha = color.alpha * 0.25f
    val brushColor = color.copy(alpha = alpha)
    val w = size.width
    val h = size.height
    
    if (horizontal) {
        val y = h / 2f
        drawLine(
            color = brushColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = h
        )
    } else {
        val x = w / 2f
        drawLine(
            color = brushColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = w
        )
    }
}

/**
 * Draws an architectural dot grid background.
 */
fun DrawScope.drawSubtleGrid(
    color: Color,
    spacing: Float = 40f
) {
    val dotColor = color.copy(alpha = 0.08f)
    val w = size.width
    val h = size.height
    
    var x = spacing / 2f
    while (x < w) {
        var y = spacing / 2f
        while (y < h) {
            drawCircle(
                color = dotColor,
                radius = 1f,
                center = Offset(x, y)
            )
            y += spacing
        }
        x += spacing
    }
}

    /**
     * Draws a hatching pattern in a corner or area.
     */
    fun DrawScope.drawHatching(
        color: Color,
        size: Size = this.size,
        spacing: Float = 12f,
        count: Int = 5,
        angle: Float = 45f // Currently simplified to diagonal
    ) {
        val w = size.width
        val h = size.height
        
        repeat(count) { i ->
            val offset = i * spacing
            drawSketchedLine(
                start = Offset(w - 40f - offset, 0f),
                end = Offset(w, 40f + offset),
                color = color.copy(alpha = color.alpha * 0.3f),
                strokeWidth = 1f,
                passes = 1,
                overshoot = 0f
            )
        }
    }
}
