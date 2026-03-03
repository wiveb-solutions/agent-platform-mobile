package com.wiveb.agentplatform.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Background / Surface
val Gray950 = Color(0xFF030712)
val Gray900 = Color(0xFF111827)
val Gray800 = Color(0xFF1F2937)
val Gray700 = Color(0xFF374151)
val Gray600 = Color(0xFF4B5563)
val Gray500 = Color(0xFF6B7280)
val Gray400 = Color(0xFF9CA3AF)
val Gray300 = Color(0xFFD1D5DB)
val Gray200 = Color(0xFFE5E7EB)
val Gray100 = Color(0xFFF3F4F6)

// Accent
val Indigo600 = Color(0xFF4F46E5)
val Indigo500 = Color(0xFF6366F1)
val Indigo400 = Color(0xFF818CF8)

// Agent colors
val Green400 = Color(0xFF4ADE80)
val Green600 = Color(0xFF16A34A)
val Orange400 = Color(0xFFFB923C)
val Orange600 = Color(0xFFEA580C)
val Blue400 = Color(0xFF60A5FA)
val Blue600 = Color(0xFF2563EB)
val Purple400 = Color(0xFFA78BFA)
val Purple600 = Color(0xFF9333EA)
val Yellow400 = Color(0xFFFACC15)

// Status
val Red400 = Color(0xFFF87171)
val Red600 = Color(0xFFDC2626)
val Emerald400 = Color(0xFF34D399)
val Amber400 = Color(0xFFFBBF24)

object AgentColors {
    fun badge(agentId: String): Color = when (agentId) {
        "dev" -> Green600
        "pm" -> Orange600
        "qa" -> Blue600
        "notaire" -> Purple600
        "main" -> Indigo600
        else -> Gray600
    }

    fun text(agentId: String): Color = when (agentId) {
        "dev" -> Green400
        "pm" -> Orange400
        "qa" -> Blue400
        "notaire" -> Yellow400
        "main" -> Purple400
        else -> Gray400
    }

    fun hex(agentId: String): Color = when (agentId) {
        "dev" -> Green400
        "pm" -> Orange400
        "qa" -> Blue400
        "notaire" -> Yellow400
        "main" -> Purple400
        else -> Gray400
    }
}

private val DarkColorScheme = darkColorScheme(
    primary = Indigo600,
    onPrimary = Color.White,
    primaryContainer = Indigo600,
    onPrimaryContainer = Color.White,
    secondary = Gray600,
    onSecondary = Gray100,
    background = Gray950,
    onBackground = Gray100,
    surface = Gray900,
    onSurface = Gray100,
    surfaceVariant = Gray800,
    onSurfaceVariant = Gray300,
    outline = Gray700,
    outlineVariant = Gray800,
    error = Red400,
    onError = Color.White,
)

@Composable
fun AgentPlatformTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content,
    )
}
