package com.wiveb.agentplatform.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.ChatSession
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatListScreenModel(
    private val api: AgentPlatformApi,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<List<ChatSession>>>(UiState.Loading)
    val state: StateFlow<UiState<List<ChatSession>>> = _state.asStateFlow()

    private val _filter = MutableStateFlow<String?>(null)
    val filter: StateFlow<String?> = _filter.asStateFlow()

    init {
        load()
    }

    fun setFilter(agentId: String?) {
        _filter.value = agentId
        load()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val resp = api.getSessions(agentId = _filter.value)
                _state.value = UiState.Success(resp.sessions)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load sessions")
            }
        }
    }
}
