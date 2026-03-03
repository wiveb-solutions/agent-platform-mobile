package com.wiveb.agentplatform.ui.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.navigation.AgentsTab
import com.wiveb.agentplatform.ui.navigation.BoardTab
import com.wiveb.agentplatform.ui.navigation.ChatTab
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val model = koinInject<DashboardScreenModel>()
    val state by model.state.collectAsState()
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
            is UiState.Success -> DashboardContent(s.data)
        }
    }
}

@Composable
private fun DashboardContent(data: DashboardData) {
    val tabNavigator = LocalTabNavigator.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Health row
        item {
            SectionHeader("SERVICES")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                HealthDot("OpenClaw", data.health.openclaw.status)
                HealthDot("vLLM", data.health.vllm.status)
                HealthDot("Plane", data.health.plane.status)
            }
        }

        // Sprint progress
        item {
            val total = data.burndown.totalStories
            val done = data.burndown.completedStories
            val pct = if (total > 0) (done * 100 / total) else 0

            Card(
                colors = CardDefaults.cardColors(containerColor = Gray900),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("Sprint Progress", color = Gray100, fontWeight = FontWeight.SemiBold)
                        Text("$done/$total ($pct%)", color = Indigo400, fontSize = 13.sp)
                    }
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (total > 0) done.toFloat() / total else 0f },
                        color = Indigo600,
                        trackColor = Gray800,
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    )
                    if (data.burndown.velocity > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Velocity: %.1f stories/week".format(data.burndown.velocity),
                            color = Gray400,
                            fontSize = 11.sp,
                        )
                    }
                }
            }
        }

        // KPI cards — 2x2 grid
        item {
            SectionHeader("KEY METRICS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                KpiCard(
                    label = "Agents Online",
                    value = data.agents.count { it.status == "active" }.toString(),
                    total = "/${data.agents.size}",
                    color = Emerald400,
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    label = "Tasks",
                    value = data.tasks.count { it.status == "running" || it.status == "pending" }.toString(),
                    total = " active",
                    color = Amber400,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Quick actions
        item {
            SectionHeader("QUICK ACTIONS")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickAction("Chat", Icons.Default.Chat, Modifier.weight(1f)) {
                    tabNavigator.current = ChatTab
                }
                QuickAction("Board", Icons.Default.ViewColumn, Modifier.weight(1f)) {
                    tabNavigator.current = BoardTab
                }
                QuickAction("Agents", Icons.Default.People, Modifier.weight(1f)) {
                    tabNavigator.current = AgentsTab
                }
            }
        }

        // Recent tasks
        if (data.tasks.isNotEmpty()) {
            item { SectionHeader("RECENT TASKS") }
            items(data.tasks.take(5)) { task ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray900),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AgentBadge(task.agent)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = task.prompt.take(60),
                            color = Gray100,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                        )
                        Spacer(Modifier.width(8.dp))
                        StatusBadge(task.status)
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthDot(label: String, status: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        StatusDot(status = if (status == "ok" || status == "up") "active" else "offline", size = 12)
        Spacer(Modifier.height(4.dp))
        Text(label, color = Gray400, fontSize = 11.sp)
    }
}

@Composable
private fun KpiCard(
    label: String,
    value: String,
    total: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = modifier,
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(label, color = Gray400, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(total, color = Gray500, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(icon, contentDescription = label, tint = Indigo400, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = Gray100, fontSize = 12.sp)
        }
    }
}
