package com.wiveb.agentplatform.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.ui.navigation.*
import com.wiveb.agentplatform.ui.theme.*

@Composable
fun AgentBadge(agentId: String, modifier: Modifier = Modifier) {
    Surface(
        color = AgentColors.badge(agentId),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = agentId.uppercase(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun StatusDot(
    status: String,
    modifier: Modifier = Modifier,
    size: Int = 8,
) {
    val color = when (status) {
        "active", "ok", "up" -> Emerald400
        "idle" -> Amber400
        else -> Gray500
    }

    val isActive = status == "active"
    val pulseScale = if (isActive) {
        val infiniteTransition = rememberInfiniteTransition()
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.4f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse,
            ),
        ).value
    } else {
        1f
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .scale(pulseScale)
            .clip(CircleShape)
            .background(color),
    )
}

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (color, label) = when (status) {
        "pending" -> Gray500 to "Pending"
        "running", "in_progress" -> Amber400 to "Running"
        "done", "completed" -> Emerald400 to "Done"
        "error" -> Red400 to "Error"
        else -> Gray500 to status.replaceFirstChar { it.uppercase() }
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun PriorityBadge(priority: String?, modifier: Modifier = Modifier) {
    if (priority == null || priority == "none") return

    val (color, label) = when (priority) {
        "urgent" -> Red400 to "Urgent"
        "high" -> Orange400 to "High"
        "medium" -> Amber400 to "Medium"
        "low" -> Blue400 to "Low"
        else -> Gray400 to priority
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp),
        modifier = modifier,
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = Gray400,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

@Composable
fun ErrorCard(message: String, onRetry: (() -> Unit)? = null) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Red400.copy(alpha = 0.1f)),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = message,
                color = Red400,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            if (onRetry != null) {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Indigo500)
    }
}

@Composable
fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().padding(48.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = Gray500,
            fontSize = 14.sp,
        )
    }
}

@Composable
fun ContextBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier,
) {
    if (max <= 0) return
    val ratio = (current.toFloat() / max).coerceIn(0f, 1f)
    val color = when {
        ratio > 0.85f -> Red400
        ratio > 0.65f -> Amber400
        else -> Emerald400
    }
    val pct = (ratio * 100).toInt()

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Context", color = Gray400, fontSize = 10.sp)
            Text("$pct%", color = color, fontSize = 10.sp)
        }
        Spacer(Modifier.height(2.dp))
        LinearProgressIndicator(
            progress = { ratio },
            color = color,
            trackColor = Gray800,
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    currentTab: Any,
    onTabSelected: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    var drawerOpen by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Fermer le drawer quand le clavier mobile s'ouvre (input focus)
    DisposableEffect(Unit) {
        val handler = {
            // Fermer le drawer si un input reçoit le focus
            focusManager.clearFocus()
            drawerOpen = false
        }
        // Note: Compose Multiplatform n'a pas d'événement beforefocus natif
        // On utilise clearFocus() pour fermer le drawer
        onDispose { }
    }

    ModalNavigationDrawer(
        drawerState = rememberModalDrawerState(defaultDrawerPosition = DrawerValue.Closed),
        drawerContent = {
            DrawerContainer {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                ) {
                    // Header
                    Text(
                        text = "Agent Platform",
                        color = Gray100,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp),
                    )

                    Spacer(Modifier.height(8.dp))

                    // Navigation items
                    val tabs = listOf(
                        DashboardTab to "Dashboard",
                        ChatTab to "Chat",
                        BoardTab to "Board",
                        ActivityTab to "Activity",
                        AgentsTab to "Agents",
                    )

                    tabs.forEach { (tab, label) ->
                        NavigationDrawerItem(
                            selected = currentTab == tab,
                            onClick = {
                                onTabSelected(tab)
                                drawerOpen = false
                            },
                            icon = {
                                tab.options.icon?.let { icon ->
                                    Icon(
                                        painter = icon,
                                        contentDescription = label,
                                        tint = if (currentTab == tab) Indigo500 else Gray500,
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = label,
                                    color = if (currentTab == tab) Indigo500 else Gray500,
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (currentTab == tab) Gray800.copy(alpha = 0.5f) else Color.Transparent,
                                    RoundedCornerShape(8.dp),
                                ),
                        )
                    }
                }
            }
        },
        modifier = modifier,
    ) {
        // Contenu principal avec aria-hidden quand le drawer est ouvert
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            // Bouton hamburger
            IconButton(
                onClick = { drawerOpen = true },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Open menu",
                    tint = Gray100,
                )
            }
        }
    }
}

@Composable
private fun DrawerContainer(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        color = Gray900,
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content,
        )
    }
}

@Composable
fun NewConversationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String?, String) -> Unit, // (agentId, initialMessage?, sessionKey)
    isLoading: Boolean = false,
    error: String? = null,
    availableAgents: List<String> = listOf("main", "dev", "pm", "qa"),
) {
    if (!showDialog) return

    var title by remember { mutableStateOf("") }
    var initialMessage by remember { mutableStateOf("") }
    var selectedAgent by remember { mutableStateOf("dev") }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = {
            if (!isLoading) onDismiss()
        },
        title = {
            Text(
                text = "New Conversation",
                color = Gray100,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Agent selection
                Text(
                    text = "Select Agent",
                    color = Gray400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {},
                ) {
                    var expanded by remember { mutableStateOf(false) }
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
                        modifier = Modifier.menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        availableAgents.forEach { agent ->
                            DropdownMenuItem(
                                text = { Text(agent.uppercase(), color = Gray100) },
                                onClick = {
                                    selectedAgent = agent
                                    expanded = false
                                },
                                trailingIcon = {
                                    if (selectedAgent == agent) {
                                        Icon(Icons.Default.Check, null, tint = Indigo400)
                                    }
                                },
                            )
                        }
                    }
                }

                // Title field
                Text(
                    text = "Title *",
                    color = Gray400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Enter conversation title...", color = Gray500) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Gray700,
                        focusedTextColor = Gray100,
                        unfocusedTextColor = Gray100,
                        cursorColor = Indigo400,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // Initial message (optional)
                Text(
                    text = "Initial Message (optional)",
                    color = Gray400,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                OutlinedTextField(
                    value = initialMessage,
                    onValueChange = { initialMessage = it },
                    placeholder = { Text("Start the conversation...", color = Gray500) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Indigo600,
                        unfocusedBorderColor = Gray700,
                        focusedTextColor = Gray100,
                        unfocusedTextColor = Gray100,
                        cursorColor = Indigo400,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    shape = RoundedCornerShape(8.dp),
                )

                // Error message
                error?.let { err ->
                    Text(
                        text = err,
                        color = Red400,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (title.isBlank()) {
                        // Error will be shown in the dialog
                    } else {
                        onCreate(selectedAgent, if (initialMessage.isBlank()) null else initialMessage, "")
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Gray100,
                    )
                } else {
                    Text("Create", color = Gray100)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (!isLoading) {
                        title = ""
                        initialMessage = ""
                        onDismiss()
                    }
                },
                enabled = !isLoading,
            ) {
                Text("Cancel", color = Gray400)
            }
        },
        containerColor = Gray900,
        tonalElevation = 4.dp,
    )
}
