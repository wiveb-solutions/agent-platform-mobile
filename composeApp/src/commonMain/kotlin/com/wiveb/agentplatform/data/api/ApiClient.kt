package com.wiveb.agentplatform.data.api

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient

/**
 * Crée un HttpClient Ktor.
 * 
 * @param skipSslVerification Si true, désactive la vérification SSL.
 * ⚠️ À utiliser UNIQUEMENT en mode debug/dev local. JAMAIS en production!
 */
fun createHttpClient(skipSslVerification: Boolean = false): HttpClient {
    return HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
            connectTimeoutMillis = 10_000
        }
        expectSuccess = false
        
        // Désactivation SSL verification pour dev local
        if (skipSslVerification) {
            engine {
                // Ktor 3.x: utiliser clientConfig pour passer un OkHttpClient personnalisé
                val sslContext = createInsecureSslContext()
                val trustManager = TrustAllTrustManager()
                
                val customOkHttp = OkHttpClient.Builder().apply {
                    sslSocketFactory(sslContext.socketFactory, trustManager)
                    hostnameVerifier(AllowAllHostnameVerifier())
                }.build()
                
                // Passer le client OkHttp personnalisé
                this.client = customOkHttp
            }
        }
    }
}
