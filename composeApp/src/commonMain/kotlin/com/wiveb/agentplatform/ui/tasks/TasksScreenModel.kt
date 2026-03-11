package com.wiveb.agentplatform.ui.tasks

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.AgentTask
import com.wiveb.agentplatform.data.model.TasksResponse
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class TasksFilter(
    val agent: String = "All",
    val status: String = "All"
)

data class TasksData(
    val tasks: List<AgentTask> = emptyList(),
    val filteredTasks: List<AgentTask> = emptyList(),
)

class TasksScreenModel(
    private val api: AgentPlatformApi,
) : ScreenModel {

    private val _state = MutableStateFlow<UiState<TasksData>>(UiState.Loading)
    val state: StateFlow<UiState<TasksData>> = _state.asStateFlow()

    private val _filter = MutableStateFlow(TasksFilter())
    val filter: StateFlow<TasksFilter> = _filter.asStateFlow()

    private var _lastCreateResult = MutableStateFlow<Result<Unit>?>(null)
    val lastCreateResult: StateFlow<Result<Unit>?> = _lastCreateResult.asStateFlow()

    private var refreshJob: Job? = null

    init {
        load()
        startAutoRefresh()
    }

    fun load() {
        screenModelScope.launch {
            _state.value = UiState.Loading
            try {
                val tasksResponse = runCatching { api.getTasks() }.getOrDefault(TasksResponse())
                val allTasks = tasksResponse.tasks
                val currentFilter = _filter.value
                val filtered = filterTasks(allTasks, currentFilter)
                _state.value = UiState.Success(TasksData(allTasks, filtered))
            } catch (e: Exception) {
                _state.value = UiState.Error(e.message ?: "Failed to load tasks")
            }
        }
    }

    fun setAgentFilter(agent: String) {
        _filter.value = _filter.value.copy(agent = agent)
        applyFilter()
    }

    fun setStatusFilter(status: String) {
        _filter.value = _filter.value.copy(status = status)
        applyFilter()
    }

    fun clearFilters() {
        _filter.value = TasksFilter()
        applyFilter()
    }

    private fun applyFilter() {
        screenModelScope.launch {
            val currentState = _state.value
            if (currentState is UiState.Success) {
                val filtered = filterTasks(currentState.data.tasks, _filter.value)
                _state.value = UiState.Success(currentState.data.copy(filteredTasks = filtered))
            }
        }
    }

    private fun filterTasks(tasks: List<AgentTask>, filter: TasksFilter): List<AgentTask> {
        return tasks.filter { task ->
            val agentMatch = filter.agent == "All" || task.agent == filter.agent
            val statusMatch = filter.status == "All" || task.status == filter.status
            agentMatch && statusMatch
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = screenModelScope.launch {
            while (true) {
                delay(5000) // Auto-refresh every 5 seconds
                load()
            }
        }
    }

    fun createTask(agent: String, prompt: String) {
        screenModelScope.launch {
            _lastCreateResult.value = runCatching {
                api.createTask(agent, prompt)
                Unit
            }
        }
    }

    fun clearLastCreateResult() {
        _lastCreateResult.value = null
    }
}
