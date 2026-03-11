package com.wiveb.agentplatform.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.ChatMessage
import com.wiveb.agentplatform.data.sse.SseEvent
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.ui.components.SidebarState
import com.wiveb.agentplatform.ui.components.UiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ChatDetailScreenModel(
    private val api: AgentPlatformApi,
    val sessionKey: String,
    private val sseService: SseService,
) : ScreenModel {

    private val _messages = MutableStateFlow<UiState<List<ChatMessage>>>(UiState.Loading)
    val messages: StateFlow<UiState<List<ChatMessage>>> = _messages.asStateFlow()

    private val _sending = MutableStateFlow(false)
    val sending: StateFlow<Boolean> = _sending.asStateFlow()

    private val _streaming = MutableStateFlow(false)
    val streaming: StateFlow<Boolean> = _streaming.asStateFlow()

    private val _thinking = MutableStateFlow("medium")
    val thinking: StateFlow<String> = _thinking.asStateFlow()

    private val _sidebarState = MutableStateFlow(SidebarState(isExpanded = false))
    val sidebarState: StateFlow<SidebarState> = _sidebarState.asStateFlow()

    private var pollJob: Job? = null
    private var sseJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true }

    init {
        loadMessages()
        startSseListener()
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

    fun toggleSidebar() {
        _sidebarState.value = _sidebarState.value.copy(isExpanded = !_sidebarState.value.isExpanded)
    }

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        screenModelScope.launch {
            _sending.value = true
            _streaming.value = true
            try {
                api.sendMessage(sessionKey, text, _thinking.value)
                // Start polling as fallback if SSE doesn't receive updates
                startPolling()
            } catch (e: Exception) {
                _sending.value = false
                _streaming.value = false
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
            _streaming.value = false
        }
    }

    private fun startSseListener() {
        sseJob = screenModelScope.launch {
            sseService.events.collect { event ->
                try {
                    handleSseEvent(event)
                } catch (e: Exception) {
                    // Log but don't break the stream
                }
            }
        }
    }

    private fun handleSseEvent(event: SseEvent) {
        // Parse event data as JSON to check sessionKey
        val eventSessionKey = try {
            val obj = json.parseToJsonElement(event.data).jsonObject
            obj["sessionKey"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            null
        }

        // Only process events for this session
        if (eventSessionKey != null && eventSessionKey != sessionKey) return

        when (event.type) {
            "message", "chat" -> {
                // New or updated message — reload the full message list
                loadMessages()
            }
            "thinking" -> {
                // Thinking event — message is being generated
                _streaming.value = true
            }
            "complete", "done" -> {
                _streaming.value = false
                _sending.value = false
                stopPolling()
                loadMessages()
            }
            "error" -> {
                _streaming.value = false
                _sending.value = false
                stopPolling()
                loadMessages()
            }
        }
    }

    private fun startPolling() {
        stopPolling()
        pollJob = screenModelScope.launch {
            var attempts = 0
            while (attempts < 60 && _streaming.value) { // max 3 minutes, stop if streaming via SSE
                delay(3_000)
                try {
                    val resp = api.getMessages(sessionKey)
                    _messages.value = UiState.Success(resp.messages)
                    // Check if last message is from assistant (response received)
                    val last = resp.messages.lastOrNull()
                    if (last?.role == "assistant" && !last.content.isBlank()) {
                        _sending.value = false
                        _streaming.value = false
                        break
                    }
                } catch (_: Exception) {
                    // Continue polling on error
                }
                attempts++
            }
            if (_streaming.value) {
                _sending.value = false
                _streaming.value = false
            }
        }
    }

    private fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

}
