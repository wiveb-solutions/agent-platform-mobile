package com.wiveb.agentplatform.data.api

import com.wiveb.agentplatform.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

class AgentPlatformApi(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
) {
    private val baseUrl: String get() = baseUrlProvider().trimEnd('/')

    // ── Health ──

    suspend fun getHealth(): HealthStatus {
        return client.get("$baseUrl/api/health").body()
    }

    // ── Agents ──

    suspend fun getAgents(): List<Agent> {
        return client.get("$baseUrl/api/agents").body()
    }

    suspend fun getAgentStatuses(): AgentStatusResponse {
        return client.get("$baseUrl/api/activity/agents/status").body()
    }

    // ── Chat ──

    suspend fun getSessions(agentId: String? = null, limit: Int = 30): SessionsResponse {
        return client.get("$baseUrl/api/chat/sessions") {
            if (agentId != null) parameter("agentId", agentId)
            parameter("limit", limit)
        }.body()
    }

    suspend fun getMessages(sessionKey: String, limit: Int = 100): MessagesResponse {
        return client.get("$baseUrl/api/chat/sessions/$sessionKey/messages") {
            parameter("limit", limit)
        }.body()
    }

    suspend fun sendMessage(sessionKey: String, message: String, thinking: String? = null): SendResult {
        return client.post("$baseUrl/api/chat/sessions/$sessionKey/send") {
            contentType(ContentType.Application.Json)
            setBody(SendMessageRequest(message, thinking))
        }.body()
    }

    suspend fun abortGeneration(sessionKey: String): SendResult {
        return client.post("$baseUrl/api/chat/sessions/$sessionKey/abort").body()
    }

    // ── Board ──

    suspend fun getBoard(): BoardResponse {
        return client.get("$baseUrl/api/board").body()
    }

    suspend fun moveItem(itemId: String, state: String): SendResult {
        return client.patch("$baseUrl/api/board/$itemId/state") {
            contentType(ContentType.Application.Json)
            setBody(MoveStateRequest(state))
        }.body()
    }

    suspend fun assignAgent(itemId: String, agent: String?): SendResult {
        return client.patch("$baseUrl/api/board/$itemId/agent") {
            contentType(ContentType.Application.Json)
            setBody(AssignAgentRequest(agent))
        }.body()
    }

    // ── Activity ──

    suspend fun getActivity(
        agentId: String? = null,
        type: String? = null,
        limit: Int = 50,
    ): ActivityResponse {
        return client.get("$baseUrl/api/activity") {
            if (agentId != null) parameter("agent", agentId)
            if (type != null) parameter("type", type)
            parameter("limit", limit)
        }.body()
    }

    // ── Tasks ──

    suspend fun getTasks(agent: String? = null, status: String? = null): TasksResponse {
        return client.get("$baseUrl/api/tasks") {
            if (agent != null) parameter("agent", agent)
            if (status != null) parameter("status", status)
        }.body()
    }

    suspend fun createTask(agent: String, prompt: String): AgentTask {
        return client.post("$baseUrl/api/tasks") {
            contentType(ContentType.Application.Json)
            setBody(CreateTaskRequest(agent, prompt))
        }.body()
    }

    // ── Metrics ──

    suspend fun getMetrics(days: Int = 7): MetricsResponse {
        return client.get("$baseUrl/api/metrics") {
            parameter("days", days)
        }.body()
    }

    suspend fun getBurndown(): BurndownResponse {
        return client.get("$baseUrl/api/metrics/burndown").body()
    }

    suspend fun getAgentStats(): AgentStatsResponse {
        return client.get("$baseUrl/api/metrics/agent-stats").body()
    }
}
