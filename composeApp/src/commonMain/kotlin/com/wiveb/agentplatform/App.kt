package com.wiveb.agentplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.di.appModule
import com.wiveb.agentplatform.ui.navigation.*
import com.wiveb.agentplatform.ui.settings.SettingsScreen
import com.wiveb.agentplatform.ui.theme.AgentPlatformTheme
import com.wiveb.agentplatform.ui.theme.Gray100
import com.wiveb.agentplatform.ui.theme.Gray500
import com.wiveb.agentplatform.ui.theme.Gray800
import com.wiveb.agentplatform.ui.theme.Gray900
import com.wiveb.agentplatform.ui.theme.Indigo500
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
                                        Icons.Default.Settings,
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
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        "Agent Platform",
                                        color = Gray100,
                                    )
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
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                            ) {
                                val tabs = listOf(DashboardTab, ChatTab, BoardTab, ActivityTab, AgentsTab)
                                tabs.forEach { tab ->
                                    NavigationBarItem(
                                        selected = tabNavigator.current == tab,
                                        onClick = { tabNavigator.current = tab },
                                        icon = {
                                            tab.options.icon?.let {
                                                Icon(painter = it, contentDescription = tab.options.title)
                                            }
                                        },
                                        label = { Text(tab.options.title) },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Indigo500,
                                            selectedTextColor = Indigo500,
                                            unselectedIconColor = Gray500,
                                            unselectedTextColor = Gray500,
                                            indicatorColor = Gray800,
                                        ),
                                    )
                                }
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
