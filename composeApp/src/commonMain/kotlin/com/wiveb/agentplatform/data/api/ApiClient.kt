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
    // Créer l'OkHttpClient personnalisé si nécessaire
    val okHttpClientBuilder = OkHttpClient.Builder()
    
    if (skipSslVerification) {
        val sslContext = createInsecureSslContext()
        val trustManager = TrustAllTrustManager()
        okHttpClientBuilder
            .sslSocketFactory(sslContext.socketFactory, trustManager)
            .hostnameVerifier(AllowAllHostnameVerifier())
    }
    
    val okHttpClient = okHttpClientBuilder.build()
    
    // Créer l'engine OkHttp avec le client personnalisé
    val engine = OkHttp.create {
        // La configuration est gérée par l'OkHttpClient passé
    }
    
    return HttpClient(engine) {
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
    }
}
