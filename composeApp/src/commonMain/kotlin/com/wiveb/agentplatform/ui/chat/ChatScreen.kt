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
import androidx.compose.ui.platform.LocalConfiguration
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
import com.wiveb.agentplatform.ui.components.CollapsibleThinkingBlock
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*
import com.wiveb.agentplatform.ui.utils.TimeUtils
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
                    is UiState.Loading -> Box(Modifier.fillMaxSize()) { LoadingIndicator() }
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
    val timeAgo = TimeUtils.formatTimeAgo(session.updatedAt)

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
            Spacer(Modifier.width(8.dp))
            Text(
                text = timeAgo,
                color = Gray600,
                fontSize = 10.sp,
            )
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
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Context window info (placeholder - would need to be fetched from API)
    val contextTokens by remember { mutableStateOf(12500) }
    val maxContextTokens by remember { mutableStateOf(81920) }

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

        // Context bar
        Surface(color = Gray900) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = "Context",
                    tint = Gray500,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                ContextBar(
                    current = contextTokens,
                    max = maxContextTokens,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Messages
        when (val s = messagesState) {
            is UiState.Loading -> Box(Modifier.fillMaxSize()) { LoadingIndicator() }
            is UiState.Error -> Box(Modifier.fillMaxSize()) { ErrorCard(s.message, onRetry = { model.loadMessages() }) }
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

        // Thinking selector chips
        Surface(color = Gray900) {
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(
                    listOf(
                        "off" to Gray600,
                        "low" to Blue600,
                        "medium" to Indigo600,
                        "high" to Orange600,
                    )
                ) { (level, color) ->
                    FilterChip(
                        selected = thinking == level,
                        onClick = { model.setThinking(level) },
                        label = {
                            Text(
                                level.replaceFirstChar { it.uppercase() },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color,
                            selectedLabelColor = Gray100,
                            containerColor = Gray800,
                            labelColor = Gray400,
                        ),
                    )
                }
            }
        }

        // Input bar with icons
        InputBar(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank()) {
                    model.sendMessage(inputText)
                    inputText = ""
                }
            },
            onStop = { model.abort() },
            isSending = sending,
            onAttachmentClick = { /* TODO: Implement attachment */ },
            onVoiceClick = { /* TODO: Implement voice input */ },
            onWebSearchClick = { /* TODO: Implement web search */ },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun MessageBubble(msg: com.wiveb.agentplatform.data.model.ChatMessage) {
    val isUser = msg.role == "user"
    val timeAgo = TimeUtils.formatTimeAgo(msg.createdAt)
    var saved by remember { mutableStateOf(false) }
    
    // Extract blocks if available
    val blocks = msg.blocks ?: emptyList()
    val thinkingBlocks = blocks.filterIsInstance<com.wiveb.agentplatform.data.model.ThinkingBlock>()
    val textBlocks = blocks.filterIsInstance<com.wiveb.agentplatform.data.model.TextBlock>()
    
    // Fallback to content if no blocks
    val hasThinking = thinkingBlocks.isNotEmpty() || (msg.content.isNotEmpty() && blocks.isEmpty())

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        ) {
            // Thinking block (collapsible with lightbulb icon)
            if (!isUser && hasThinking) {
                val thinkingContent = if (thinkingBlocks.isNotEmpty()) {
                    thinkingBlocks.joinToString("\n\n") { it.thinking }
                } else if (msg.content.isNotEmpty() && blocks.isEmpty()) {
                    msg.content
                } else {
                    ""
                }
                
                if (thinkingContent.isNotEmpty()) {
                    CollapsibleThinkingBlock(
                        content = thinkingContent,
                        defaultOpen = true,
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
            
            // Main message bubble
            Surface(
                color = if (isUser) Indigo600 else Gray800,
                shape = RoundedCornerShape(
                    topStart = 12.dp,
                    topEnd = 12.dp,
                    bottomStart = if (isUser) 12.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 12.dp,
                ),
                modifier = Modifier.widthIn(max = (LocalConfiguration.current.screenWidthDp * 0.85f).dp),
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    if (isUser) {
                        Text(
                            text = msg.content,
                            color = Gray100,
                            fontSize = 14.sp,
                        )
                    } else {
                        // Use text blocks or fallback to content
                        val content = if (textBlocks.isNotEmpty()) {
                            textBlocks.joinToString("\n\n") { it.text }
                        } else {
                            msg.content
                        }
                        
                        if (content.isNotEmpty()) {
                            MarkdownRenderer(content = content)
                        } else {
                            Text(
                                text = "[${msg.role}]",
                                color = Gray500,
                                fontSize = 12.sp,
                            )
                        }
                    }
                }
            }
            
            // Timestamp and save button
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            ) {
                Text(
                    text = timeAgo,
                    color = Gray600,
                    fontSize = 10.sp,
                )
                IconButton(
                    onClick = { saved = !saved },
                    modifier = Modifier.size(20.dp),
                ) {
                    Icon(
                        imageVector = if (saved) Icons.Default.Check else Icons.Default.Bookmark,
                        contentDescription = "Save to Knowledge Base",
                        tint = if (saved) Emerald400 else Gray600,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }
        }
    }
}
