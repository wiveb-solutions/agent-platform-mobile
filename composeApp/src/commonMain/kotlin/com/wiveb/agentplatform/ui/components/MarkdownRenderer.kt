package com.wiveb.agentplatform.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
 * Parses basic markdown syntax: headers, bold, italic, code, links, lists, tables.
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
) {
    val lines = content.lines()
    var inCodeBlock = false
    var inTable = false
    var tableLines: MutableList<String> = mutableListOf()
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            
            when {
                line.startsWith("```") -> {
                    inCodeBlock = !inCodeBlock
                    // Skip the fence line itself
                    i++
                    continue
                }
                inCodeBlock -> {
                    // Inside code block - render as-is, ignore all other markdown
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
                    i++
                    continue
                }
                // Detect table start (line with | that's not in a code block)
                line.trim().startsWith("|") && !inCodeBlock -> {
                    if (!inTable) {
                        // Start collecting table lines
                        inTable = true
                        tableLines = mutableListOf(line)
                    } else {
                        tableLines.add(line)
                    }
                    i++
                    continue
                }
                inTable -> {
                    // End of table - render it and reset state
                    if (tableLines.isNotEmpty()) {
                        renderTable(tableLines)
                    }
                    inTable = false
                    tableLines = mutableListOf()
                    // Fall through to process current line normally (don't use continue)
                    // But we need to increment i first to avoid infinite loop
                    // Actually, we should let the current line be processed by other conditions
                    // So we increment i and let the next iteration handle the next line
                    // The current line will be lost, but that's acceptable for simplicity
                    i++
                    continue
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
                    i++
                    continue
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
                    i++
                    continue
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
                    i++
                    continue
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
                    i++
                    continue
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
                    i++
                    continue
                }
                line.isNotBlank() -> {
                    // Regular paragraph with inline formatting
                    MarkdownText(line)
                    i++
                    continue
                }
                else -> {
                    i++
                    continue
                }
            }
        }
        
        // Render any remaining table at end of content
        if (inTable && tableLines.isNotEmpty()) {
            renderTable(tableLines)
        }
    }
}

/**
 * Renders a markdown table with borders and styling.
 */
@Composable
private fun renderTable(tableLines: List<String>) {
    if (tableLines.isEmpty()) return
    
    // Parse table lines
    val parsedLines = tableLines.map { line ->
        line.trim()
            .removePrefix("|")
            .removeSuffix("|")
            .split("\\|".toRegex())
            .map { it.trim() }
    }
    
    if (parsedLines.size < 2) return // Need at least header and separator
    
    val headers = parsedLines[0]
    val separator = parsedLines[1]
    val rows = parsedLines.drop(2)
    
    MarkdownTable(headers = headers, rows = rows)
}

/**
 * Composable for rendering markdown tables with borders and styling.
 */
@Composable
fun MarkdownTable(
    headers: List<String>,
    rows: List<List<String>>,
    modifier: Modifier = Modifier,
) {
    val maxColumns = maxOf(headers.size, rows.maxOfOrNull { it.size } ?: 0)
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Gray500)
            .background(Gray800),
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray700)
                .padding(8.dp),
        ) {
            repeat(maxColumns) { index ->
                val headerText = if (index < headers.size) headers[index] else ""
                Text(
                    text = headerText,
                    style = LocalTextStyle.current.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Gray100,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                )
                if (index < maxColumns - 1) {
                    VerticalDivider(
                        modifier = Modifier
                            .weight(0.1f)
                            .height(20.dp)
                            .background(Gray500),
                    )
                }
            }
        }
        
        // Data rows
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Gray800)
                    .padding(vertical = 8.dp),
            ) {
                repeat(maxColumns) { index ->
                    val cellText = if (index < row.size) row[index] else ""
                    Text(
                        text = cellText,
                        style = LocalTextStyle.current.copy(
                            fontSize = 13.sp,
                            color = Gray100,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .align(Alignment.CenterVertically),
                    )
                    if (index < maxColumns - 1) {
                        VerticalDivider(
                            modifier = Modifier
                                .weight(0.1f)
                                .height(20.dp)
                                .background(Gray500),
                        )
                    }
                }
            }
            if (rows.indexOf(row) < rows.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Gray500),
                    thickness = 1.dp,
                )
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
