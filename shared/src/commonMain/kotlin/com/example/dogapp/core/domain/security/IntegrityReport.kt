package com.example.dogapp.core.domain.security

data class IntegrityReport(
    val checks: List<IntegrityCheck>,
) {
    val warningCount: Int
        get() = checks.count { it.isFlagged }
}

data class IntegrityCheck(
    val kind: IntegrityCheckKind,
    val isFlagged: Boolean,
)

enum class IntegrityCheckKind {
    RootOrMagisk,
    TestKeysBuild,
    AppSignature,
    Emulator,
    JailbreakFiles,
    SandboxWrite,
    Simulator,
    Debugger,
}
