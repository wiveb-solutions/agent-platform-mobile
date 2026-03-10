package com.wiveb.agentplatform.ui.agents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.wiveb.agentplatform.data.model.Agent
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.navigation.LocalAppNavigationState
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentsScreen() {
    val model = koinInject<AgentsScreenModel>()
    val navState = LocalAppNavigationState.current
    val state by model.state.collectAsState()
    val expanded by model.expanded.collectAsState()
    val searchQuery by model.searchQuery.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    // Reset agent detail when leaving AgentsTab
    DisposableEffect(Unit) {
        onDispose {
            navState.agentDetailId = null
            navState.agentDetailName = null
        }
    }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    // Show agent detail if selected
    val selectedAgentId = navState.agentDetailId
    if (selectedAgentId != null && state is UiState.Success) {
        val agents = (state as UiState.Success).data
        val agent = agents.find { it.id == selectedAgentId }
        if (agent != null) {
            AgentDetailView(agent = agent)
            return
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            model.load()
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { model.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = {
                    Text("Search agents...", color = Gray500)
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, null, tint = Gray500)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { model.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, null, tint = Gray500)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Indigo600,
                    unfocusedBorderColor = Gray700,
                    focusedTextColor = Gray100,
                    unfocusedTextColor = Gray100,
                    cursorColor = Indigo400,
                ),
                singleLine = true,
            )

            when (val s = state) {
                is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
                is UiState.Error -> ErrorCard(s.message, onRetry = { model.load() })
                is UiState.Success -> {
                    val filteredAgents = if (searchQuery.isBlank()) {
                        s.data
                    } else {
                        s.data.filter {
                            it.id.contains(searchQuery, ignoreCase = true) ||
                            it.name.contains(searchQuery, ignoreCase = true) ||
                            it.role.contains(searchQuery, ignoreCase = true)
                        }
                    }

                    if (filteredAgents.isEmpty()) {
                        EmptyState(
                            if (searchQuery.isBlank()) "No agents found" else "No agents match your search",
                            Modifier.fillMaxSize()
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            items(filteredAgents) { agent ->
                                AgentCard(
                                    agent = agent,
                                    isExpanded = agent.id in expanded,
                                    onToggle = { model.toggleExpanded(agent.id) },
                                    onViewDetails = {
                                        navState.agentDetailId = agent.id
                                        navState.agentDetailName = agent.name
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentCard(
    agent: Agent,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onViewDetails: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onViewDetails),
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
                    Icons.Default.ChevronRight,
                    contentDescription = "View details",
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
                    agent.recentTools.take(3).forEach { tool ->
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
                    if (agent.recentTools.size > 3) {
                        Surface(
                            color = Gray800,
                            shape = RoundedCornerShape(4.dp),
                        ) {
                            Text(
                                "+${agent.recentTools.size - 3}",
                                color = Gray500,
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
        }
    }
}

@Composable
private fun AgentDetailView(agent: Agent) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
            // Status section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray800),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Status", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusDot(agent.status, size = 12)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                agent.status.capitalize(),
                                color = getStatusColor(agent.status),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                        agent.lastAction?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Last action: $it",
                                color = Gray500,
                                fontSize = 13.sp,
                            )
                        }
                        agent.lastActivity?.let {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Last activity: ${formatTimeAgo(it)}",
                                color = Gray500,
                                fontSize = 13.sp,
                            )
                        }
                    }
                }
            }

            // Current session
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray800),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Current Session", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.Chat, null, tint = Indigo400, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            agent.currentSession ?: "No active session",
                            color = if (agent.currentSession != null) Gray100 else Gray500,
                            fontSize = 14.sp,
                        )
                    }
                }
            }

            // Token usage
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray800),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Token Usage", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.DataUsage, null, tint = Violet400, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        if (agent.tokenUsage != null) {
                            DetailRow("Input tokens", formatTokens(agent.tokenUsage.input))
                            DetailRow("Output tokens", formatTokens(agent.tokenUsage.output))
                            DetailRow("Total tokens", formatTokens(agent.tokenUsage.input + agent.tokenUsage.output))
                        } else {
                            Text("No usage data available", color = Gray500, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Context window
            item {
                if (agent.contextTokens != null && agent.contextWindow != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Gray800),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Context Window", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Spacer(Modifier.height(8.dp))
                            ContextBar(agent.contextTokens, agent.contextWindow)
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text("${formatTokens(agent.contextTokens)} used", color = Gray500, fontSize = 11.sp)
                                Text("/ ${formatTokens(agent.contextWindow)} max", color = Gray500, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Recent tools
            item {
                if (!agent.recentTools.isNullOrEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Gray800),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text("Recent Tools", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Default.Build, null, tint = Amber400, modifier = Modifier.size(16.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                agent.recentTools.forEach { tool ->
                                    Surface(
                                        color = Gray900,
                                        shape = RoundedCornerShape(6.dp),
                                    ) {
                                        Text(
                                            tool,
                                            color = Gray300,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = Gray500, fontSize = 12.sp)
        Text(value, color = Gray300, fontSize = 12.sp, fontWeight = FontWeight.Medium)
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

private fun getStatusColor(status: String): Color {
    return when (status) {
        "active", "ok", "up" -> Emerald400
        "idle" -> Amber400
        else -> Gray500
    }
}

private fun String.capitalize(): String {
    return if (this.isEmpty()) this else this[0].uppercase() + substring(1)
}
