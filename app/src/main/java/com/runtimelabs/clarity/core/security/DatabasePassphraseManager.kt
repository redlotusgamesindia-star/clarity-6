package com.runtimelabs.clarity.core.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.security.keystore.StrongBoxUnavailableException
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the SQLCipher database passphrase.
 *
 * Scheme (plan §14):
 *  - a 256-bit random passphrase is generated once on first launch,
 *  - it is wrapped (AES/GCM) with a key that lives in Android Keystore
 *    (hardware-backed; StrongBox when the device has it),
 *  - only the *wrapped* blob is persisted, in app-private SharedPreferences.
 *
 * Consequences, all intentional:
 *  - the raw passphrase never touches disk,
 *  - it is independent of the user's app-lock PIN — a Phase A PIN change
 *    never requires re-encrypting the database,
 *  - backup/transfer of app data is useless off-device because the Keystore
 *    key cannot leave the hardware (and backups are disabled anyway).
 */
@Singleton
class DatabasePassphraseManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Returns the raw DB passphrase, creating and wrapping it on first call.
     * Cheap after first launch (one prefs read + one AES-GCM decrypt).
     */
    @Synchronized
    fun getOrCreatePassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val stored = prefs.getString(KEY_WRAPPED_PASSPHRASE, null)
        if (stored != null) {
            return unwrap(stored)
        }

        val passphrase = ByteArray(PASSPHRASE_LENGTH_BYTES).also { SecureRandom().nextBytes(it) }
        prefs.edit().putString(KEY_WRAPPED_PASSPHRASE, wrap(passphrase)).apply()
        return passphrase
    }

    private fun wrap(plaintext: ByteArray): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKeystoreKey())
        val ciphertext = cipher.doFinal(plaintext)
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val ct = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
        return "$iv:$ct"
    }

    private fun unwrap(stored: String): ByteArray {
        val (ivPart, ctPart) = stored.split(":", limit = 2)
        val iv = Base64.decode(ivPart, Base64.NO_WRAP)
        val ciphertext = Base64.decode(ctPart, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKeystoreKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
        return cipher.doFinal(ciphertext)
    }

    private fun getOrCreateKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        fun buildSpec(strongBox: Boolean): KeyGenParameterSpec =
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .apply {
                    if (strongBox && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        setIsStrongBoxBacked(true)
                    }
                }
                .build()

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        return try {
            generator.init(buildSpec(strongBox = true))
            generator.generateKey()
        } catch (e: StrongBoxUnavailableException) {
            // Most devices lack a StrongBox; TEE-backed Keystore is the normal path.
            generator.init(buildSpec(strongBox = false))
            generator.generateKey()
        }
    }

    private companion object {
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val KEY_ALIAS = "clarity_db_master_key"
        const val PREFS_NAME = "clarity_key_prefs"
        const val KEY_WRAPPED_PASSPHRASE = "wrapped_db_passphrase"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_BITS = 128
        const val PASSPHRASE_LENGTH_BYTES = 32
    }
}
