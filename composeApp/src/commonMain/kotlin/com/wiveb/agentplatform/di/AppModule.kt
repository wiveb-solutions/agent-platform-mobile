package com.wiveb.agentplatform.di

import com.wiveb.agentplatform.data.SettingsRepository
import com.wiveb.agentplatform.data.api.AgentPlatformApi
import com.wiveb.agentplatform.data.api.createHttpClient
import com.wiveb.agentplatform.data.sse.SseService
import com.wiveb.agentplatform.ui.activity.ActivityScreenModel
import com.wiveb.agentplatform.ui.agents.AgentsScreenModel
import com.wiveb.agentplatform.ui.board.BoardScreenModel
import com.wiveb.agentplatform.ui.chat.ChatListScreenModel
import com.wiveb.agentplatform.ui.dashboard.DashboardScreenModel
import com.wiveb.agentplatform.ui.projects.ProjectsScreenModel
import com.wiveb.agentplatform.ui.settings.SettingsScreenModel
import com.wiveb.agentplatform.ui.tasks.TasksScreenModel
import org.koin.dsl.module

val appModule = module {
    single { SettingsRepository() }
    
    // ⚠️ skipSslVerification = true pour dev local avec certificat auto-signé (mkcert)
    // À mettre à false en production!
    single { createHttpClient(skipSslVerification = true) }
    
    single { AgentPlatformApi(get(), baseUrlProvider = { get<SettingsRepository>().getBaseUrl() }) }
    single { SseService(get(), baseUrlProvider = { get<SettingsRepository>().getBaseUrl() }) }

    single { DashboardScreenModel(get(), get()) }
    single { ChatListScreenModel(get()) }
    single { BoardScreenModel(get()) }
    single { ActivityScreenModel(get(), get()) }
    single { AgentsScreenModel(get()) }
    single { ProjectsScreenModel(get()) }
    single { SettingsScreenModel(get(), get()) }
    single { TasksScreenModel(get()) }
}
