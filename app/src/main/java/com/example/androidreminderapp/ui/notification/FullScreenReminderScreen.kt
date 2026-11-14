package com.example.androidreminderapp.ui.notification

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val BackgroundTop = Color(0xFF2B0A45)
private val BackgroundBottom = Color(0xFF14061F)
private val NeonPurple = Color(0xFFC83EFF)
private val NeonPink = Color(0xFFFF4FD8)
private val BellGold = Color(0xFFFFD84D)

@Composable
fun FullScreenReminderScreen(
    alert: ReminderAlert,
    onSnooze: () -> Unit,
    onDismiss: () -> Unit,
    onMarkDone: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(BackgroundTop, Color(0xFF200936), BackgroundBottom)
                )
            )
    ) {
        AlertStars()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(tween(420)) + scaleIn(initialScale = 0.96f, animationSpec = tween(420))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = onDismiss,
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Dismiss reminder",
                                tint = Color.White,
                                modifier = Modifier.padding(10.dp).size(20.dp)
                            )
                        }
                        Text(
                            text = "Reminder Alert",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.size(40.dp))
                    }

                    Spacer(Modifier.height(36.dp))
                    PulsingBell()
                    Spacer(Modifier.height(28.dp))
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = alert.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.82f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))

                    Surface(
                        onClick = onSnooze,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .shadow(
                                elevation = 18.dp,
                                shape = RoundedCornerShape(50.dp),
                                spotColor = NeonPink.copy(alpha = 0.65f)
                            ),
                        shape = RoundedCornerShape(50.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(listOf(NeonPink, NeonPurple)),
                                    RoundedCornerShape(50.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Snooze,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Snooze (${alert.snoozeMinutes} min)",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(50.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            NeonPink.copy(alpha = 0.85f)
                        )
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Dismiss", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                   
                }
            }
        }
    }
}

@Composable
private fun PulsingBell() {
    val transition = rememberInfiniteTransition(label = "reminder bell pulse")
    val pulse by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell scale"
    )
    val glowAlpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.58f,
        animationSpec = infiniteRepeatable(
            animation = tween(1050, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell glow"
    )

    Box(
        modifier = Modifier
            .size(184.dp)
            .graphicsLayer {
                scaleX = pulse
                scaleY = pulse
            }
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            NeonPink.copy(alpha = glowAlpha),
                            NeonPurple.copy(alpha = glowAlpha * 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = size.minDimension / 2f
                    )
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(124.dp),
            shape = CircleShape,
            color = NeonPurple.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink.copy(alpha = 0.7f))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    modifier = Modifier.size(92.dp),
                    shape = CircleShape,
                    color = BackgroundBottom.copy(alpha = 0.82f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Alarm bell",
                            tint = BellGold,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertStars() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val points = listOf(
            Offset(size.width * 0.12f, size.height * 0.22f),
            Offset(size.width * 0.83f, size.height * 0.18f),
            Offset(size.width * 0.22f, size.height * 0.69f),
            Offset(size.width * 0.91f, size.height * 0.61f),
            Offset(size.width * 0.72f, size.height * 0.82f)
        )
        points.forEachIndexed { index, point ->
            drawCircle(
                color = Color.White.copy(alpha = if (index % 2 == 0) 0.34f else 0.2f),
                radius = if (index % 2 == 0) 1.7.dp.toPx() else 1.dp.toPx(),
                center = point
            )
        }
        drawArc(
            color = NeonPurple.copy(alpha = 0.16f),
            startAngle = 195f,
            sweepAngle = 150f,
            useCenter = false,
            topLeft = Offset(size.width * 0.18f, size.height * 0.17f),
            size = androidx.compose.ui.geometry.Size(size.width * 0.64f, size.height * 0.58f),
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
