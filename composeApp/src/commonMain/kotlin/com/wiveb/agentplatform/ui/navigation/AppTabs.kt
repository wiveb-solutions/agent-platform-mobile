package com.wiveb.agentplatform.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wiveb.agentplatform.ui.activity.ActivityScreen
import com.wiveb.agentplatform.ui.agents.AgentsScreen
import com.wiveb.agentplatform.ui.board.BoardScreen
import com.wiveb.agentplatform.ui.chat.ChatScreen
import com.wiveb.agentplatform.ui.dashboard.DashboardScreen
import com.wiveb.agentplatform.ui.projects.ProjectsScreen
import com.wiveb.agentplatform.ui.tasks.TasksScreen

object DashboardTab : Tab {
    private fun readResolve(): Any = DashboardTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Dashboard)
            return remember { TabOptions(index = 0u, title = "Dashboard", icon = icon) }
        }

    @Composable
    override fun Content() {
        DashboardScreen()
    }
}

object ChatTab : Tab {
    private fun readResolve(): Any = ChatTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Chat)
            return remember { TabOptions(index = 1u, title = "Chat", icon = icon) }
        }

    @Composable
    override fun Content() {
        ChatScreen()
    }
}

object BoardTab : Tab {
    private fun readResolve(): Any = BoardTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.ViewColumn)
            return remember { TabOptions(index = 2u, title = "Board", icon = icon) }
        }

    @Composable
    override fun Content() {
        BoardScreen()
    }
}

object ActivityTab : Tab {
    private fun readResolve(): Any = ActivityTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Timeline)
            return remember { TabOptions(index = 3u, title = "Activity", icon = icon) }
        }

    @Composable
    override fun Content() {
        ActivityScreen()
    }
}

object AgentsTab : Tab {
    private fun readResolve(): Any = AgentsTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.People)
            return remember { TabOptions(index = 4u, title = "Agents", icon = icon) }
        }

    @Composable
    override fun Content() {
        AgentsScreen()
    }
}

object ProjectsTab : Tab {
    private fun readResolve(): Any = ProjectsTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Folder)
            return remember { TabOptions(index = 5u, title = "Projects", icon = icon) }
        }

    @Composable
    override fun Content() {
        ProjectsScreen()
    }
}

object TasksTab : Tab {
    private fun readResolve(): Any = TasksTab

    override val options: TabOptions
        @Composable
        get() {
            val icon = rememberVectorPainter(Icons.Default.Checklist)
            return remember { TabOptions(index = 6u, title = "Tasks", icon = icon) }
        }

    @Composable
    override fun Content() {
        TasksScreen()
    }
}
