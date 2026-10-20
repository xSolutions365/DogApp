package com.example.dogapp.core.data.security

import okhttp3.Interceptor
import okhttp3.Response

class PinReportingInterceptor(
    private val reporter: CertificatePinReporter,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        chain.connection()?.handshake()?.let { handshake ->
            reporter.report(chain.request().url.host, handshake.peerCertificates.map { it.encoded })
        }
        return chain.proceed(chain.request())
    }
}
