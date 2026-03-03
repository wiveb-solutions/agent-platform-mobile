package com.wiveb.agentplatform.ui.dashboard

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.*
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class DashboardData(
    val health: HealthStatus = HealthStatus(),
    val agents: List<Agent> = emptyList(),
    val burndown: BurndownResponse = BurndownResponse(),
    val tasks: List<AgentTask> = emptyList(),
)

class DashboardScreenModel(
    private val api: AgentPlatformApi,
    private val sse: SseService,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<DashboardData>>(UiState.Loading)
    val state: StateFlow<UiState<DashboardData>> = _state.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    init {
        load()
        observeSse()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val health = runCatching { api.getHealth() }.getOrDefault(HealthStatus())
                val agents = runCatching { api.getAgents() }.getOrDefault(emptyList())
                val burndown = runCatching { api.getBurndown() }.getOrDefault(BurndownResponse())
                val tasks = runCatching { api.getTasks() }.getOrDefault(TasksResponse()).tasks.take(5)
                _state.value = UiState.Success(DashboardData(health, agents, burndown, tasks))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load dashboard")
            }
        }
    }

    private fun observeSse() {
        screenModelScope.launch {
            sse.events.collect { event ->
                val current = (_state.value as? UiState.Success)?.data ?: return@collect
                when (event.type) {
                    "health" -> {
                        val health = runCatching { json.decodeFromString<HealthStatus>(event.data) }.getOrNull()
                        if (health != null) _state.value = UiState.Success(current.copy(health = health))
                    }
                    "activity:agents" -> load()
                }
            }
        }
    }
}
