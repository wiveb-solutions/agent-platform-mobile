package com.wiveb.agentplatform.ui.projects

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.wiveb.agentplatform.data.model.Project
import com.wiveb.agentplatform.ui.components.*
import com.wiveb.agentplatform.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectsScreen() {
    val model = koinInject<ProjectsScreenModel>()
    val state by model.state.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state !is UiState.Loading) isRefreshing = false
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            model.load()
        },
    ) {
        when (val s = state) {
            is UiState.Loading -> LoadingIndicator(Modifier.fillMaxSize())
            is UiState.Error -> ErrorCard(s.message, onRetry = { model.load() })
            is UiState.Success -> {
                if (s.data.isEmpty()) {
                    EmptyState("No projects found", Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(s.data) { project ->
                            ProjectCard(project)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectCard(project: Project) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gray900),
        modifier = Modifier.fillMaxWidth().clickable(onClick = { /* TODO: Navigate to project detail */ }),
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header row with icon and identifier
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = null,
                    tint = Indigo400,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        project.name,
                        color = Gray100,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        project.identifier,
                        color = Gray500,
                        fontSize = 12.sp,
                    )
                }
            }

            // Description
            project.description?.let { desc ->
                Spacer(Modifier.height(12.dp))
                Text(
                    desc,
                    color = Gray400,
                    fontSize = 13.sp,
                    maxLines = 3,
                )
            }

            // Created at
            project.createdAt?.let { createdAt ->
                Spacer(Modifier.height(8.dp))
                Text(
                    "Created: $createdAt",
                    color = Gray600,
                    fontSize = 11.sp,
                )
            }
        }
    }
}
