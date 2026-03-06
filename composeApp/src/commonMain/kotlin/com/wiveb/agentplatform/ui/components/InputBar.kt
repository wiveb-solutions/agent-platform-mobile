package com.wiveb.agentplatform.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.wiveb.agentplatform.ui.theme.*

/**
 * Enhanced input bar with icons for attachments, voice input, and web search.
 */
@Composable
fun InputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onWebSearchClick: () -> Unit = {},
    onStop: (() -> Unit)? = null,
    isSending: Boolean = false,
    placeholder: String = "Message...",
    modifier: Modifier = Modifier,
) {
    Surface(
        color = Gray900,
        tonalElevation = 2.dp,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Attachment icon
            IconButton(
                onClick = onAttachmentClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach file",
                    tint = Gray400,
                    modifier = Modifier.size(20.dp),
                )
            }

            // Text input field
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = { Text(placeholder, color = Gray500) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Gray100,
                    unfocusedTextColor = Gray100,
                    focusedBorderColor = Indigo600,
                    unfocusedBorderColor = Gray700,
                    cursorColor = Indigo400,
                    focusedContainerColor = Gray800,
                    unfocusedContainerColor = Gray800,
                ),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                singleLine = false,
            )

            Spacer(Modifier.width(4.dp))

            // Voice input icon
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Mic,
                    contentDescription = "Voice input",
                    tint = Gray400,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(2.dp))

            // Web search icon
            IconButton(
                onClick = onWebSearchClick,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    Icons.Default.Public,
                    contentDescription = "Web search",
                    tint = Gray400,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(4.dp))

            // Send or Stop button
            if (isSending && onStop != null) {
                IconButton(
                    onClick = onStop,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.Default.Stop,
                        contentDescription = "Stop generating",
                        tint = Red400,
                        modifier = Modifier.size(20.dp),
                    )
                }
            } else {
                IconButton(
                    onClick = onSend,
                    enabled = value.isNotBlank(),
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (value.isNotBlank()) Indigo400 else Gray600,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
