package com.wiveb.agentplatform.ui.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.model.ChatMessage
import com.wiveb.agentplatform.data.sse.SseEvent
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.ui.components.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

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

    // Deep Search state
    private val _deepSearchState = MutableStateFlow(DeepSearchProgressState(isActive = false))
    val deepSearchState: StateFlow<DeepSearchProgressState> = _deepSearchState.asStateFlow()

    private var deepSearchTimerJob: Job? = null
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
        _sidebarState.value = SidebarState(isExpanded = !_sidebarState.value.isExpanded)
    }

    // Deep Search methods
    fun startDeepSearch() {
        screenModelScope.launch {
            _deepSearchState.value = DeepSearchProgressState(
                isActive = true,
                events = listOf(
                    DeepSearchEvent(
                        id = DeepSearchEvent.generateId(),
                        type = EventType.START,
                        description = "Deep Search initiated",
                    )
                ),
                sourcesFound = 0,
                elapsedSeconds = 0,
                stepsCompleted = 0,
                totalSteps = 5,
            )
            startDeepSearchTimer()
        }
    }

    fun addDeepSearchEvent(event: DeepSearchEvent) {
        screenModelScope.launch {
            _deepSearchState.value = _deepSearchState.value.copy(
                events = _deepSearchState.value.events + event,
            )
        }
    }

    fun updateDeepSearchStats(sourcesFound: Int, stepsCompleted: Int) {
        screenModelScope.launch {
            _deepSearchState.value = _deepSearchState.value.copy(
                sourcesFound = sourcesFound,
                stepsCompleted = stepsCompleted,
            )
        }
    }

    fun completeDeepSearch() {
        screenModelScope.launch {
            stopDeepSearchTimer()
            _deepSearchState.value = _deepSearchState.value.copy(
                isActive = false,
                events = _deepSearchState.value.events + DeepSearchEvent(
                    id = DeepSearchEvent.generateId(),
                    type = EventType.COMPLETE,
                    description = "Deep Search completed successfully",
                ),
                stepsCompleted = _deepSearchState.value.totalSteps,
            )
            // Auto-dismiss after a short delay
            delay(2000)
            _deepSearchState.value = DeepSearchProgressState(isActive = false)
        }
    }

    fun failDeepSearch(message: String) {
        screenModelScope.launch {
            stopDeepSearchTimer()
            _deepSearchState.value = _deepSearchState.value.copy(
                isActive = false,
                errorMessage = message,
                events = _deepSearchState.value.events + DeepSearchEvent(
                    id = DeepSearchEvent.generateId(),
                    type = EventType.ERROR,
                    description = message,
                ),
            )
            // Auto-dismiss after a short delay
            delay(3000)
            _deepSearchState.value = DeepSearchProgressState(isActive = false)
        }
    }

    fun dismissDeepSearchPanel() {
        screenModelScope.launch {
            stopDeepSearchTimer()
            _deepSearchState.value = DeepSearchProgressState(isActive = false)
        }
    }

    private fun startDeepSearchTimer() {
        stopDeepSearchTimer()
        deepSearchTimerJob = screenModelScope.launch {
            var seconds = 0L
            while (_deepSearchState.value.isActive) {
                delay(1000)
                seconds++
                _deepSearchState.value = _deepSearchState.value.copy(elapsedSeconds = seconds)
            }
        }
    }

    private fun stopDeepSearchTimer() {
        deepSearchTimerJob?.cancel()
        deepSearchTimerJob = null
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
        when (event.type) {
            "message" -> {
                // Parse the event data - it might be wrapped or direct
                try {
                    val data = parseEventData(event.data)
                    // Check if this message is for our session by looking at sessionKey in the JSON
                    val sessionKeyFromEvent = extractSessionKey(data)
                    if (sessionKeyFromEvent == null || sessionKeyFromEvent == sessionKey) {
                        // Try to extract ChatMessage from the data
                        try {
                            val messageJson = extractMessageJson(data)
                            val message = json.decodeFromString<ChatMessage>(messageJson)
                            updateMessagesWithNewMessage(message)
                        } catch (_: Exception) {
                            // Fallback: create a simple message from raw data
                            val message = ChatMessage(
                                role = "assistant",
                                content = data,
                            )
                            updateMessagesWithNewMessage(message)
                        }
                    }
                } catch (e: Exception) {
                    // Ignore malformed events
                }
            }
            "thinking" -> {
                // Update thinking level if provided
                try {
                    val data = parseEventData(event.data)
                    val sessionKeyFromEvent = extractSessionKey(data)
                    if (sessionKeyFromEvent == null || sessionKeyFromEvent == sessionKey) {
                        // Try to extract thinking level
                        val level = extractThinkingLevel(data)
                        if (level != null) {
                            _thinking.value = level
                        }
                    }
                } catch (_: Exception) {
                    // Ignore malformed events
                }
            }
            "complete" -> {
                // Streaming complete for this session
                try {
                    val data = parseEventData(event.data)
                    val sessionKeyFromEvent = extractSessionKey(data)
                    if (sessionKeyFromEvent == null || sessionKeyFromEvent == sessionKey) {
                        _streaming.value = false
                        _sending.value = false
                        stopPolling()
                    }
                } catch (_: Exception) {
                    // Ignore malformed events
                }
            }
            "error" -> {
                // Handle error events
                try {
                    val data = parseEventData(event.data)
                    val sessionKeyFromEvent = extractSessionKey(data)
                    if (sessionKeyFromEvent == null || sessionKeyFromEvent == sessionKey) {
                        _streaming.value = false
                        _sending.value = false
                        stopPolling()
                    }
                } catch (_: Exception) {
                    // Ignore malformed events
                }
            }
        }
    }

    private fun parseEventData(data: String): String {
        // Remove surrounding quotes if present
        return data.trim('"')
    }

    private fun extractSessionKey(data: String): String? {
        try {
            val element = json.parseToJsonElement(data)
            val obj = element as? JsonObject ?: return null
            val value = obj.get("sessionKey") ?: return null
            return (value as? JsonPrimitive)?.contentOrNull
        } catch (_: Exception) {
            return null
        }
    }

    private fun extractMessageJson(data: String): String {
        try {
            val element = json.parseToJsonElement(data)
            return (element as? JsonObject)?.get("message")?.toString() ?: data
        } catch (_: Exception) {
            return data
        }
    }

    private fun extractThinkingLevel(data: String): String? {
        try {
            val element = json.parseToJsonElement(data)
            val obj = element as? JsonObject ?: return null
            val value = obj.get("level") ?: return null
            return (value as? JsonPrimitive)?.contentOrNull
        } catch (_: Exception) {
            return null
        }
    }

    private fun updateMessagesWithNewMessage(newMessage: ChatMessage) {
        screenModelScope.launch {
            when (val current = _messages.value) {
                is UiState.Success -> {
                    val currentList = current.data
                    // Find the last assistant message and update it (streaming)
                    val assistantMessages = currentList.indices.filter { currentList[it].role == "assistant" }
                    val updatedList = if (assistantMessages.isNotEmpty()) {
                        // Update the last assistant message
                        val list = currentList.toMutableList()
                        val lastIndex = assistantMessages.last()
                        // Append content if it's a streaming update
                        val existingMessage = list[lastIndex] as ChatMessage
                        list[lastIndex] = ChatMessage(
                            role = existingMessage.role,
                            content = existingMessage.content + newMessage.content,
                            createdAt = existingMessage.createdAt,
                            blocks = existingMessage.blocks,
                            usage = existingMessage.usage,
                        )
                        list
                    } else {
                        // Add new message
                        currentList + newMessage
                    }
                    _messages.value = UiState.Success(updatedList)
                }
                else -> {
                    // If not in Success state, reload messages
                    loadMessages()
                }
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
                    if (last?.role == "assistant" && last.content.isNotBlank()) {
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
