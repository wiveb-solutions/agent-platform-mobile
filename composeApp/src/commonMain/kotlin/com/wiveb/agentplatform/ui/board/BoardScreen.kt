package com.wiveb.agentplatform.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.wiveb.agentplatform.data.model.BoardColumn
import com.wiveb.agentplatform.data.model.WorkItem
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen() {
    val model = koinInject<BoardScreenModel>()
    val state by model.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<WorkItem?>(null) }

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
            is UiState.Success -> BoardContent(s.data, onItemClick = { selectedItem = it })
        }
    }

    // Bottom sheet for item actions
    selectedItem?.let { item ->
        ItemActionSheet(
            item = item,
            onDismiss = { selectedItem = null },
            onMove = { state ->
                model.moveItem(item.id, state)
                selectedItem = null
            },
            onAssign = { agent ->
                model.assignAgent(item.id, agent)
                selectedItem = null
            },
        )
    }
}

@Composable
private fun BoardContent(columns: List<BoardColumn>, onItemClick: (WorkItem) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(columns.filter { it.state != "Cancelled" }) { column ->
            BoardColumnView(column, onItemClick)
        }
    }
}

@Composable
private fun BoardColumnView(column: BoardColumn, onItemClick: (WorkItem) -> Unit) {
    val headerColor = when (column.state) {
        "Backlog" -> Gray600
        "Todo" -> Blue600
        "In Progress" -> Amber400
        "Done" -> Emerald400
        else -> Gray600
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = Modifier.width(280.dp).fillMaxHeight(),
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerColor.copy(alpha = 0.15f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    column.state,
                    color = headerColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
                Surface(
                    color = headerColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(10.dp),
                ) {
                    Text(
                        "${column.items.size}",
                        color = headerColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
            }

            // Items
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(column.items) { item ->
                    WorkItemCard(item, onClick = { onItemClick(item) })
                }
                if (column.items.isEmpty()) {
                    item {
                        Text(
                            "No items",
                            color = Gray600,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkItemCard(item: WorkItem, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray800),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Column(Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.identifier, color = Gray500, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.width(6.dp))
                PriorityBadge(item.priority)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                item.title,
                color = Gray100,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (item.labels.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    item.labels.forEach { label ->
                        val agentId = label.removePrefix("agent:")
                        if (label.startsWith("agent:")) {
                            AgentBadge(agentId)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemActionSheet(
    item: WorkItem,
    onDismiss: () -> Unit,
    onMove: (String) -> Unit,
    onAssign: (String?) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Gray900,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        ) {
            Text(item.identifier, color = Gray500, fontSize = 12.sp)
            Text(item.title, color = Gray100, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)

            Spacer(Modifier.height(16.dp))
            Text("Move to...", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            val states = listOf("Backlog", "Todo", "In Progress", "Done")
            states.forEach { state ->
                Surface(
                    onClick = { onMove(state) },
                    color = Gray800,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    Text(
                        state,
                        color = Gray100,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Assign agent...", color = Gray400, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            val agents = listOf("dev" to "Dev", "pm" to "PM", "qa" to "QA")
            agents.forEach { (id, name) ->
                Surface(
                    onClick = { onAssign(id) },
                    color = Gray800,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AgentBadge(id)
                        Spacer(Modifier.width(8.dp))
                        Text(name, color = Gray100)
                    }
                }
            }

            Surface(
                onClick = { onAssign(null) },
                color = Gray800,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            ) {
                Text("Remove agent", color = Gray400, modifier = Modifier.padding(12.dp))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
