package com.wiveb.agentplatform.ui.tasks

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.wiveb.agentplatform.data.model.AgentTask
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.navigation.LocalAppNavigationState
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen() {
    val model = koinInject<TasksScreenModel>()
    val navState = LocalAppNavigationState.current
    val state by model.state.collectAsState()
    val currentFilter by model.filter.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Reset any detail view when leaving TasksTab
    DisposableEffect(Unit) {
        onDispose {
            navState.chatDetailSessionKey = null
            navState.agentDetailId = null
            navState.agentDetailName = null
        }
    }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    LaunchedEffect(model.lastCreateResult) {
        val result = model.lastCreateResult.value
        if (result != null) {
            if (result.isSuccess) {
                model.load()
            } else {
                snackbarHostState.showSnackbar(result.exceptionOrNull()?.message ?: "Failed to create task")
            }
            model.clearLastCreateResult()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = Indigo600,
                contentColor = Color.White,
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create task")
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                model.load()
            },
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                // Filter chips
                FilterChips(
                    currentFilter = currentFilter,
                    onAgentFilterChange = { model.setAgentFilter(it) },
                    onStatusFilterChange = { model.setStatusFilter(it) },
                    onClearFilters = { model.clearFilters() },
                )

                when (val s = state) {
                    is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
                    is UiState.Error -> ErrorCard(s.message, onRetry = { model.load() })
                    is UiState.Success -> {
                        val tasks = s.data.filteredTasks
                        if (tasks.isEmpty()) {
                            EmptyState(
                                if (currentFilter.agent != "All" || currentFilter.status != "All")
                                    "No tasks match your filters"
                                else
                                    "No tasks found",
                                Modifier.fillMaxSize()
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(tasks) { task ->
                                    TaskCard(task = task)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateTaskDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { selectedAgent, prompt ->
                model.createTask(selectedAgent, prompt)
                showCreateDialog = false
            },
        )
    }
}

@Composable
private fun FilterChips(
    currentFilter: TasksFilter,
    onAgentFilterChange: (String) -> Unit,
    onStatusFilterChange: (String) -> Unit,
    onClearFilters: () -> Unit,
) {
    val agentOptions = listOf("All", "main", "dev", "pm", "qa", "notaire")
    val statusOptions = listOf("All", "pending", "running", "done", "error")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Agent filter
        Text(
            text = "Agent",
            color = Gray400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            agentOptions.forEach { agent ->
                FilterChip(
                    selected = currentFilter.agent == agent,
                    onClick = { onAgentFilterChange(agent) },
                    label = agent.uppercase(),
                )
            }
        }

        // Status filter
        Text(
            text = "Status",
            color = Gray400,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            statusOptions.forEach { status ->
                FilterChip(
                    selected = currentFilter.status == status,
                    onClick = { onStatusFilterChange(status) },
                    label = status.replaceFirstChar { it.uppercase() },
                )
            }
        }

        // Clear filters button
        if (currentFilter.agent != "All" || currentFilter.status != "All") {
            TextButton(
                onClick = onClearFilters,
                modifier = Modifier.align(Alignment.Start)
            ) {
                Icon(
                    Icons.Default.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = Indigo400,
                )
                Spacer(Modifier.width(4.dp))
                Text("Clear filters", color = Indigo400, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Indigo600.copy(alpha = 0.2f),
            selectedLabelColor = Indigo400,
            containerColor = Gray800,
            labelColor = Gray400,
        ),
        shape = RoundedCornerShape(8.dp),
    )
}

@Composable
private fun TaskCard(task: AgentTask) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header row with badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AgentBadge(task.agent)
                Spacer(Modifier.width(8.dp))
                StatusBadge(task.status)
            }

            // Task message/prompt
            Spacer(Modifier.height(10.dp))
            Text(
                text = task.prompt,
                color = Gray100,
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )

            // Timestamp
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = formatTimestamp(task.createdAt),
                    color = Gray500,
                    fontSize = 11.sp,
                )
            }

            // Result or error
            if (task.status == "done" && task.result != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Emerald400.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Emerald400,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "Completed successfully",
                            color = Emerald400,
                            fontSize = 11.sp,
                        )
                    }
                }
            } else if (task.status == "error" && task.error != null) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Red400.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(6.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = Red400,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = task.error.take(50) + if (task.error!!.length > 50) "..." else "",
                            color = Red400,
                            fontSize = 11.sp,
                        )
                    }
                }
            }

            // Duration if available
            if (task.duration != null && task.duration > 0) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Timer,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formatDuration(task.duration),
                        color = Gray500,
                        fontSize = 11.sp,
                    )
                }
            }
        }
    }
}

private fun formatTimestamp(iso: String?): String {
    return try {
        if (iso == null) return "Unknown time"
        val ts = kotlinx.datetime.Instant.parse(iso)
        val now = kotlinx.datetime.Clock.System.now()
        val diff = now - ts
        when {
            diff.inWholeMinutes < 1 -> "Just now"
            diff.inWholeMinutes < 60 -> "${diff.inWholeMinutes}m ago"
            diff.inWholeHours < 24 -> "${diff.inWholeHours}h ago"
            diff.inWholeDays < 7 -> "${diff.inWholeDays}d ago"
            else -> {
                // Fallback: just show the ISO string truncated
                iso.take(10) // YYYY-MM-DD
            }
        }
    } catch (_: Exception) {
        iso?.toString() ?: "Unknown time"
    }
}

private fun formatDuration(ms: Long): String {
    return when {
        ms < 1000 -> "${ms}ms"
        ms < 60000 -> "${ms / 1000}s"
        ms < 3600000 -> "${ms / 60000}m ${ms % 60000 / 1000}s"
        else -> "${ms / 3600000}h ${ms % 3600000 / 60000}m"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateTaskDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var selectedAgent by remember { mutableStateOf("main") }
    var prompt by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Task", color = Gray100, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Agent selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Agent",
                        color = Gray400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                    ) {
                        OutlinedTextField(
                            value = selectedAgent.uppercase(),
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Indigo600,
                                unfocusedBorderColor = Gray700,
                                focusedTextColor = Gray100,
                                unfocusedTextColor = Gray100,
                            ),
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                        ) {
                            listOf("main", "dev", "pm", "qa", "notaire").forEach { agent ->
                                DropdownMenuItem(
                                    text = { Text(agent.uppercase(), color = Gray100) },
                                    onClick = {
                                        selectedAgent = agent
                                        expanded = false
                                    },
                                )
                            }
                        }
                    }
                }

                // Prompt text field
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Task Prompt",
                        color = Gray400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        value = prompt,
                        onValueChange = { prompt = it },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 6,
                        placeholder = { Text("Enter your task prompt...", color = Gray500) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Indigo600,
                            unfocusedBorderColor = Gray700,
                            focusedTextColor = Gray100,
                            unfocusedTextColor = Gray100,
                        ),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (prompt.isNotBlank()) onCreate(selectedAgent, prompt.trim()) },
                enabled = prompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
            ) {
                Text("Create", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Gray400)
            }
        },
        containerColor = Gray800,
    )
}
