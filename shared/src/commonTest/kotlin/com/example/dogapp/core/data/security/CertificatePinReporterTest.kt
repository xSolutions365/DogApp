package com.example.dogapp.core.data.security

import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.loggerConfigInit
import com.example.dogapp.testing.RecordingLogWriter
import com.example.dogapp.testing.TestCertificates
import kotlin.test.Test
import kotlin.test.assertEquals

class CertificatePinReporterTest {
    private val logWriter = RecordingLogWriter()
    private val logger = Logger(config = loggerConfigInit(logWriter), tag = CERTIFICATE_PIN_LOG_TAG)

    @Test
    fun shippedPinsAcceptTheChainsEachApiHostServesToday() {
        val reporter = CertificatePinReporter(logger = logger)

        reporter.report("api.thedogapi.com", listOf(TestCertificates.gtsRootR4))
        reporter.report("dog.ceo", listOf(TestCertificates.gtsRootR4))
        reporter.report("api.api-ninjas.com", listOf(TestCertificates.amazonRootCa1))

        assertEquals(emptyList(), logWriter.entries)
    }

    @Test
    fun aMismatchIsWarnedOncePerHostAndUnpinnedHostsAreIgnored() {
        val reporter =
            CertificatePinReporter(
                pins =
                    mapOf(
                        "dog.ceo" to setOf(TestCertificates.GTS_ROOT_R4_PIN),
                        "api.api-ninjas.com" to setOf(TestCertificates.AMAZON_ROOT_CA_1_PIN),
                    ),
                logger = logger,
            )

        reporter.report("dog.ceo", listOf(TestCertificates.amazonRootCa1, TestCertificates.gtsRootR4))
        reporter.report("dog.ceo", listOf(TestCertificates.amazonRootCa1))
        reporter.report("dog.ceo", listOf(TestCertificates.amazonRootCa1))
        reporter.report("images.dog.ceo", listOf(TestCertificates.amazonRootCa1))
        reporter.report("api.api-ninjas.com", listOf("not a certificate".encodeToByteArray()))

        assertEquals(
            listOf(
                RecordingLogWriter.Entry(
                    Severity.Warn,
                    "Certificate pin mismatch for dog.ceo (report only, the request continues): " +
                        "chain [${TestCertificates.AMAZON_ROOT_CA_1_PIN}], expected [${TestCertificates.GTS_ROOT_R4_PIN}]",
                    CERTIFICATE_PIN_LOG_TAG,
                ),
                RecordingLogWriter.Entry(
                    Severity.Warn,
                    "Certificate pin mismatch for api.api-ninjas.com (report only, the request continues): " +
                        "chain [unreadable], expected [${TestCertificates.AMAZON_ROOT_CA_1_PIN}]",
                    CERTIFICATE_PIN_LOG_TAG,
                ),
            ),
            logWriter.entries,
        )
    }
}
