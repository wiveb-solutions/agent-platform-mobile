package com.wiveb.agentplatform.ui.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.Agent
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun NewConversationDialog(
    agents: List<Agent>,
    onDismiss: () -> Unit,
    onCreateConversation: (String, String) -> Unit,
) {
    var selectedAgentId by remember { mutableStateOf<String?>(null) }
    var initialMessage by remember { mutableStateOf("") }
    var isCreating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = {
            if (!isCreating) onDismiss()
        },
        title = {
            Text(
                text = "New Conversation",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Gray100,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Agent selection
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Select Agent",
                        color = Gray400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    
                    if (agents.isEmpty()) {
                        Text(
                            text = "No agents available",
                            color = Gray500,
                            fontSize = 14.sp,
                        )
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            agents.forEach { agent ->
                                AgentSelectionCard(
                                    agent = agent,
                                    isSelected = selectedAgentId == agent.id,
                                    onClick = { selectedAgentId = agent.id },
                                )
                            }
                        }
                    }

                    // Error message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = Red400,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }

                // Initial message
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Initial Message (optional)",
                        color = Gray400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    OutlinedTextField(
                        value = initialMessage,
                        onValueChange = { initialMessage = it },
                        placeholder = { Text("Start a conversation...", color = Gray500) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Gray100,
                            unfocusedTextColor = Gray100,
                            focusedBorderColor = Indigo600,
                            unfocusedBorderColor = Gray700,
                            cursorColor = Indigo400,
                        ),
                        maxLines = 4,
                        shape = MaterialTheme.shapes.medium,
                    )
                }
            }
        },
        confirmButton = {
            if (isCreating) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Indigo400,
                    )
                    Text("Creating...", color = Gray400, fontSize = 14.sp)
                }
            } else {
                Button(
                    onClick = {
                        if (selectedAgentId == null) {
                            errorMessage = "Please select an agent"
                            return@Button
                        }
                        scope.launch {
                            isCreating = true
                            errorMessage = null
                            onCreateConversation(selectedAgentId!!, initialMessage)
                            isCreating = false
                        }
                    },
                    enabled = !isCreating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Indigo600,
                    ),
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Start Conversation", color = Gray100)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    if (!isCreating) onDismiss()
                },
                enabled = !isCreating,
            ) {
                Text("Cancel", color = Gray400)
            }
        },
        containerColor = Gray800,
    )
}

@Composable
private fun AgentSelectionCard(
    agent: Agent,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val agentId = agent.id
    val badgeColor = AgentColors.badge(agentId)
    
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) badgeColor.copy(alpha = 0.3f) else Gray900,
        ),
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusDot(agent.status, size = 10)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        agent.name,
                        color = if (isSelected) Gray100 else AgentColors.text(agentId),
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    )
                    Spacer(Modifier.width(8.dp))
                    AgentBadge(agentId)
                }
                Text(
                    agent.role,
                    color = Gray500,
                    fontSize = 12.sp,
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = if (agentId == "main") Orange400 else Indigo400,
                )
            }
        }
    }
}
