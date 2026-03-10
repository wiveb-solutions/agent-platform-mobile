package com.wiveb.agentplatform.ui.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
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
    var selectedFilter by remember { mutableStateOf<String>("All") }

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
                BoardContent(
                    columns = s.data,
                    selectedFilter = selectedFilter,
                    onFilterSelected = { selectedFilter = it },
                    onItemClick = { selectedItem = it }
                )
            }
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
private fun BoardContent(
    columns: List<BoardColumn>,
    selectedFilter: String,
    onFilterSelected: (String) -> Unit,
    onItemClick: (WorkItem) -> Unit
) {
    // Flatten all items from columns
    val allItems = columns.flatMap { it.items }
    val filteredItems = if (selectedFilter == "All") {
        allItems
    } else {
        allItems.filter { item ->
            columns.find { col -> col.items.contains(item) }?.state == selectedFilter
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Filter chips
        LazyRow(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val filters = listOf("All", "Backlog", "Todo", "In Progress", "Done")
            items(filters) { filter ->
                FilterChip(
                    filter = filter,
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) }
                )
            }
        }

        // List view
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(filteredItems) { item ->
                WorkItemCard(item, onClick = { onItemClick(item) })
            }
            if (filteredItems.isEmpty()) {
                item {
                    EmptyState("No items in this filter")
                }
            }
        }
    }
}

@Composable
private fun FilterChip(
    filter: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Indigo600 else Gray800
    val textColor = if (isSelected) Gray100 else Gray400

    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = filter,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = backgroundColor,
            labelColor = textColor,
            selectedContainerColor = Indigo600,
            selectedLabelColor = Gray100,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkItemCard(item: WorkItem, onClick: () -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Gray800),
        modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded },
    ) {
        Column(Modifier.padding(12.dp)) {
            // Header row: identifier + priority
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                // Identifier badge
                Surface(
                    color = Gray700,
                    shape = RoundedCornerShape(4.dp),
                ) {
                    Text(
                        item.identifier,
                        color = Gray400,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    )
                }
                Spacer(Modifier.width(8.dp))
                PriorityBadge(item.priority)
                Spacer(Modifier.weight(1f))
                // Expand/collapse icon
                Icon(
                    if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = Gray500,
                    modifier = Modifier.size(16.dp),
                )
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                item.title,
                color = Gray100,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
            )

            // Assignee and labels
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                // Assignee
                item.assignee?.let { assignee ->
                    AgentBadge(assignee)
                }
                // Labels (agent: prefixed)
                item.labels.filter { it.startsWith("agent:") }.forEach { label ->
                    val agentId = label.removePrefix("agent:")
                    AgentBadge(agentId)
                }
            }

            // Expanded details
            if (isExpanded) {
                HorizontalDivider(color = Gray700, modifier = Modifier.padding(vertical = 12.dp))

                // Description
                item.description?.let { description ->
                    Text(
                        text = "Description",
                        color = Gray400,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = description,
                        color = Gray300,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // Created date
                item.createdAt?.let { createdAt ->
                    Text(
                        text = "Created: $createdAt",
                        color = Gray500,
                        fontSize = 11.sp,
                    )
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
