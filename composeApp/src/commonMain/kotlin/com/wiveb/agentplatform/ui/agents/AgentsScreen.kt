package com.wiveb.agentplatform.ui.agents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.koin.koinScreenModel
import com.wiveb.agentplatform.data.model.Agent
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen() {
    val model = koinScreenModel<AgentsScreenModel>()
    val state by model.state.collectAsState()
    val expanded by model.expanded.collectAsState()
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
        when (val s = state) {
            is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
            is UiState.Error -> ErrorCard(s.message, onRetry = { model.load() })
            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(s.data) { agent ->
                        AgentCard(
                            agent = agent,
                            isExpanded = agent.id in expanded,
                            onToggle = { model.toggleExpanded(agent.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentCard(agent: Agent, isExpanded: Boolean, onToggle: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StatusDot(agent.status, size = 10)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            agent.name,
                            color = AgentColors.text(agent.id),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.width(8.dp))
                        AgentBadge(agent.id)
                    }
                    Text(agent.role, color = Gray500, fontSize = 12.sp)
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Gray500,
                )
            }

            // Last action
            Spacer(Modifier.height(8.dp))
            Text(
                text = agent.lastAction ?: "Idle",
                color = if (agent.status == "active") Emerald400 else Gray500,
                fontSize = 13.sp,
            )

            // Recent tools
            if (!agent.recentTools.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    agent.recentTools.forEach { tool ->
                        Surface(
                            color = Gray800,
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                tool,
                                color = Gray400,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            )
                        }
                    }
                }
            }

            // Context bar
            if (agent.contextTokens != null && agent.contextWindow != null) {
                Spacer(Modifier.height(8.dp))
                ContextBar(agent.contextTokens, agent.contextWindow)
            }

            // Expanded details
            AnimatedVisibility(visible = isExpanded) {
                Column(Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = Gray800)
                    Spacer(Modifier.height(8.dp))

                    if (agent.tokenUsage != null) {
                        DetailRow("Input tokens", formatTokens(agent.tokenUsage.input))
                        DetailRow("Output tokens", formatTokens(agent.tokenUsage.output))
                    }

                    if (agent.currentSession != null) {
                        DetailRow("Session", agent.currentSession.substringAfterLast(":").take(20))
                    }

                    if (agent.lastActivity != null) {
                        DetailRow("Last activity", formatTimeAgo(agent.lastActivity))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Gray500, fontSize = 12.sp)
        Text(value, color = Gray300, fontSize = 12.sp)
    }
}

private fun formatTokens(count: Int): String {
    return when {
        count >= 1_000_000 -> "%.1fM".format(count / 1_000_000.0)
        count >= 1_000 -> "%.1fK".format(count / 1_000.0)
        else -> count.toString()
    }
}

private fun formatTimeAgo(iso: String): String {
    return try {
        val ts = kotlinx.datetime.Instant.parse(iso)
        val now = kotlinx.datetime.Clock.System.now()
        val diff = now - ts
        when {
            diff.inWholeMinutes < 1 -> "just now"
            diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes}m ago"
            diff.inWholeHours < 24 -> "${diff.inWholeHours}h ago"
            else -> "${diff.inWholeDays}d ago"
        }
    } catch (_: Exception) {
        iso
    }
}
