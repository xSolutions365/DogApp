package com.example.dogapp.feature.wallet.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.feature.wallet.domain.WalletChange
import com.example.dogapp.feature.wallet.domain.WalletError
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_prompt_cancel
import dogapp.shared.generated.resources.wallet_prompt_unlock_title
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFTypeDictionaryKeyCallBacks
import platform.CoreFoundation.kCFTypeDictionaryValueCallBacks
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSError
import platform.Foundation.create
import platform.LocalAuthentication.LAContext
import platform.LocalAuthentication.LAErrorAppCancel
import platform.LocalAuthentication.LAErrorBiometryNotAvailable
import platform.LocalAuthentication.LAErrorBiometryNotEnrolled
import platform.LocalAuthentication.LAErrorPasscodeNotSet
import platform.LocalAuthentication.LAErrorSystemCancel
import platform.LocalAuthentication.LAErrorUserCancel
import platform.LocalAuthentication.LAPolicyDeviceOwnerAuthenticationWithBiometrics
import platform.Security.SecAccessControlCreateWithFlags
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.errSecUserCanceled
import platform.Security.kSecAccessControlBiometryCurrentSet
import platform.Security.kSecAttrAccessControl
import platform.Security.kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecUseAuthenticationContext
import platform.Security.kSecValueData
import platform.posix.memcpy
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class KeychainWalletVault(
    private val dataStore: DataStore<Preferences>,
) : WalletVault {
    override suspend fun exists(): Boolean = dataStore.data.first()[STORED] == true

    override suspend fun read(): Outcome<ByteArray, WalletError> {
        val context = authenticationContext()
        biometricAvailability(context)?.let { return Outcome.Failure(it) }
        context.localizedReason = getString(Res.string.wallet_prompt_unlock_title)
        return withContext(Dispatchers.IO) {
            memScoped {
                val result = alloc<CFTypeRefVar>()
                val status =
                    withItemQuery(
                        kSecReturnData to kCFBooleanTrue,
                        kSecUseAuthenticationContext to context,
                    ) { SecItemCopyMatching(it, result.ptr) }
                when (status) {
                    errSecSuccess -> Outcome.Success((CFBridgingRelease(result.value) as NSData).toByteArray())
                    errSecUserCanceled -> Outcome.Failure(WalletError.AuthenticationCancelled)
                    errSecItemNotFound -> Outcome.Failure(WalletError.KeyInvalidated)
                    else -> Outcome.Failure(WalletError.AuthenticationFailed)
                }
            }
        }
    }

    override suspend fun write(
        contents: ByteArray,
        change: WalletChange,
    ): Outcome<Unit, WalletError> {
        val context = authenticationContext()
        biometricAvailability(context)?.let { return Outcome.Failure(it) }
        evaluateBiometrics(context, getString(change.promptTitle))?.let { return Outcome.Failure(it) }
        withContext(Dispatchers.IO) {
            withItemQuery { SecItemDelete(it) }
            val accessControl =
                checkNotNull(
                    SecAccessControlCreateWithFlags(
                        null,
                        kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly,
                        kSecAccessControlBiometryCurrentSet,
                        null,
                    ),
                )
            val status =
                try {
                    withItemQuery(kSecValueData to contents.toNSData(), kSecAttrAccessControl to accessControl) { SecItemAdd(it, null) }
                } finally {
                    CFRelease(accessControl)
                }
            check(status == errSecSuccess) { "Keychain add failed with status $status" }
        }
        dataStore.edit { preferences -> preferences[STORED] = true }
        return Outcome.Success(Unit)
    }

    override suspend fun delete() {
        withContext(Dispatchers.IO) { withItemQuery { SecItemDelete(it) } }
        dataStore.edit { preferences -> preferences.remove(STORED) }
    }

    private suspend fun authenticationContext(): LAContext =
        LAContext().apply {
            localizedCancelTitle = getString(Res.string.wallet_prompt_cancel)
            localizedFallbackTitle = ""
        }

    private fun biometricAvailability(context: LAContext): WalletError? =
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            if (context.canEvaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, error.ptr)) {
                null
            } else {
                error.value.toWalletError()
            }
        }

    private suspend fun evaluateBiometrics(
        context: LAContext,
        reason: String,
    ): WalletError? =
        suspendCancellableCoroutine { continuation ->
            continuation.invokeOnCancellation { context.invalidate() }
            context.evaluatePolicy(LAPolicyDeviceOwnerAuthenticationWithBiometrics, reason) { success, error ->
                continuation.resume(if (success) null else error.toWalletError())
            }
        }

    private fun NSError?.toWalletError(): WalletError =
        when (this?.code) {
            LAErrorUserCancel, LAErrorSystemCancel, LAErrorAppCancel -> WalletError.AuthenticationCancelled
            LAErrorBiometryNotAvailable, LAErrorBiometryNotEnrolled, LAErrorPasscodeNotSet -> WalletError.BiometricUnavailable
            else -> WalletError.AuthenticationFailed
        }

    private fun <T> withItemQuery(
        vararg extra: Pair<Any?, Any?>,
        block: (CFMutableDictionaryRef?) -> T,
    ): T {
        val entries = listOf(kSecClass to kSecClassGenericPassword, kSecAttrService to SERVICE, kSecAttrAccount to ACCOUNT) + extra
        val retained = mutableListOf<CFTypeRef?>()
        val query =
            CFDictionaryCreateMutable(null, entries.size.convert(), kCFTypeDictionaryKeyCallBacks.ptr, kCFTypeDictionaryValueCallBacks.ptr)
        try {
            entries.forEach { (key, value) ->
                CFDictionaryAddValue(query, key.toCFType(retained), value.toCFType(retained))
            }
            return block(query)
        } finally {
            retained.forEach { CFRelease(it) }
            CFRelease(query)
        }
    }

    private fun Any?.toCFType(retained: MutableList<CFTypeRef?>): CFTypeRef? =
        when (this) {
            is CPointer<*> -> this
            else -> CFBridgingRetain(this).also { retained += it }
        }

    private fun ByteArray.toNSData(): NSData =
        if (isEmpty()) NSData() else usePinned { pinned -> NSData.create(bytes = pinned.addressOf(0), length = size.toULong()) }

    private fun NSData.toByteArray(): ByteArray =
        ByteArray(length.toInt()).also { bytes ->
            if (bytes.isNotEmpty()) bytes.usePinned { pinned -> memcpy(pinned.addressOf(0), this.bytes, length) }
        }

    private companion object {
        const val SERVICE = "com.example.dogapp.wallet"
        const val ACCOUNT = "wallet"
        val STORED = booleanPreferencesKey("wallet_in_keychain")
    }
}
