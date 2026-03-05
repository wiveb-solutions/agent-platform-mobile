package com.wiveb.agentplatform.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
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
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*
import org.koin.compose.koinInject
import kotlinx.coroutines.launch

@Composable
fun ChatScreen() {
    var selectedSession by remember { mutableStateOf<String?>(null) }
    var showNewConversationDialog by remember { mutableStateOf(false) }
    val api = koinInject<AgentPlatformApi>()
    val scope = rememberCoroutineScope()
    val agentsState by api.agentsState.collectAsState()
    val agents = (agentsState as? UiState.Success)?.data ?: emptyList()

    // Handle new conversation creation
    val onCreateConversation: (String, String) -> Unit = remember {
        { agentId, initialMessage ->
            scope.launch {
                val title = if (initialMessage.isNotBlank()) initialMessage.take(30) else "New conversation"
                try {
                    api.createSession(agentId, title, if (initialMessage.isNotBlank()) initialMessage else null)
                    showNewConversationDialog = false
                } catch (e: Exception) {
                    // Error handled by API
                }
            }
        }
    }

    if (selectedSession != null) {
        ChatDetailView(
            sessionKey = selectedSession!!,
            onBack = { selectedSession = null },
        )
    } else {
        ChatListView(
            onSelectSession = { selectedSession = it },
            onNewConversation = { showNewConversationDialog = true },
        )
    }

    if (showNewConversationDialog) {
        NewConversationDialog(
            agents = agents,
            onDismiss = { showNewConversationDialog = false },
            onCreateConversation = onCreateConversation,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatListView(
    onSelectSession: (String) -> Unit,
    onNewConversation: () -> Unit,
) {
    val model = koinInject<ChatListScreenModel>()
    val state by model.state.collectAsState()
    val filter by model.filter.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    val agents = listOf(null, "main", "dev", "pm", "qa", "notaire")

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize()) {
            // Filter chips
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(agents) { agentId ->
                    FilterChip(
                        selected = filter == agentId,
                        onClick = { model.setFilter(agentId) },
                        label = { Text(agentId?.uppercase() ?: "All", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (agentId != null) AgentColors.badge(agentId) else Indigo600,
                            selectedLabelColor = Gray100,
                            containerColor = Gray800,
                            labelColor = Gray400,
                        ),
                    )
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = {
                    isRefreshing = true
                    model.load()
                },
                modifier = Modifier.fillMaxSize(),
            ) {
                when (val s = state) {
                    is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
                    is UiState.Error -> ErrorCard(s.message, onRetry = { model.load() })
                    is UiState.Success -> {
                        if (s.data.isEmpty()) {
                            EmptyState("No sessions found")
                        } else {
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(s.data) { session ->
                                    SessionItem(session, onClick = { onSelectSession(session.key) })
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAB for new conversation
        FloatingActionButton(
            onClick = onNewConversation,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = Indigo600,
        ) {
            Icon(Icons.Default.Add, contentDescription = "New Conversation", tint = Gray100)
        }
    }
}

@Composable
private fun SessionItem(
    session: com.wiveb.agentplatform.data.model.ChatSession,
    onClick: () -> Unit,
) {
    val agentId = session.agentId ?: session.key.split(":").getOrNull(1) ?: "main"

    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AgentBadge(agentId)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = session.title?.takeIf { it.isNotEmpty() } ?: session.key.substringAfterLast(":").take(20),
                    color = Gray100,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!session.preview.isNullOrBlank()) {
                    Text(
                        text = session.preview,
                        color = Gray500,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatDetailView(sessionKey: String, onBack: () -> Unit) {
    val api = koinInject<AgentPlatformApi>()
    val model = remember(sessionKey) { ChatDetailScreenModel(api, sessionKey) }
    val messagesState by model.messages.collectAsState()
    val sending by model.sending.collectAsState()
    val thinking by model.thinking.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var showThinkingMenu by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom on new messages
    LaunchedEffect(messagesState) {
        val msgs = (messagesState as? UiState.Success)?.data ?: return@LaunchedEffect
        if (msgs.isNotEmpty()) {
            listState.animateScrollToItem(msgs.size - 1)
        }
    }

    Column(Modifier.fillMaxSize()) {
        // Top bar
        TopAppBar(
            title = {
                Text(
                    sessionKey.substringAfterLast(":").take(20),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Gray900,
                titleContentColor = Gray100,
                navigationIconContentColor = Gray100,
            ),
        )

        // Messages
        Box(Modifier.weight(1f)) {
            when (val s = messagesState) {
                is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
                is UiState.Error -> ErrorCard(s.message, onRetry = { model.loadMessages() })
                is UiState.Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(s.data) { msg ->
                            MessageBubble(msg)
                        }
                        if (sending) {
                            item {
                                Row(Modifier.padding(8.dp)) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Indigo400,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Agent is thinking...", color = Gray400, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Input bar
        Surface(
            color = Gray900,
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Thinking level button
                Box {
                    IconButton(onClick = { showThinkingMenu = true }) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = "Thinking",
                            tint = when (thinking) {
                                "off" -> Gray500
                                "low" -> Blue400
                                "medium" -> Indigo400
                                "high" -> Purple400
                                else -> Gray500
                            },
                        )
                    }
                    DropdownMenu(
                        expanded = showThinkingMenu,
                        onDismissRequest = { showThinkingMenu = false },
                    ) {
                        listOf("off", "low", "medium", "high").forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level.replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    model.setThinking(level)
                                    showThinkingMenu = false
                                },
                                trailingIcon = {
                                    if (thinking == level) Icon(Icons.Default.Check, null, tint = Indigo400)
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Message...", color = Gray500) },
                    modifier = Modifier.weight(1f),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Gray100,
                        unfocusedTextColor = Gray100,
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Gray700,
                        cursorColor = Indigo400,
                    ),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                )

                Spacer(Modifier.width(4.dp))

                if (sending) {
                    IconButton(onClick = { model.abort() }) {
                        Icon(Icons.Default.Stop, "Stop", tint = Red400)
                    }
                } else {
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                model.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Send",
                            tint = if (inputText.isNotBlank()) Indigo400 else Gray600,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(msg: com.wiveb.agentplatform.data.model.ChatMessage) {
    val isUser = msg.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            color = if (isUser) Indigo600 else Gray800,
            shape = RoundedCornerShape(
                topStart = 12.dp,
                topEnd = 12.dp,
                bottomStart = if (isUser) 12.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 12.dp,
            ),
            modifier = Modifier.widthIn(max = 300.dp),
        ) {
            if (isUser) {
                Text(
                    text = msg.content,
                    color = Gray100,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(10.dp),
                )
            } else {
                val content = msg.content
                if (content.isNotEmpty()) {
                    Markdown(
                        content = content,
                        colors = markdownColor(text = Gray100),
                        modifier = Modifier.padding(10.dp),
                    )
                } else {
                    Text(
                        text = "[${msg.role}]",
                        color = Gray500,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(10.dp),
                    )
                }
            }
        }
    }
}
