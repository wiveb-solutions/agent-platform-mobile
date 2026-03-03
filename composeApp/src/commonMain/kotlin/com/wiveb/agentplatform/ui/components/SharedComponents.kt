package com.wiveb.agentplatform.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.ui.theme.*

@Composable
fun AgentBadge(agentId: String, modifier: Modifier = Modifier) {
    Surface(
        color = AgentColors.badge(agentId),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = agentId.uppercase(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun StatusDot(
    status: String,
    modifier: Modifier = Modifier,
    size: Int = 8,
) {
    val color = when (status) {
        "active", "ok", "up" -> Emerald400
        "idle" -> Amber400
        else -> Gray500
    }

    val isActive = status == "active"
    val pulseScale = if (isActive) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        ).value
    } else {
        1f
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        "pending" -> Gray500 to "Pending"
        "running", "in_progress" -> Amber400 to "Running"
        "done", "completed" -> Emerald400 to "Done"
        "error" -> Red400 to "Error"
        else -> Gray500 to status.replaceFirstChar { it.uppercase() }
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun PriorityBadge(priority: String?, modifier: Modifier = Modifier) {
    if (priority == null || priority == "none") return

    val (color, label) = when (priority) {
        "urgent" -> Red400 to "Urgent"
        "high" -> Orange400 to "High"
        "medium" -> Amber400 to "Medium"
        "low" -> Blue400 to "Low"
        else -> Gray400 to priority
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = Gray400,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun ErrorCard(message: String, onRetry: (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                color = Red400,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (onRetry != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Indigo500)
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Gray500,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun ContextBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
) {
    if (max <= 0) return
    val ratio = (current.toFloat() / max).coerceIn(0f, 1f)
    val color = when {
        ratio > 0.85f -> Red400
        ratio > 0.65f -> Amber400
        else -> Emerald400
    }
    val pct = (ratio * 100).toInt()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Context", color = Gray400, fontSize = 10.sp)
            Text("$pct%", color = color, fontSize = 10.sp)
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { ratio },
            color = color,
            trackColor = Gray800,
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        )
    }
}
