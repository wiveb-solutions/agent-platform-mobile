package com.wiveb.agentplatform.data

expect class PlatformSettings {
    fun getString(key: String, default: String): String
    fun putString(key: String, value: String)
}
