package com.wiveb.agentplatform.data.api

import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.SSLContext

/**
 * TrustManager qui accepte TOUS les certificats.
 * ⚠️ À utiliser UNIQUEMENT en mode debug/dev local.
 * JAMAIS en production!
 */
class TrustAllTrustManager : X509TrustManager {
    override fun checkClientTrusted(chain: Array<out X509Certificate>, authType: String) {
        // Accepte tous les certificats client
    }

    override fun checkServerTrusted(chain: Array<out X509Certificate>, authType: String) {
        // Accepte tous les certificats serveur
    }

    override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
}

/**
 * HostnameVerifier qui accepte TOUS les hostnames.
 * ⚠️ À utiliser UNIQUEMENT en mode debug/dev local.
 * JAMAIS en production!
 */
class AllowAllHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String, session: javax.net.ssl.SSLSession): Boolean = true
}

/**
 * Crée un SSLContext qui ne vérifie pas les certificats.
 * ⚠️ À utiliser UNIQUEMENT en mode debug/dev local.
 * JAMAIS en production!
 */
fun createInsecureSslContext(): SSLContext {
    val trustAllCerts = arrayOf<TrustManager>(TrustAllTrustManager())
    val sslContext = SSLContext.getInstance("TLS")
    sslContext.init(null, trustAllCerts, java.security.SecureRandom())
    return sslContext
}
