package com.example.dogapp.core.data.security

import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.getAndUpdate

const val CERTIFICATE_PIN_LOG_TAG = "CertificatePins"

class CertificatePinReporter(
    private val pins: Map<String, Set<String>> = PinnedHosts.pins,
    private val logger: Logger = Logger.withTag(CERTIFICATE_PIN_LOG_TAG),
) {
    private val reportedHosts = MutableStateFlow(emptySet<String>())

    fun report(
        host: String,
        certificateChain: List<ByteArray>,
    ) {
        val expected = pins[host] ?: return
        val chainPins = certificateChain.map { spkiPin(it) ?: UNREADABLE_CERTIFICATE }
        if (chainPins.any { it in expected }) return
        val alreadyReported = host in reportedHosts.getAndUpdate { it + host }
        if (alreadyReported) return
        logger.w {
            "Certificate pin mismatch for $host (report only, the request continues): " +
                "chain ${chainPins.joinToString(prefix = "[", postfix = "]")}, " +
                "expected ${expected.joinToString(prefix = "[", postfix = "]")}"
        }
    }

    private companion object {
        const val UNREADABLE_CERTIFICATE = "unreadable"
    }
}
