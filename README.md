# Agent Platform Mobile

Android app for monitoring and interacting with OpenClaw AI agents. Built with Compose Multiplatform.

## Features

- **Dashboard** — Health status, sprint progress, KPIs, recent tasks
- **Chat** — Send messages to agents, view sessions with markdown rendering
- **Board** — Kanban board with drag-free mobile UX (bottom sheet actions)
- **Activity** — Real-time event feed via SSE, agent status pills
- **Agents** — Agent cards with status, context usage, recent tools
- **Settings** — Configurable backend URL with connection test

## Tech Stack

| Component | Choice |
|-----------|--------|
| Language | Kotlin |
| UI | Compose Multiplatform 1.8.0 + Material 3 |
| Navigation | Voyager 1.1.0 |
| HTTP | Ktor Client 3.1.1 + OkHttp engine |
| SSE | Ktor streaming |
| Serialization | kotlinx.serialization |
| DI | Koin 4.0.2 |
| Markdown | mikepenz/multiplatform-markdown-renderer |
| Min SDK | 26 (Android 8.0) |

## Build

```bash
# Debug APK
./gradlew :composeApp:assembleDebug

# Install on connected device
adb install composeApp/build/outputs/apk/debug/composeApp-debug.apk
```

## Setup

1. Build and install the APK
2. Open Settings (gear icon in top bar)
3. Set Backend URL to your agent-platform instance (default: `https://agents.home-server.com`)
4. Tap "Test Connection" to verify

## Backend

This app is a pure client — no backend modifications needed. It connects to the existing [agent-platform](https://github.com/wiveb-solutions/agent-platform) REST API.

## CI

GitHub Actions builds a debug APK on every push to main. Download from the Actions artifacts.
