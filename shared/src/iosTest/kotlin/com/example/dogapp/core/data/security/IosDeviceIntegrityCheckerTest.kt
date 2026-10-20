package com.example.dogapp.core.data.security

import com.example.dogapp.core.domain.security.IntegrityCheck
import com.example.dogapp.core.domain.security.IntegrityCheckKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class IosDeviceIntegrityCheckerTest {
    @Test
    fun onTheSimulatorOnlyTheSimulatorIsFlagged() =
        runTest {
            assertEquals(
                listOf(
                    IntegrityCheck(IntegrityCheckKind.JailbreakFiles, isFlagged = false),
                    IntegrityCheck(IntegrityCheckKind.SandboxWrite, isFlagged = false),
                    IntegrityCheck(IntegrityCheckKind.Simulator, isFlagged = true),
                    IntegrityCheck(IntegrityCheckKind.Debugger, isFlagged = false),
                ),
                IosDeviceIntegrityChecker().check().checks,
            )
        }
}
