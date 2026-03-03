package com.wiveb.agentplatform.ui.board

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.BoardColumn
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BoardScreenModel(
    private val api: AgentPlatformApi,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<List<BoardColumn>>>(UiState.Loading)
    val state: StateFlow<UiState<List<BoardColumn>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val resp = api.getBoard()
                _state.value = UiState.Success(resp.columns)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load board")
            }
        }
    }

    fun moveItem(itemId: String, state: String) {
        screenModelScope.launch {
            try {
                api.moveItem(itemId, state)
                load()
            } catch (_: Exception) {}
        }
    }

    fun assignAgent(itemId: String, agent: String?) {
        screenModelScope.launch {
            try {
                api.assignAgent(itemId, agent)
                load()
            } catch (_: Exception) {}
        }
    }
}
