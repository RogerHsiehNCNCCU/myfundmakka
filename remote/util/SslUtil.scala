package com.oring.smartcity.util

import java.io.InputStream
import java.security.{KeyStore, SecureRandom}
import javax.net.ssl.{SSLContext, TrustManagerFactory}


class SslUtil {
  def getSslContext(key: String, ks: InputStream): SSLContext = {
    val passwd = key
    val sslContext = SSLContext.getInstance("SSL")
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    val keyStore = KeyStore.getInstance("JKS", "SUN")
    keyStore.load(ks, passwd.toCharArray())
    trustManagerFactory.init(keyStore)
    sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom())
    sslContext
  }
}
