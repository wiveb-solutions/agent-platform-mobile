package com.wiveb.agentplatform.ui.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.SettingsRepository
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val settings: SettingsRepository,
    private val api: AgentPlatformApi,
) : ScreenModel {

    companion object {
        const val DEFAULT_BASE_URL = "https://agents.home-server.com/api"
    }

    val baseUrl: StateFlow<String> = settings.baseUrl

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _testing = MutableStateFlow(false)
    val testing: StateFlow<Boolean> = _testing.asStateFlow()

    private val _connectionStatus = MutableStateFlow<String>("unknown")
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()

    private var connectionCheckJob: Job? = null

    init {
        startConnectionMonitoring()
    }

    private fun startConnectionMonitoring() {
        connectionCheckJob?.cancel()
        connectionCheckJob = screenModelScope.launch {
            while (true) {
                checkConnectionSilently()
                kotlinx.coroutines.delay(5000) // Check every 5 seconds
            }
        }
    }

    private suspend fun checkConnectionSilently() {
        try {
            val health = api.getHealth()
            val hasHealthyService = listOf(health.openclaw.status, health.vllm.status, health.plane.status)
                .any { it == "ok" || it == "up" }
            _connectionStatus.value = if (hasHealthyService) "active" else "error"
        } catch (e: Exception) {
            _connectionStatus.value = "error"
        }
    }

    fun setBaseUrl(url: String) {
        settings.setBaseUrl(url)
        _testResult.value = null
    }

    fun resetBaseUrl() {
        settings.setBaseUrl(DEFAULT_BASE_URL)
        _testResult.value = null
    }

    fun testConnection() {
        screenModelScope.launch {
            _testing.value = true
            _testResult.value = null
            try {
                val health = api.getHealth()
                val ok = listOf(health.openclaw.status, health.vllm.status, health.plane.status)
                    .count { it == "ok" || it == "up" }
                _testResult.value = "Connected ($ok/3 services healthy)"
                _connectionStatus.value = "active"
            } catch (e: Exception) {
                _testResult.value = "Error: ${e.message}"
                _connectionStatus.value = "error"
            } finally {
                _testing.value = false
            }
        }
    }
}
