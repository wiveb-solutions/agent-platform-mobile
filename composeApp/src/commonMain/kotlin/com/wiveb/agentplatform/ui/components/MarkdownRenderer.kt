package com.wiveb.agentplatform.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.ui.theme.*

/**
 * Custom Markdown renderer using standard Compose components.
 * Parses basic markdown syntax: headers, bold, italic, code, links, lists.
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        var inCodeBlock = false
        
        content.lines().forEach { line ->
            when {
                line.startsWith("```") -> {
                    inCodeBlock = !inCodeBlock
                    // Skip the fence line itself
                }
                inCodeBlock -> {
                    // Inside code block
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                    ) {
                        Text(
                            text = line,
                            color = Gray300,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 1.dp),
                        )
                    }
                }
                line.startsWith("# ") -> {
                    Text(
                        text = line.substring(2),
                        style = LocalTextStyle.current.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gray100,
                        ),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                line.startsWith("## ") -> {
                    Text(
                        text = line.substring(3),
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray100,
                        ),
                        modifier = Modifier.padding(vertical = 3.dp),
                    )
                }
                line.startsWith("### ") -> {
                    Text(
                        text = line.substring(4),
                        style = LocalTextStyle.current.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray100,
                        ),
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                line.startsWith("#### ") -> {
                    Text(
                        text = line.substring(5),
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Gray100,
                        ),
                        modifier = Modifier.padding(vertical = 2.dp),
                    )
                }
                line.startsWith("- ") || line.startsWith("* ") -> {
                    Text(
                        text = "• ${line.substring(2)}",
                        style = LocalTextStyle.current.copy(
                            fontSize = 14.sp,
                            color = Gray100,
                        ),
                        modifier = Modifier.padding(vertical = 1.dp),
                    )
                }
                line.isNotBlank() -> {
                    // Regular paragraph with inline formatting
                    MarkdownText(line)
                }
            }
        }
    }
}

@Composable
private fun MarkdownText(text: String) {
    val annotatedString = buildAnnotatedString {
        var pos = 0
        
        while (pos < text.length) {
            // Bold: **text** or __text__
            if (text.startsWith("**", pos) || text.startsWith("__", pos)) {
                val endMarker = if (text[pos] == '*') "**" else "__"
                val end = text.indexOf(endMarker, pos + 2)
                if (end != -1) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Gray100))
                    append(text.substring(pos + 2, end))
                    pop()
                    pos = end + 2
                    continue
                }
            }
            
            // Italic: *text* or _text_
            if (text.startsWith("*", pos) || text.startsWith("_", pos)) {
                val endMarker = if (text[pos] == '*') "*" else "_"
                val end = text.indexOf(endMarker, pos + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Gray100))
                    append(text.substring(pos + 1, end))
                    pop()
                    pos = end + 1
                    continue
                }
            }
            
            // Code: `code`
            if (text[pos] == '`') {
                val end = text.indexOf('`', pos + 1)
                if (end != -1) {
                    pushStyle(SpanStyle(
                        color = Gray300,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                    ))
                    append(text.substring(pos + 1, end))
                    pop()
                    pos = end + 1
                    continue
                }
            }
            
            // Link: [text](url)
            if (text[pos] == '[') {
                val textEnd = text.indexOf(']', pos + 1)
                if (textEnd != -1) {
                    val urlStart = text.indexOf('(', textEnd + 1)
                    val urlEnd = text.indexOf(')', urlStart + 1)
                    if (urlStart != -1 && urlEnd != -1) {
                        val linkText = text.substring(pos + 1, textEnd)
                        pushStyle(SpanStyle(
                            color = Indigo400,
                            textDecoration = TextDecoration.Underline,
                        ))
                        append(linkText)
                        pop()
                        pos = urlEnd + 1
                        continue
                    }
                }
            }
            
            // Regular character
            append(text[pos])
            pos++
        }
    }
    
    Text(
        text = annotatedString,
        style = LocalTextStyle.current.copy(
            fontSize = 14.sp,
            color = Gray100,
        ),
        modifier = Modifier.padding(vertical = 1.dp),
    )
}
