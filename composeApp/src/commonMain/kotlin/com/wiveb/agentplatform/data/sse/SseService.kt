package com.wiveb.agentplatform.data.sse

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.Json

data class SseEvent(
    val type: String,
    val data: String,
)

class SseService(
    private val client: HttpClient,
    private val baseUrlProvider: () -> String,
) {
    private val _events = MutableSharedFlow<SseEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<SseEvent> = _events.asSharedFlow()

    private val json = Json { ignoreUnknownKeys = true }
    private var job: Job? = null

    fun start(scope: CoroutineScope) {
        stop()
        job = scope.launch {
            while (isActive) {
                try {
                    connect()
                } catch (_: CancellationException) {
                    break
                } catch (_: Exception) {
                    // Reconnect after delay
                }
                delay(5_000)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun connect() {
        val baseUrl = baseUrlProvider().trimEnd('/')
        client.prepareGet("$baseUrl/api/events").execute { response: HttpResponse ->
            val channel: ByteReadChannel = response.bodyAsChannel()
            var eventType = ""
            val dataBuffer = StringBuilder()

            while (!channel.isClosedForRead) {
                val line = channel.readUTF8Line() ?: break

                when {
                    line.startsWith("event:") -> {
                        eventType = line.removePrefix("event:").trim()
                    }
                    line.startsWith("data:") -> {
                        dataBuffer.append(line.removePrefix("data:").trim())
                    }
                    line.isEmpty() && dataBuffer.isNotEmpty() -> {
                        val data = dataBuffer.toString()
                        dataBuffer.clear()
                        if (eventType.isNotEmpty() && eventType != "heartbeat") {
                            _events.tryEmit(SseEvent(eventType, data))
                        }
                        eventType = ""
                    }
                }
            }
        }
    }
}
