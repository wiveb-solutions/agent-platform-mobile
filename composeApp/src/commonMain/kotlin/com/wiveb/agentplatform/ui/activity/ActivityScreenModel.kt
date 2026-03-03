package com.wiveb.agentplatform.ui.activity

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.ActivityEvent
import com.wiveb.agentplatform.data.model.AgentStatus
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

data class ActivityData(
    val agentStatuses: List<AgentStatus> = emptyList(),
    val events: List<ActivityEvent> = emptyList(),
)

class ActivityScreenModel(
    private val api: AgentPlatformApi,
    private val sse: SseService,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<ActivityData>>(UiState.Loading)
    val state: StateFlow<UiState<ActivityData>> = _state.asStateFlow()

    private val _filterAgent = MutableStateFlow<String?>(null)
    val filterAgent: StateFlow<String?> = _filterAgent.asStateFlow()

    private val _filterType = MutableStateFlow<String?>(null)
    val filterType: StateFlow<String?> = _filterType.asStateFlow()

    private val json = Json { ignoreUnknownKeys = true }

    init {
        load()
        observeSse()
    }

    fun setFilterAgent(agentId: String?) {
        _filterAgent.value = agentId
        load()
    }

    fun setFilterType(type: String?) {
        _filterType.value = type
        load()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val statuses = runCatching { api.getAgentStatuses() }.getOrNull()?.agents ?: emptyList()
                val events = runCatching {
                    api.getActivity(agentId = _filterAgent.value, type = _filterType.value)
                }.getOrNull()?.events ?: emptyList()
                _state.value = UiState.Success(ActivityData(statuses, events))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load activity")
            }
        }
    }

    private fun observeSse() {
        screenModelScope.launch {
            sse.events.collect { event ->
                when (event.type) {
                    "activity:event" -> {
                        val current = (_state.value as? UiState.Success)?.data ?: return@collect
                        val newEvent = runCatching {
                            json.decodeFromString<ActivityEvent>(event.data)
                        }.getOrNull() ?: return@collect
                        val updated = listOf(newEvent) + current.events
                        _state.value = UiState.Success(current.copy(events = updated.take(100)))
                    }
                    "activity:agents" -> load()
                }
            }
        }
    }
}
