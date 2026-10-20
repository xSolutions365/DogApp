package com.example.dogapp.core.data.security

import android.content.Context
import android.os.Build
import android.os.Debug
import androidx.core.content.pm.PackageInfoCompat
import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import com.example.dogapp.core.domain.security.IntegrityCheck
import com.example.dogapp.core.domain.security.IntegrityCheckKind
import com.example.dogapp.core.domain.security.IntegrityReport
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

const val SIGNING_CERT_SHA256_PROPERTY = "signingCertSha256"

class AndroidDeviceIntegrityChecker(
    private val context: Context,
    private val expectedSigningCertSha256: String,
) : DeviceIntegrityChecker {
    override suspend fun check(): IntegrityReport =
        withContext(Dispatchers.IO) {
            IntegrityReport(
                listOf(
                    IntegrityCheck(IntegrityCheckKind.RootOrMagisk, rootPaths.any { File(it).exists() }),
                    IntegrityCheck(IntegrityCheckKind.TestKeysBuild, Build.TAGS?.contains("test-keys") == true),
                    IntegrityCheck(IntegrityCheckKind.AppSignature, !isSignedWithExpectedCertificate()),
                    IntegrityCheck(IntegrityCheckKind.Emulator, isEmulator()),
                    IntegrityCheck(IntegrityCheckKind.Debugger, Debug.isDebuggerConnected() || Debug.waitingForDebugger()),
                ),
            )
        }

    private fun isSignedWithExpectedCertificate(): Boolean {
        if (expectedSigningCertSha256.isBlank()) return false
        val signatures = PackageInfoCompat.getSignatures(context.packageManager, context.packageName)
        return signatures.isNotEmpty() &&
            signatures.all {
                sha256(
                    it.toByteArray(),
                ).toHexString(HexFormat.UpperCase).equals(expectedSigningCertSha256, ignoreCase = true)
            }
    }

    private fun isEmulator(): Boolean =
        Build.HARDWARE in emulatorHardware ||
            Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator") ||
            Build.PRODUCT.contains("sdk") ||
            Build.MODEL.contains("Emulator") ||
            Build.MODEL.contains("Android SDK built for")

    private companion object {
        val rootPaths =
            listOf(
                "/system/bin/su",
                "/system/xbin/su",
                "/sbin/su",
                "/su/bin/su",
                "/system/sd/xbin/su",
                "/data/local/su",
                "/data/local/bin/su",
                "/data/local/xbin/su",
                "/data/adb/magisk",
                "/sbin/.magisk",
                "/cache/.disable_magisk",
                "/dev/.magisk.unblock",
            )
        val emulatorHardware = setOf("goldfish", "ranchu")
    }
}
