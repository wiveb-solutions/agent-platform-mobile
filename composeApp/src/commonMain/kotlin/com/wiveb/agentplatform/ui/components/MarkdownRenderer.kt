package com.wiveb.agentplatform.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.*
import com.mikepenz.markdown.ui.*

/**
 * Custom Markdown renderer with:
 * - Proper table styling (borders, padding, alignment)
 * - Responsive header sizes (h1, h2, h3 proportioned for mobile)
 * - Consistent spacing and colors
 */
@Composable
fun MarkdownRenderer(
    content: String,
    modifier: Modifier = Modifier,
) {
    Markdown(
        content = content,
        modifier = modifier,
        colors = markdownColor(
            text = Gray100,
            heading1 = Gray100,
            heading2 = Gray100,
            heading3 = Gray100,
            link = Indigo400,
            code = Gray300,
            codeBlockBackground = Gray900,
            tableHeader = Gray100,
            tableRow = Gray100,
        ),
        typography = markdownTypography(
            text = LocalTextStyle.current,
            heading1 = LocalTextStyle.current.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
            heading2 = LocalTextStyle.current.copy(
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
            ),
            heading3 = LocalTextStyle.current.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 22.sp,
                letterSpacing = 0.sp,
            ),
            heading4 = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 20.sp,
            ),
            code = LocalTextStyle.current.copy(
                fontSize = 12.sp,
            ),
        ),
        elements = markdownElements(
            table = { table ->
                TableRenderer(table)
            },
            codeBlock = { codeBlock ->
                CodeBlockRenderer(codeBlock)
            },
        ),
    )
}

@Composable
private fun TableRenderer(table: Table) {
    // Table container with horizontal scroll for overflow
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .wrapContentWidth()
                .padding(vertical = 8.dp),
        ) {
            // Header row
            table.header?.let { header ->
                HeaderRowRenderer(header)
            }
            
            // Body rows
            table.body.forEach { row ->
                BodyRowRenderer(row)
            }
        }
    }
}

@Composable
private fun HeaderRowRenderer(cells: List<TableCell>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray800)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        cells.forEach { cell ->
            Text(
                text = cell.content,
                color = Gray100,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun BodyRowRenderer(cells: List<TableCell>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Gray900)
            .padding(horizontal = 8.dp, vertical = 6.dp),
    ) {
        cells.forEach { cell ->
            Text(
                text = cell.content,
                color = Gray300,
                fontSize = 12.sp,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun CodeBlockRenderer(codeBlock: CodeBlock) {
    androidx.compose.foundation.layout.Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Gray900)
                .padding(12.dp),
        ) {
            Text(
                text = codeBlock.content,
                color = Gray300,
                fontSize = 12.sp,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            )
        }
    }
}
