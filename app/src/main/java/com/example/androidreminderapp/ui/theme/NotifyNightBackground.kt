package com.example.androidreminderapp.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import kotlin.random.Random

@Composable
fun NotifyNightBackground(showWaves: Boolean = false) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF47084F),
                    Color(0xFF200D2A),
                    Color(0xFF100916),
                    Color(0xFF0B0810)
                )
            )
        )
        val random = Random(31)
        repeat(105) {
            val x = random.nextFloat() * size.width
            val y = random.nextFloat() * size.height
            drawCircle(
                color = Color(0xFFFFE9FF).copy(alpha = random.nextFloat() * 0.4f + 0.1f),
                radius = random.nextFloat() * 1.5f + 0.35f,
                center = Offset(x, y)
            )
        }
        if (showWaves) {
            val wave = Path().apply {
                moveTo(0f, size.height * 0.88f)
                cubicTo(
                    size.width * 0.24f, size.height * 0.73f,
                    size.width * 0.39f, size.height * 0.98f,
                    size.width * 0.61f, size.height * 0.86f
                )
                cubicTo(
                    size.width * 0.8f, size.height * 0.76f,
                    size.width * 0.91f, size.height * 0.79f,
                    size.width, size.height * 0.71f
                )
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            drawPath(
                wave,
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF7C22D2), Color(0xFF32145C)),
                    startY = size.height * 0.72f,
                    endY = size.height
                )
            )
        }
    }
}
