package com.wiveb.agentplatform.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.ui.theme.*

/**
 * Collapsible thinking block with lightbulb icon and violet theme.
 * Used to display AI reasoning/thinking process.
 */
@Composable
fun CollapsibleThinkingBlock(
    content: String,
    defaultOpen: Boolean = true,
) {
    var expanded by remember { mutableStateOf(defaultOpen) }

    Surface(
        color = Violet900.copy(alpha = 0.2f),
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = Violet600.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp),
            )
            .clip(RoundedCornerShape(8.dp)),
    ) {
        Column {
            // Header with lightbulb icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "Thinking",
                        tint = Violet400,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Thinking Process",
                        color = Violet400,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(24.dp),
                ) {
                    Icon(
                        Icons.Default.ArrowDropDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = Violet400,
                        modifier = Modifier.rotate(if (expanded) 180f else 0f),
                    )
                }
            }

            // Content
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(tween(200)),
                exit = shrinkVertically(tween(200)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = content,
                        color = Gray300,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                    )
                }
            }
        }
    }
}
