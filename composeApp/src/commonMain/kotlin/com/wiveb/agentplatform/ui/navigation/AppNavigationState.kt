package com.wiveb.agentplatform.ui.navigation

import androidx.compose.runtime.*

/**
 * Shared navigation state accessible via CompositionLocal.
 * Allows child screens (e.g. ChatScreen) to control the global top bar.
 */
class AppNavigationState {
    /** When non-null, the chat detail view is active for this session key. */
    var chatDetailSessionKey by mutableStateOf<String?>(null)

    /** When non-null, the agent detail view is active for this agent ID. */
    var agentDetailId by mutableStateOf<String?>(null)

    /** Display name for the agent detail top bar. */
    var agentDetailName by mutableStateOf<String?>(null)

    /** Callback to open the navigation drawer. Set by App.kt. */
    var openDrawer: () -> Unit = {}
}

val LocalAppNavigationState = staticCompositionLocalOf { AppNavigationState() }
