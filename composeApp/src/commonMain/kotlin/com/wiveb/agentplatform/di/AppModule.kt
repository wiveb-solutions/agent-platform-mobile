package com.wiveb.agentplatform.di

import com.wiveb.agentplatform.data.SettingsRepository
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.api.createHttpClient
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.ui.activity.ActivityScreenModel
import com.wiveb.agentplatform.ui.agents.AgentsScreenModel
import com.wiveb.agentplatform.ui.board.BoardScreenModel
import com.wiveb.agentplatform.ui.chat.ChatDetailScreenModel
import com.wiveb.agentplatform.ui.chat.ChatListScreenModel
import com.wiveb.agentplatform.ui.dashboard.DashboardScreenModel
import com.wiveb.agentplatform.ui.settings.SettingsScreenModel
import org.koin.dsl.module

val appModule = module {
    single { SettingsRepository() }
    single { createHttpClient() }
    single { AgentPlatformApi(get(), baseUrlProvider = { get<SettingsRepository>().getBaseUrl() }) }
    single { SseService(get(), baseUrlProvider = { get<SettingsRepository>().getBaseUrl() }) }

    factory { DashboardScreenModel(get(), get()) }
    factory { ChatListScreenModel(get()) }
    factory { params -> ChatDetailScreenModel(get(), params.get()) }
    factory { BoardScreenModel(get()) }
    factory { ActivityScreenModel(get(), get()) }
    factory { AgentsScreenModel(get()) }
    factory { SettingsScreenModel(get(), get()) }
}
