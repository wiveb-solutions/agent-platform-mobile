package com.wiveb.agentplatform.ui.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.SettingsRepository
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val settings: SettingsRepository,
    private val api: AgentPlatformApi,
) : ScreenModel {

    val baseUrl: StateFlow<String> = settings.baseUrl

    private val _testResult = MutableStateFlow<String?>(null)
    val testResult: StateFlow<String?> = _testResult.asStateFlow()

    private val _testing = MutableStateFlow(false)
    val testing: StateFlow<Boolean> = _testing.asStateFlow()

    fun setBaseUrl(url: String) {
        settings.setBaseUrl(url)
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
            } catch (e: Exception) {
                _testResult.value = "Error: ${e.message}"
            } finally {
                _testing.value = false
            }
        }
    }
}
