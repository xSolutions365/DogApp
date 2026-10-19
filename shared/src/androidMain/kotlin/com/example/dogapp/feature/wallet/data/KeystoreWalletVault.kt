package com.example.dogapp.feature.wallet.data

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.byteArrayPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.dogapp.core.domain.Outcome
import com.example.dogapp.core.domain.map
import com.example.dogapp.feature.wallet.domain.WalletChange
import com.example.dogapp.feature.wallet.domain.WalletError
import dogapp.shared.generated.resources.Res
import dogapp.shared.generated.resources.wallet_prompt_cancel
import dogapp.shared.generated.resources.wallet_prompt_unlock_title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class KeystoreWalletVault(
    private val context: Context,
    private val dataStore: DataStore<Preferences>,
    private val promptHost: BiometricPromptHost,
) : WalletVault {
    override suspend fun exists(): Boolean = dataStore.data.first().contains(CIPHERTEXT)

    override suspend fun read(): Outcome<ByteArray, WalletError> {
        val stored = dataStore.data.first()
        val iv = checkNotNull(stored[IV])
        val ciphertext = checkNotNull(stored[CIPHERTEXT])
        val key = existingKey() ?: return Outcome.Failure(WalletError.KeyInvalidated)
        val cipher =
            initialisedCipher { init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(GCM_TAG_BITS, iv)) }
                ?: return Outcome.Failure(WalletError.KeyInvalidated)
        return authenticate(Res.string.wallet_prompt_unlock_title, cipher).map { it.doFinal(ciphertext) }
    }

    override suspend fun write(
        contents: ByteArray,
        change: WalletChange,
    ): Outcome<Unit, WalletError> {
        if (BiometricManager.from(context).canAuthenticate(BIOMETRIC_STRONG) != BiometricManager.BIOMETRIC_SUCCESS) {
            return Outcome.Failure(WalletError.BiometricUnavailable)
        }
        val key = existingKey() ?: generateKey()
        val cipher =
            initialisedCipher { init(Cipher.ENCRYPT_MODE, key) }
                ?: return Outcome.Failure(WalletError.KeyInvalidated)
        return authenticate(change.promptTitle, cipher).map { authenticated ->
            val ciphertext = authenticated.doFinal(contents)
            dataStore.edit { preferences ->
                preferences[IV] = authenticated.iv
                preferences[CIPHERTEXT] = ciphertext
            }
        }
    }

    override suspend fun delete() {
        withContext(Dispatchers.IO) { keyStore().deleteEntry(KEY_ALIAS) }
        dataStore.edit { preferences ->
            preferences.remove(IV)
            preferences.remove(CIPHERTEXT)
        }
    }

    private suspend fun authenticate(
        title: StringResource,
        cipher: Cipher,
    ): Outcome<Cipher, WalletError> =
        promptHost
            .authenticate(
                title = getString(title),
                negativeButtonText = getString(Res.string.wallet_prompt_cancel),
                cryptoObject = BiometricPrompt.CryptoObject(cipher),
            ).map { checkNotNull(it.cipher) }

    private fun initialisedCipher(init: Cipher.() -> Unit): Cipher? =
        try {
            Cipher.getInstance(TRANSFORMATION).apply(init)
        } catch (_: KeyPermanentlyInvalidatedException) {
            null
        }

    private suspend fun existingKey(): SecretKey? = withContext(Dispatchers.IO) { keyStore().getKey(KEY_ALIAS, null) as SecretKey? }

    private suspend fun generateKey(): SecretKey =
        withContext(Dispatchers.IO) {
            val spec =
                KeyGenParameterSpec
                    .Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE_BITS)
                    .setUserAuthenticationRequired(true)
                    .setInvalidatedByBiometricEnrollment(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                spec.setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P &&
                context.packageManager.hasSystemFeature(PackageManager.FEATURE_STRONGBOX_KEYSTORE)
            ) {
                spec.setIsStrongBoxBacked(true)
            }
            KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                .apply { init(spec.build()) }
                .generateKey()
        }

    private fun keyStore(): KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "wallet_key"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val KEY_SIZE_BITS = 256
        const val GCM_TAG_BITS = 128
        val IV = byteArrayPreferencesKey("wallet_iv")
        val CIPHERTEXT = byteArrayPreferencesKey("wallet_ciphertext")
    }
}
