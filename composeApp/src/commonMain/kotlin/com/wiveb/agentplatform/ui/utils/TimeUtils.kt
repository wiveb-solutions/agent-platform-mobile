package com.wiveb.agentplatform.ui.utils

import java.time.Instant
import java.util.Date

object TimeUtils {
    fun formatTimeAgo(timestamp: String?): String {
        if (timestamp == null) return ""
        
        return try {
            val ms = Date.from(Instant.parse(timestamp)).time
            val diff = System.currentTimeMillis() - ms
            val secs = diff / 1000
            val mins = secs / 60
            val hours = mins / 60
            val days = hours / 24
            
            when {
                secs < 60 -> "now"
                mins < 60 -> "${mins}m"
                hours < 24 -> "${hours}h"
                else -> "${days}d"
            }
        } catch (e: Exception) {
            ""
        }
    }
    
    fun formatTimestamp(timestamp: String?): String {
        if (timestamp == null) return ""
        
        return try {
            val instant = Instant.parse(timestamp)
            val date = Date.from(instant)
            val year = date.year + 1900
            val month = date.month + 1
            val day = date.date
            val hour = date.hours
            val minute = date.minutes
            String.format("%02d/%02d/%d %02d:%02d", day, month, year, hour, minute)
        } catch (e: Exception) {
            ""
        }
    }
}
