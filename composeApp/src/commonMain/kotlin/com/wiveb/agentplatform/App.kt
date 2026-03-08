package com.wiveb.agentplatform

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.RowScope
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.di.appModule
import com.wiveb.agentplatform.ui.components.NavigationDrawer
import com.wiveb.agentplatform.ui.navigation.*
import com.wiveb.agentplatform.ui.navigation.ChatTab
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
                        StickyTopAppBar(
                            title = "Settings",
                            navigationIcon = Icons.Default.ArrowBack,
                            onNavigationClick = { showSettings = false },
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
                                if (tabNavigator.current != ChatTab) {
                                    StickyTopAppBar(
                                        title = "Agent Platform",
                                        navigationIcon = Icons.Default.Menu,
                                        onNavigationClick = onOpenDrawer,
                                        actions = {
                                            IconButton(onClick = { showSettings = true }) {
                                                Icon(Icons.Default.Settings, "Settings", tint = Gray500)
                                            }
                                        },
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

/**
 * TopAppBar sticky avec hauteur max 56px et layout optimisé
 * Tous les éléments sur une seule ligne pour gagner de l'espace
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StickyTopAppBar(
    title: String,
    navigationIcon: ImageVector,
    onNavigationClick: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    TopAppBar(
        title = {
            Text(
                title,
                color = Gray100,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigationClick) {
                Icon(
                    navigationIcon,
                    contentDescription = "Navigation",
                    tint = Gray100,
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Gray900,
            titleContentColor = Gray100,
            navigationIconContentColor = Gray100,
            actionIconContentColor = Gray100,
        ),
        modifier = Modifier.heightIn(max = 56.dp),
    )
}
