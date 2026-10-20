package com.example.dogapp.core.data.security

import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import com.example.dogapp.core.domain.security.IntegrityCheck
import com.example.dogapp.core.domain.security.IntegrityCheckKind
import com.example.dogapp.core.domain.security.IntegrityReport
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValuesOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import platform.Foundation.NSFileManager
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.darwin.CTL_KERN
import platform.darwin.KERN_PROC
import platform.darwin.KERN_PROC_PID
import platform.darwin.P_TRACED
import platform.darwin.kinfo_proc
import platform.darwin.sysctl
import platform.posix.getpid
import platform.posix.size_tVar

class IosDeviceIntegrityChecker : DeviceIntegrityChecker {
    override suspend fun check(): IntegrityReport =
        withContext(Dispatchers.IO) {
            IntegrityReport(
                listOf(
                    IntegrityCheck(
                        IntegrityCheckKind.JailbreakFiles,
                        jailbreakPaths.any { NSFileManager.defaultManager.fileExistsAtPath(it) },
                    ),
                    IntegrityCheck(IntegrityCheckKind.SandboxWrite, canWriteOutsideSandbox()),
                    IntegrityCheck(IntegrityCheckKind.Simulator, NSProcessInfo.processInfo.environment[SIMULATOR_ENVIRONMENT_KEY] != null),
                    IntegrityCheck(IntegrityCheckKind.Debugger, isTraced()),
                ),
            )
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun canWriteOutsideSandbox(): Boolean {
        val written =
            NSString
                .create(
                    string = "DogApp",
                ).writeToFile(SANDBOX_PROBE_PATH, atomically = true, encoding = NSUTF8StringEncoding, error = null)
        if (written) NSFileManager.defaultManager.removeItemAtPath(SANDBOX_PROBE_PATH, error = null)
        return written
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun isTraced(): Boolean =
        memScoped {
            val info = alloc<kinfo_proc>()
            val size = alloc<size_tVar>()
            size.value = sizeOf<kinfo_proc>().convert()
            val name = cValuesOf(CTL_KERN, KERN_PROC, KERN_PROC_PID, getpid())
            sysctl(name, 4u, info.ptr, size.ptr, null, 0u) == 0 && (info.kp_proc.p_flag and P_TRACED) != 0
        }

    private companion object {
        const val SIMULATOR_ENVIRONMENT_KEY = "SIMULATOR_DEVICE_NAME"
        const val SANDBOX_PROBE_PATH = "/private/dogapp_integrity_probe.txt"
        val jailbreakPaths =
            listOf(
                "/Applications/Cydia.app",
                "/Applications/Sileo.app",
                "/Applications/Zebra.app",
                "/Library/MobileSubstrate/MobileSubstrate.dylib",
                "/usr/lib/libsubstrate.dylib",
                "/usr/libexec/cydia",
                "/private/var/lib/apt",
                "/private/var/lib/cydia",
                "/var/jb",
                "/private/preboot/jb",
            )
    }
}
