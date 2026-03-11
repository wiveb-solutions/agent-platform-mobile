package com.wiveb.agentplatform.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.ui.theme.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Event representing a step in the Deep Search process
 */
data class DeepSearchEvent(
    val id: String,
    val type: EventType,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap(),
) {
    companion object {
        fun generateId(): String = "${System.currentTimeMillis()}-${(System.nanoTime() and Long.MAX_VALUE).toString(16).substring(0, 8)}"
    }
}

enum class EventType(
    val icon: (@Composable () -> Unit),
    val color: Color,
    val label: String
) {
    START(
        icon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Violet400,
        label = "Started"
    ),
    QUERY(
        icon = { Icon(Icons.Default.QueryStats, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Blue400,
        label = "Query"
    ),
    SOURCE(
        icon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Green400,
        label = "Source"
    ),
    DISCOVERY(
        icon = { Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Yellow400,
        label = "Discovery"
    ),
    ANALYSIS(
        icon = { Icon(Icons.Default.Analytics, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Indigo400,
        label = "Analysis"
    ),
    COMPLETING(
        icon = { Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Emerald400,
        label = "Completing"
    ),
    COMPLETE(
        icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Emerald400,
        label = "Complete"
    ),
    ERROR(
        icon = { Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(14.dp)) },
        color = Red400,
        label = "Error"
    ),
}

/**
 * State for the Deep Search progress panel
 */
data class DeepSearchProgressState(
    val isActive: Boolean = false,
    val events: List<DeepSearchEvent> = emptyList(),
    val sourcesFound: Int = 0,
    val elapsedSeconds: Long = 0,
    val stepsCompleted: Int = 0,
    val totalSteps: Int = 0,
    val errorMessage: String? = null,
)

/**
 * Panel displayed during Deep Search execution showing progress, timeline, and stats
 */
@Composable
fun DeepSearchProgressPanel(
    state: DeepSearchProgressState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new events are added
    LaunchedEffect(state.events.size) {
        if (state.events.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(state.events.size - 1)
            }
        }
    }

    // Infinite repeatable animation for the progress indicator
    val infiniteTransition = rememberInfiniteTransition(label = "progressAnimation")
    val progressAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Gray950),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            // Header - Purple surface
            Surface(
                color = Violet900,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        // Animated spinner
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(24.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.5.dp,
                                color = Violet400,
                            )
                        }
                        Column {
                            Text(
                                text = "Deep Search in progress...",
                                color = Gray100,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Gathering insights from multiple sources",
                                color = Violet400,
                                fontSize = 12.sp,
                            )
                        }
                    }

                    // Dismiss button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray400,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Stats Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Sources Found
                StatCard(
                    icon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    value = state.sourcesFound.toString(),
                    label = "Sources",
                    color = Green400,
                    modifier = Modifier.weight(1f),
                )

                // Time Elapsed
                StatCard(
                    icon = { Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    value = formatElapsedTime(state.elapsedSeconds),
                    label = "Duration",
                    color = Blue400,
                    modifier = Modifier.weight(1f),
                )

                // Steps Completed
                StatCard(
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    value = "${state.stepsCompleted}/${state.totalSteps}",
                    label = "Steps",
                    color = Indigo400,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(16.dp))

            // Progress Bar
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Progress",
                        color = Gray400,
                        fontSize = 12.sp,
                    )
                    Text(
                        text = "${if (state.totalSteps > 0) (state.stepsCompleted * 100 / state.totalSteps) else 0}%",
                        color = Gray400,
                        fontSize = 12.sp,
                    )
                }
                LinearProgressIndicator(
                    progress = if (state.totalSteps > 0) state.stepsCompleted / state.totalSteps.toFloat() else 0f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = Violet400,
                    trackColor = Gray800,
                )
            }

            Spacer(Modifier.height(16.dp))

            // Timeline - Vertical list of events
            Surface(
                color = Gray900,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                ) {
                    Text(
                        text = "Timeline",
                        color = Gray400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(12.dp))

                    if (state.events.isEmpty()) {
                        // Empty state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(32.dp),
                                    strokeWidth = 2.5.dp,
                                    color = Violet400.copy(alpha = 0.5f),
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Initializing Deep Search...",
                                    color = Gray500,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    } else {
                        // Timeline events
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.events, key = { it.id }) { event ->
                                TimelineEventItem(event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: @Composable () -> Unit,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Gray900,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                icon()
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = value,
                color = Gray100,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                color = Gray500,
                fontSize = 10.sp,
            )
        }
    }
}

@Composable
private fun TimelineEventItem(event: DeepSearchEvent) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        // Icon with colored background
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(event.type.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            event.type.icon()
        }

        Spacer(Modifier.width(10.dp))

        // Event details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 2.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = event.type.label,
                    color = event.type.color,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = formatTimestamp(event.timestamp),
                    color = Gray600,
                    fontSize = 10.sp,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = event.description,
                color = Gray300,
                fontSize = 12.sp,
            )
            
            // Metadata if available
            if (event.metadata.isNotEmpty()) {
                Spacer(Modifier.height(4.dp))
                event.metadata.forEach { (key, value) ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "$key:",
                            color = Gray500,
                            fontSize = 10.sp,
                        )
                        Text(
                            text = value,
                            color = Gray400,
                            fontSize = 10.sp,
                        )
                    }
                }
            }
        }
    }
}

private fun formatElapsedTime(seconds: Long): String {
    return if (seconds < 60) {
        "${seconds}s"
    } else if (seconds < 3600) {
        val mins = seconds / 60
        val secs = seconds % 60
        "${mins}m ${secs}s"
    } else {
        val hours = seconds / 3600
        val mins = (seconds % 3600) / 60
        "${hours}h ${mins}m"
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return if (diff < 1000) {
        "Just now"
    } else if (diff < 60000) {
        "${diff / 1000}s ago"
    } else if (diff < 3600000) {
        "${diff / 60000}m ago"
    } else {
        val instant = Instant.ofEpochMilli(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    }
}
