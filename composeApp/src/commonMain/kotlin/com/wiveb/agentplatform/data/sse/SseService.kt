package com.wiveb.agentplatform.data.sse

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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
        val response: HttpResponse = client.get("$baseUrl/api/events")
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
