package com.wiveb.agentplatform.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.wiveb.agentplatform.ui.theme.*
import cafe.adriel.voyager.navigator.tab.Tab

data class DrawerItem(
    val title: String,
    val icon: ImageVector,
    val tab: Tab,
    val selectedColor: Color = Indigo600,
    val unselectedColor: Color = Gray500
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawer(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (onOpenDrawer: () -> Unit) -> Unit
) {
    var drawerOpen by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    // Animation pour l'overlay
    val overlayAlpha = remember { Animatable(0f) }
    LaunchedEffect(drawerOpen) {
        overlayAlpha.animateTo(
            targetValue = if (drawerOpen) 0.5f else 0f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
    }
    
    // Animation pour le drawer
    val drawerOffset = remember { Animatable(0f) }
    LaunchedEffect(drawerOpen) {
        drawerOffset.animateTo(
            targetValue = if (drawerOpen) 0f else -1f,
            animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
        )
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        // Contenu principal
        content {
            drawerOpen = true
        }
        
        // Overlay sombre
        if (drawerOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = overlayAlpha.value))
                    .clickable { 
                        drawerOpen = false
                        focusManager.clearFocus()
                    }
            )
        }
        
        // Drawer latéral
        val drawerWidth = 280.dp
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset {
                    IntOffset((drawerWidth.toPx() * drawerOffset.value).toInt(), 0)
                }
                .background(Gray900)
                .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
        ) {
            NavigationDrawerContent(
                currentTab = currentTab,
                onTabSelected = { 
                    drawerOpen = false
                    onTabSelected(it)
                    focusManager.clearFocus()
                },
                onCloseDrawer = { drawerOpen = false }
            )
        }
    }
}

@Composable
private fun NavigationDrawerContent(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    onCloseDrawer: () -> Unit
) {
    val drawerItems = remember {
        listOf(
            DrawerItem(
                title = "Dashboard",
                icon = Icons.Default.Dashboard,
                tab = com.wiveb.agentplatform.ui.navigation.DashboardTab
            ),
            DrawerItem(
                title = "Projects",
                icon = Icons.Default.Folder,
                tab = com.wiveb.agentplatform.ui.navigation.BoardTab
            ),
            DrawerItem(
                title = "Tasks",
                icon = Icons.Default.Checklist,
                tab = com.wiveb.agentplatform.ui.navigation.BoardTab
            ),
            DrawerItem(
                title = "Agents",
                icon = Icons.Default.People,
                tab = com.wiveb.agentplatform.ui.navigation.AgentsTab
            ),
            DrawerItem(
                title = "CI/CD",
                icon = Icons.Default.Build,
                tab = com.wiveb.agentplatform.ui.navigation.ActivityTab
            ),
            DrawerItem(
                title = "Chat",
                icon = Icons.Default.Chat,
                tab = com.wiveb.agentplatform.ui.navigation.ChatTab
            )
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray900)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 16.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header avec logo/titre
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Indigo600,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Agent Platform",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray100
                    )
                }
            }
            
            // Séparateur
            item {
                HorizontalDivider(
                    color = Gray500.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            
            // Items de navigation
            items(drawerItems.size) { index ->
                val item = drawerItems[index]
                val isSelected = currentTab == item.tab
                
                DrawerMenuItem(
                    title = item.title,
                    icon = item.icon,
                    isSelected = isSelected,
                    onClick = { onTabSelected(item.tab) },
                    selectedColor = item.selectedColor,
                    unselectedColor = item.unselectedColor
                )
            }
            
            // Séparateur avant footer
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(
                    color = Gray500.copy(alpha = 0.3f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Footer avec version
            item {
                Text(
                    text = "v1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }
        }
        
        // Bouton de fermeture (optionnel, pour accessibilité)
        IconButton(
            onClick = onCloseDrawer,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close menu",
                tint = Gray100
            )
        }
    }
}

@Composable
private fun DrawerMenuItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    selectedColor: Color,
    unselectedColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) selectedColor.copy(alpha = 0.15f) else Color.Transparent,
        onClick = onClick,
        interactionSource = interactionSource,
        shadowElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) selectedColor else unselectedColor,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = title,
                color = if (isSelected) Gray100 else Gray500,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = selectedColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
