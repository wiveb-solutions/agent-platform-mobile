package com.wiveb.agentplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
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

            LaunchedEffect(Unit) {
                sseService.start(CoroutineScope(SupervisorJob() + Dispatchers.Default))
            }
            DisposableEffect(Unit) {
                onDispose { sseService.stop() }
            }

            if (showSettings) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Settings") },
                            navigationIcon = {
                                IconButton(onClick = { showSettings = false }) {
                                    Icon(
                                        Icons.Default.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Gray100,
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Gray900,
                                titleContentColor = Gray100,
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
                        onTabSelected = { tab: cafe.adriel.voyager.navigator.tab.Tab -> tabNavigator.current = tab },
                        modifier = Modifier.fillMaxSize(),
                        topBarTitle = "Agent Platform",
                    ) { onOpenDrawer: () -> Unit ->
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = {
                                        Text(
                                            "Agent Platform",
                                            color = Gray100,
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = onOpenDrawer) {
                                            Icon(
                                                Icons.Default.Menu,
                                                contentDescription = "Open menu",
                                                tint = Gray100,
                                            )
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
