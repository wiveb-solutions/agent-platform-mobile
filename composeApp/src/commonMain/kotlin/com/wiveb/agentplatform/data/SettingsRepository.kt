package com.wiveb.agentplatform.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(private val platformSettings: PlatformSettings) {
    companion object {
        private const val KEY_BASE_URL = "base_url"
        private const val DEFAULT_BASE_URL = "https://agents.home-server.com"
    }

    private val _baseUrl = MutableStateFlow(
        platformSettings.getString(KEY_BASE_URL, DEFAULT_BASE_URL)
    )
    val baseUrl: StateFlow<String> = _baseUrl.asStateFlow()

    fun setBaseUrl(url: String) {
        val trimmed = url.trimEnd('/')
        _baseUrl.value = trimmed
        platformSettings.putString(KEY_BASE_URL, trimmed)
    }

    fun getBaseUrl(): String = _baseUrl.value
}
