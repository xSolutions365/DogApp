package com.example.dogapp.core.domain.security

interface DeviceIntegrityChecker {
    suspend fun check(): IntegrityReport
}
