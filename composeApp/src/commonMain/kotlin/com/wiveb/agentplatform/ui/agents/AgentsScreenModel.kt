package com.wiveb.agentplatform.ui.agents

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.Agent
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AgentsScreenModel(
    private val api: AgentPlatformApi,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<List<Agent>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Agent>>> = _state.asStateFlow()

    private val _expanded = MutableStateFlow<Set<String>>(emptySet())
    val expanded: StateFlow<Set<String>> = _expanded.asStateFlow()

    init {
        load()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val agents = api.getAgents()
                _state.value = UiState.Success(agents)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load agents")
            }
        }
    }

    fun toggleExpanded(agentId: String) {
        _expanded.update { current ->
            if (agentId in current) current - agentId else current + agentId
        }
    }
}
