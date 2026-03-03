package com.wiveb.agentplatform.ui.activity

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.koin.koinScreenModel
import com.wiveb.agentplatform.data.model.ActivityEvent
import com.wiveb.agentplatform.data.model.AgentStatus
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen() {
    val model = koinScreenModel<ActivityScreenModel>()
    val state by model.state.collectAsState()
    val filterAgent by model.filterAgent.collectAsState()
    val filterType by model.filterType.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            model.load()
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            when (val s = state) {
                is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
                is UiState.Error -> ErrorCard(s.message, onRetry = { model.load() })
                is UiState.Success -> {
                    // Agent status bar
                    if (s.data.agentStatuses.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            s.data.agentStatuses.forEach { agent ->
                                AgentStatusPill(agent)
                            }
                        }
                    }

                    // Filter chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val agents = listOf(null, "main", "dev", "pm", "qa", "notaire")
                        items(agents) { agentId ->
                            FilterChip(
                                selected = filterAgent == agentId,
                                onClick = { model.setFilterAgent(agentId) },
                                label = { Text(agentId?.uppercase() ?: "All", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (agentId != null) AgentColors.badge(agentId) else Indigo600,
                                    selectedLabelColor = Gray100,
                                    containerColor = Gray800,
                                    labelColor = Gray400,
                                ),
                            )
                        }
                    }

                    // Type filter
                    LazyRow(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val types = listOf(null to "All", "tool" to "Tools", "delegation" to "Delegations", "session" to "Sessions", "ci" to "CI")
                        items(types) { (type, label) ->
                            FilterChip(
                                selected = filterType == type,
                                onClick = { model.setFilterType(type) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Indigo600,
                                    selectedLabelColor = Gray100,
                                    containerColor = Gray800,
                                    labelColor = Gray400,
                                ),
                            )
                        }
                    }

                    // Event list
                    if (s.data.events.isEmpty()) {
                        EmptyState("No activity events")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        ) {
                            items(s.data.events) { event ->
                                EventItem(event)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentStatusPill(agent: AgentStatus) {
    Surface(
        color = Gray800,
        shape = RoundedCornerShape(16.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(agent.status, size = 6)
            Spacer(Modifier.width(6.dp))
            Text(
                agent.id.uppercase(),
                color = AgentColors.text(agent.id),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            if (agent.action != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    agent.action,
                    color = Gray500,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 80.dp),
                )
            }
        }
    }
}

@Composable
private fun EventItem(event: ActivityEvent) {
    val icon = when {
        event.type.contains("tool", ignoreCase = true) -> Icons.Default.Build
        event.type.contains("delegation", ignoreCase = true) -> Icons.Default.Share
        event.type.contains("session", ignoreCase = true) -> Icons.Default.Chat
        event.type.contains("ci", ignoreCase = true) -> Icons.Default.PlayArrow
        else -> Icons.Default.Info
    }

    val time = event.timestamp.substringAfter("T").take(5) // HH:mm

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(time, color = Gray500, fontSize = 11.sp, modifier = Modifier.width(40.dp))
        Spacer(Modifier.width(4.dp))
        Icon(icon, null, tint = Gray500, modifier = Modifier.size(14.dp).padding(top = 2.dp))
        Spacer(Modifier.width(6.dp))
        AgentBadge(event.agentId)
        Spacer(Modifier.width(6.dp))
        Text(
            event.summary,
            color = Gray300,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
