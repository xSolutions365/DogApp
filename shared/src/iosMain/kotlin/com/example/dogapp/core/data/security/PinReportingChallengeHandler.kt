package com.example.dogapp.core.data.security

import io.ktor.client.engine.darwin.ChallengeHandler
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.reinterpret
import platform.CoreFoundation.CFArrayGetCount
import platform.CoreFoundation.CFArrayGetValueAtIndex
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFRelease
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.serverTrust
import platform.Security.SecCertificateCopyData
import platform.Security.SecCertificateRef
import platform.Security.SecTrustCopyCertificateChain
import platform.Security.SecTrustEvaluateWithError
import platform.Security.SecTrustRef

@OptIn(ExperimentalForeignApi::class)
fun pinReportingChallengeHandler(reporter: CertificatePinReporter): ChallengeHandler =
    { _, _, challenge, completionHandler ->
        val protectionSpace = challenge.protectionSpace
        val trust = protectionSpace.serverTrust
        if (protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust && trust != null) {
            if (SecTrustEvaluateWithError(trust, null)) reporter.report(protectionSpace.host, trust.certificateChain())
        }
        completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
    }

@OptIn(ExperimentalForeignApi::class)
private fun SecTrustRef.certificateChain(): List<ByteArray> {
    val chain = SecTrustCopyCertificateChain(this) ?: return emptyList()
    return try {
        (0 until CFArrayGetCount(chain)).mapNotNull { index ->
            val certificate: SecCertificateRef? = CFArrayGetValueAtIndex(chain, index)?.reinterpret()
            val data = SecCertificateCopyData(certificate) ?: return@mapNotNull null
            try {
                CFDataGetBytePtr(data)?.readBytes(CFDataGetLength(data).toInt())
            } finally {
                CFRelease(data)
            }
        }
    } finally {
        CFRelease(chain)
    }
}
