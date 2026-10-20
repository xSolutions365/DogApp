package com.example.dogapp.core.data.security

import com.example.dogapp.testing.TestCertificates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpkiPinTest {
    @Test
    fun pinIsTheBase64Sha256OfTheSubjectPublicKeyInfoForEcAndRsaKeys() {
        assertEquals(TestCertificates.GTS_ROOT_R4_PIN, spkiPin(TestCertificates.gtsRootR4))
        assertEquals(TestCertificates.AMAZON_ROOT_CA_1_PIN, spkiPin(TestCertificates.amazonRootCa1))
    }

    @Test
    fun anythingThatIsNotAWholeCertificateHasNoPin() {
        assertNull(spkiPin(ByteArray(0)))
        assertNull(spkiPin(TestCertificates.amazonRootCa1.copyOfRange(0, 120)))
        assertNull(spkiPin("not a certificate".encodeToByteArray()))
        assertNull(spkiPin(byteArrayOf(0x30, 0x84.toByte(), 0x7F, 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())))
    }
}
