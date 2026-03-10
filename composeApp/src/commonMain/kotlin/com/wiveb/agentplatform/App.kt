package com.wiveb.agentplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.di.appModule
import com.wiveb.agentplatform.ui.components.NavigationDrawer
import com.wiveb.agentplatform.ui.navigation.*
import com.wiveb.agentplatform.ui.settings.SettingsScreen
import com.wiveb.agentplatform.ui.theme.AgentPlatformTheme
import com.wiveb.agentplatform.ui.theme.Gray100
import com.wiveb.agentplatform.ui.theme.Gray500
import com.wiveb.agentplatform.ui.theme.Gray900
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        AgentPlatformTheme {
            val sseService = koinInject<SseService>()
            var showSettings by remember { mutableStateOf(false) }
            val navState = remember { AppNavigationState() }

            LaunchedEffect(Unit) {
                sseService.start(CoroutineScope(SupervisorJob() + Dispatchers.Default))
            }
            DisposableEffect(Unit) {
                onDispose { sseService.stop() }
            }

            CompositionLocalProvider(LocalAppNavigationState provides navState) {
                if (showSettings) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = { Text("Settings", color = Gray100) },
                                navigationIcon = {
                                    IconButton(onClick = { showSettings = false }) {
                                        Icon(Icons.Default.ArrowBack, "Back", tint = Gray100)
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Gray900,
                                ),
                            )
                        },
                    ) { padding ->
                        Box(Modifier.fillMaxSize().padding(padding)) {
                            SettingsScreen()
                        }
                    }
                } else {
                    TabNavigator(DashboardTab) { tabNavigator ->
                        NavigationDrawer(
                            currentTab = tabNavigator.current,
                            onTabSelected = { tab: cafe.adriel.voyager.navigator.tab.Tab ->
                                tabNavigator.current = tab
                            },
                            modifier = Modifier.fillMaxSize(),
                            topBarTitle = "Agent Platform",
                        ) { onOpenDrawer ->
                            SideEffect {
                                navState.openDrawer = onOpenDrawer
                            }

                            val isInChatDetail =
                                tabNavigator.current == ChatTab &&
                                    navState.chatDetailSessionKey != null

                            val isInAgentDetail =
                                tabNavigator.current == AgentsTab &&
                                    navState.agentDetailId != null

                            Scaffold(
                                topBar = {
                                    if (isInChatDetail) {
                                        TopAppBar(
                                            title = {
                                                Text(
                                                    navState.chatDetailSessionKey!!
                                                        .substringAfterLast(":")
                                                        .take(20)
                                                        .ifEmpty { "Chat" },
                                                    color = Gray100,
                                                )
                                            },
                                            navigationIcon = {
                                                IconButton(onClick = {
                                                    navState.chatDetailSessionKey = null
                                                }) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        "Back",
                                                        tint = Gray100,
                                                    )
                                                }
                                            },
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = Gray900,
                                            ),
                                        )
                                    } else if (isInAgentDetail) {
                                        TopAppBar(
                                            title = {
                                                Text(
                                                    navState.agentDetailName ?: "Agent",
                                                    color = Gray100,
                                                )
                                            },
                                            navigationIcon = {
                                                IconButton(onClick = {
                                                    navState.agentDetailId = null
                                                    navState.agentDetailName = null
                                                }) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ArrowBack,
                                                        "Back",
                                                        tint = Gray100,
                                                    )
                                                }
                                            },
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = Gray900,
                                            ),
                                        )
                                    } else {
                                        TopAppBar(
                                            title = { Text("Agent Platform", color = Gray100) },
                                            navigationIcon = {
                                                IconButton(onClick = onOpenDrawer) {
                                                    Icon(Icons.Default.Menu, "Menu", tint = Gray100)
                                                }
                                            },
                                            actions = {
                                                IconButton(onClick = { showSettings = true }) {
                                                    Icon(Icons.Default.Settings, "Settings", tint = Gray500)
                                                }
                                            },
                                            colors = TopAppBarDefaults.topAppBarColors(
                                                containerColor = Gray900,
                                            ),
                                        )
                                    }
                                },
                            ) { padding ->
                                Box(Modifier.fillMaxSize().padding(padding)) {
                                    CurrentTab()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
