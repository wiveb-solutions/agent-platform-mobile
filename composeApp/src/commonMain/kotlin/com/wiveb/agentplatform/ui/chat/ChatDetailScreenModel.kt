package com.wiveb.agentplatform.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.ChatMessage
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatDetailScreenModel(
    private val api: AgentPlatformApi,
    val sessionKey: String,
) : ScreenModel {

    private val _messages = MutableStateFlow<UiState<List<ChatMessage>>>(UiState.Loading)
    val messages: StateFlow<UiState<List<ChatMessage>>> = _messages.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _thinking = MutableStateFlow("medium")
    val thinking: StateFlow<String> = _thinking.asStateFlow()

    private var pollJob: Job? = null

    init {
        loadMessages()
    }

    fun loadMessages() {
        screenModelScope.launch {
            try {
                val resp = api.getMessages(sessionKey)
                _messages.value = UiState.Success(resp.messages)
            } catch (e: Exception) {
                _messages.value = UiState.Error(e.message ?: "Failed to load messages")
            }
        }
    }

    fun setThinking(level: String) {
        _thinking.value = level
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        screenModelScope.launch {
            _sending.value = true
            try {
                api.sendMessage(sessionKey, text, _thinking.value)
                // Start polling for response
                startPolling()
            } catch (e: Exception) {
                _sending.value = false
            }
        }
    }

    fun abort() {
        screenModelScope.launch {
            try {
                api.abortGeneration(sessionKey)
            } catch (_: Exception) {}
            stopPolling()
            _sending.value = false
        }
    }

    private fun startPolling() {
        stopPolling()
        pollJob = screenModelScope.launch {
            var attempts = 0
            while (attempts < 60) { // max 3 minutes
                delay(3_000)
                try {
                    val resp = api.getMessages(sessionKey)
                    _messages.value = UiState.Success(resp.messages)
                    // Check if last message is from assistant (response received)
                    val last = resp.messages.lastOrNull()
                    if (last?.role == "assistant") {
                        _sending.value = false
                        break
                    }
                } catch (_: Exception) {}
                attempts++
            }
            _sending.value = false
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }
}
