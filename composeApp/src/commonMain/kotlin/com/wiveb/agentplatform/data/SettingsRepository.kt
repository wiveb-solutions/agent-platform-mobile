package com.wiveb.agentplatform.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository {
    private val _baseUrl = MutableStateFlow("https://agents.home-server.com")
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    fun setBaseUrl(url: String) {
        _baseUrl.value = url.trimEnd('/')
    }

    fun getBaseUrl(): String = _baseUrl.value
}
