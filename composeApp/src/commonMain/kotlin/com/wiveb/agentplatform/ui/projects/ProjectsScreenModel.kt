package com.wiveb.agentplatform.ui.projects

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.Project
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProjectsScreenModel(
    private val api: AgentPlatformApi,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<List<Project>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Project>>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val projects = api.getProjects()
                _state.value = UiState.Success(projects)
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load projects")
            }
        }
    }
}
