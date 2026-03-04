package com.wiveb.agentplatform.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Health ──

@Serializable
data class HealthStatus(
    val openclaw: ServiceHealth = ServiceHealth(),
    val vllm: ServiceHealth = ServiceHealth(),
    val plane: ServiceHealth = ServiceHealth(),
)

@Serializable
data class ServiceHealth(
    val status: String = "unknown",
    val latency: Long? = null,
)

// ── Agents ──

@Serializable
data class Agent(
    val id: String,
    val name: String,
    val role: String,
    val status: String = "offline",
    val currentSession: String? = null,
    val lastActivity: String? = null,
    val lastAction: String? = null,
    val tokenUsage: TokenUsage? = null,
    val recentTools: List<String>? = null,
    val contextTokens: Int? = null,
    val contextWindow: Int? = null,
)

@Serializable
data class TokenUsage(
    val input: Int = 0,
    val output: Int = 0,
)

// ── Chat ──

@Serializable
data class SessionsResponse(
    val sessions: List<ChatSession> = emptyList(),
)

@Serializable
data class ChatSession(
    val key: String,
    val agentId: String? = null,
    val updatedAt: String? = null,
    val title: String? = null,
    val preview: String? = null,
)

@Serializable
data class MessagesResponse(
    val messages: List<ChatMessage> = emptyList(),
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String = "",
    val createdAt: String? = null,
)

@Serializable
data class SendMessageRequest(
    val message: String,
    val thinking: String? = null,
)

@Serializable
data class SendResult(
    val ok: Boolean = false,
    val error: String? = null,
)

@Serializable
data class CreateSessionRequest(
    val agentId: String,
    val title: String,
    val initialMessage: String? = null,
)

@Serializable
data class CreateSessionResult(
    val ok: Boolean = false,
    val sessionKey: String? = null,
    val error: String? = null,
)

// ── Board ──

@Serializable
data class BoardResponse(
    val columns: List<BoardColumn> = emptyList(),
)

@Serializable
data class BoardColumn(
    val state: String,
    val stateId: String,
    val items: List<WorkItem> = emptyList(),
)

@Serializable
data class WorkItem(
    val id: String,
    val identifier: String = "",
    val title: String = "",
    val priority: String? = null,
    val labels: List<String> = emptyList(),
    val assignee: String? = null,
    val createdAt: String? = null,
    val description: String? = null,
)

@Serializable
data class MoveStateRequest(val state: String)

@Serializable
data class AssignAgentRequest(val agent: String?)

// ── Activity ──

@Serializable
data class ActivityResponse(
    val events: List<ActivityEvent> = emptyList(),
)

@Serializable
data class ActivityEvent(
    val id: String? = null,
    val type: String = "",
    val agentId: String = "",
    val summary: String = "",
    val timestamp: String = "",
    val sessionKey: String? = null,
    val metadata: Map<String, String>? = null,
)

@Serializable
data class AgentStatusResponse(
    val agents: List<AgentStatus> = emptyList(),
)

@Serializable
data class AgentStatus(
    val id: String,
    val status: String = "offline",
    val action: String? = null,
    val sessionKey: String? = null,
)

// ── Tasks ──

@Serializable
data class TasksResponse(
    val tasks: List<AgentTask> = emptyList(),
)

@Serializable
data class AgentTask(
    val id: String,
    val agent: String = "",
    val prompt: String = "",
    val status: String = "pending",
    val result: String? = null,
    val error: String? = null,
    val createdAt: String? = null,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val duration: Long? = null,
)

@Serializable
data class CreateTaskRequest(
    val agent: String,
    val prompt: String,
)

// ── Metrics ──

@Serializable
data class MetricsResponse(
    val period: String? = null,
    val completedTasks: Int = 0,
    val totalTasks: Int = 0,
    val dailyData: List<DailyMetric> = emptyList(),
)

@Serializable
data class DailyMetric(
    val date: String,
    val completed: Int = 0,
    val commits: Int = 0,
)

@Serializable
data class BurndownResponse(
    val sprintDays: Int = 14,
    val totalStories: Int = 0,
    val completedStories: Int = 0,
    val velocity: Double = 0.0,
    val data: List<BurndownPoint> = emptyList(),
)

@Serializable
data class BurndownPoint(
    val date: String,
    val remaining: Int = 0,
    val ideal: Double = 0.0,
)

@Serializable
data class AgentStatsResponse(
    val agents: List<AgentStat> = emptyList(),
)

@Serializable
data class AgentStat(
    val agentId: String,
    val storiesCompleted: Int = 0,
    val storiesInProgress: Int = 0,
    @SerialName("avgCycleTimeHours")
    val avgCycleTimeHours: Double = 0.0,
    val commits: Int = 0,
    val prs: Int = 0,
)
