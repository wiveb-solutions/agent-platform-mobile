package com.wiveb.agentplatform.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class DeepSearchEvent(
    val type: EventType,
    val message: String,
    val timestamp: String,
)

enum class EventType {
    PLAN, SEARCHING, FOUND, READING, SENDING,
}

@Composable
fun DeepSearchProgressPanel(
    events: List<DeepSearchEvent>,
    queriesCount: Int = 0,
    urlsCount: Int = 0,
    charsRead: Int = 0,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Gray900,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Purple600.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
            )
            .clip(RoundedCornerShape(12.dp)),
    ) {
        Column {
            // Header
            Surface(
                color = Purple600.copy(alpha = 0.2f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Deep Search",
                            tint = Purple400,
                            modifier = Modifier.size(20.dp),
                        )
                        Text(
                            text = "Deep Search in Progress",
                            color = Purple400,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    Text(
                        text = "${events.size} steps",
                        color = Gray400,
                        fontSize = 12.sp,
                    )
                }
            }

            // Timeline
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(events) { event ->
                    SearchEventItem(event)
                }
            }

            // Stats footer
            if (queriesCount > 0 || urlsCount > 0 || charsRead > 0) {
                HorizontalDivider(color = Gray800, modifier = Modifier.padding(vertical = 4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    StatItem(
                        icon = Icons.Default.Search,
                        value = queriesCount.toString(),
                        label = "Queries",
                        color = Blue400,
                    )
                    StatItem(
                        icon = Icons.Default.Link,
                        value = urlsCount.toString(),
                        label = "URLs",
                        color = Emerald400,
                    )
                    StatItem(
                        icon = Icons.Default.FileSize,
                        value = formatChars(charsRead),
                        label = "Read",
                        color = Amber400,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchEventItem(event: DeepSearchEvent) {
    val (icon, color, label) = when (event.type) {
        EventType.PLAN -> Icons.Default.FileText to Gray400 to "Planning"
        EventType.SEARCHING -> Icons.Default.Search to Indigo400 to "Searching"
        EventType.FOUND -> Icons.Default.CheckCircle to Emerald400 to "Found"
        EventType.READING -> Icons.Default.Link to Blue400 to "Reading"
        EventType.SENDING -> Icons.Default.Send to Orange400 to "Sending"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                color = color,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = event.message,
                color = Gray400,
                fontSize = 12.sp,
                maxLines = 2,
            )
        }
        Text(
            text = event.timestamp,
            color = Gray600,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun StatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = Gray100,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = Gray500,
            fontSize = 10.sp,
        )
    }
}

private fun formatChars(chars: Int): String {
    return when {
        chars >= 1_000_000 -> "${chars / 1_000_000}M"
        chars >= 1_000 -> "${chars / 1_000}k"
        else -> chars.toString()
    }
}
