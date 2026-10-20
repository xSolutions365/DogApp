package com.example.dogapp.testing

import com.example.dogapp.core.domain.security.DeviceIntegrityChecker
import com.example.dogapp.core.domain.security.IntegrityReport

class FakeDeviceIntegrityChecker(
    var report: IntegrityReport = IntegrityReport(emptyList()),
) : DeviceIntegrityChecker {
    override suspend fun check(): IntegrityReport = report
}
