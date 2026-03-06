package com.wiveb.agentplatform.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wiveb.agentplatform.ui.theme.*

/**
 * Custom Markdown renderer using standard Compose components.
 * Parses basic markdown syntax: headers, bold, italic, code, links, lists, tables.
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
        content.lines().forEach { line ->
            when {
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
                line.startsWith("```") -> {
                    // Code block start/end - skip the fence lines
                }
                line.contains("```") -> {
                    // Inline code block on same line as fence
                }
                isInsideCodeBlock(content, line) -> {
                    // Inside code block
                    CodeBlockLine(line)
                }
                line.isNotBlank() -> {
                    // Regular paragraph with inline formatting
                    MarkdownText(line)
                }
            }
        }
    }
}

// Track code block state
private var inCodeBlock = false

private fun isInsideCodeBlock(content: String, currentLine: String): Boolean {
    if (currentLine.startsWith("```")) {
        inCodeBlock = !inCodeBlock
        return false
    }
    return inCodeBlock
}

@Composable
private fun CodeBlockLine(line: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray900)
            .padding(8.dp),
    ) {
        Text(
            text = line,
            color = Gray300,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Composable
private fun MarkdownText(text: String) {
    // Parse inline markdown: **bold**, *italic*, `code`, [link](url)
    val annotatedString = buildAnnotatedString {
        var index = 0
        val lines = text.split("\n")
        
        lines.forEachIndexed { lineIndex, line ->
            if (lineIndex > 0) append("\n")
            
            var pos = 0
            while (pos < line.length) {
                // Bold: **text** or __text__
                if (line.startsWith("**", pos) || line.startsWith("__", pos)) {
                    val endMarker = if (line[pos] == '*") "**" else "__"
                    val end = line.indexOf(endMarker, pos + 2)
                    if (end != -1) {
                        append("\n") // Reset for span
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Gray100)) {
                            append(line.substring(pos + 2, end))
                        }
                        pos = end + 2
                        continue
                    }
                }
                
                // Italic: *text* or _text_
                if (line.startsWith("*", pos) || line.startsWith("_", pos)) {
                    val endMarker = if (line[pos] == '*') "*" else "_"
                    val end = line.indexOf(endMarker, pos + 1)
                    if (end != -1) {
                        append("\n") // Reset for span
                        withStyle(SpanStyle(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = Gray100)) {
                            append(line.substring(pos + 1, end))
                        }
                        pos = end + 1
                        continue
                    }
                }
                
                // Code: `code`
                if (line[pos] == '`') {
                    val end = line.indexOf('`', pos + 1)
                    if (end != -1) {
                        append("\n") // Reset for span
                        withStyle(SpanStyle(
                            color = Gray300,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                        )) {
                            append(line.substring(pos + 1, end))
                        }
                        pos = end + 1
                        continue
                    }
                }
                
                // Link: [text](url)
                if (line[pos] == '[') {
                    val textEnd = line.indexOf(']', pos + 1)
                    if (textEnd != -1) {
                        val urlStart = line.indexOf('(', textEnd + 1)
                        val urlEnd = line.indexOf(')', urlStart + 1)
                        if (urlStart != -1 && urlEnd != -1) {
                            val linkText = line.substring(pos + 1, textEnd)
                            val url = line.substring(urlStart + 1, urlEnd)
                            append("\n") // Reset for span
                            withStyle(SpanStyle(color = Indigo400, textDecoration = androidx.compose.ui.text.TextDecoration.Underline)) {
                                append(linkText)
                            }
                            pos = urlEnd + 1
                            continue
                        }
                    }
                }
                
                // Regular character
                append(line[pos])
                pos++
            }
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
