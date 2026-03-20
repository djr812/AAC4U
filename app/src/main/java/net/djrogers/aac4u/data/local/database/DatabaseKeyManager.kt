package net.djrogers.aac4u.data.local.database

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the database encryption key using Android Keystore.
 *
 * How it works:
 * 1. On first launch, generates a random AES-256 key inside the Android Keystore
 * 2. Uses that Keystore key to encrypt a separate "passphrase" which is stored
 *    in SharedPreferences as an encrypted blob
 * 3. On subsequent launches, retrieves and decrypts the passphrase
 * 4. The passphrase is passed to SQLCipher to encrypt/decrypt the database
 *
 * Why not pass the Keystore key directly to SQLCipher?
 * SQLCipher expects a passphrase (byte array), but Keystore keys cannot be
 * exported. So we use a two-layer approach: Keystore protects the passphrase,
 * passphrase protects the database.
 */
@Singleton
class DatabaseKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_ALIAS = "aac4u_db_key"
        private const val PREFS_NAME = "aac4u_db_key_prefs"
        private const val PREFS_KEY_ENCRYPTED = "encrypted_passphrase"
        private const val PREFS_KEY_IV = "passphrase_iv"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val GCM_TAG_LENGTH = 128
    }

    /**
     * Get the database passphrase. Generates one on first call,
     * retrieves from encrypted storage on subsequent calls.
     */
    fun getPassphrase(): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encryptedBase64 = prefs.getString(PREFS_KEY_ENCRYPTED, null)
        val ivBase64 = prefs.getString(PREFS_KEY_IV, null)

        return if (encryptedBase64 != null && ivBase64 != null) {
            // Retrieve existing passphrase
            decryptPassphrase(
                Base64.decode(encryptedBase64, Base64.NO_WRAP),
                Base64.decode(ivBase64, Base64.NO_WRAP)
            )
        } else {
            // First launch — generate and store a new passphrase
            generateAndStorePassphrase()
        }
    }

    /**
     * Generate a random 32-byte passphrase, encrypt it with the Keystore key,
     * and store the encrypted blob in SharedPreferences.
     */
    private fun generateAndStorePassphrase(): ByteArray {
        // Generate a random passphrase (this is what SQLCipher will use)
        val passphrase = ByteArray(32)
        java.security.SecureRandom().nextBytes(passphrase)

        // Ensure the Keystore key exists
        ensureKeystoreKey()

        // Encrypt the passphrase with the Keystore key
        val keystoreKey = getKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, keystoreKey)

        val encryptedPassphrase = cipher.doFinal(passphrase)
        val iv = cipher.iv

        // Store encrypted passphrase and IV
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(PREFS_KEY_ENCRYPTED, Base64.encodeToString(encryptedPassphrase, Base64.NO_WRAP))
            .putString(PREFS_KEY_IV, Base64.encodeToString(iv, Base64.NO_WRAP))
            .apply()

        return passphrase
    }

    /**
     * Decrypt the stored passphrase using the Keystore key.
     */
    private fun decryptPassphrase(encryptedData: ByteArray, iv: ByteArray): ByteArray {
        val keystoreKey = getKeystoreKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, keystoreKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))
        return cipher.doFinal(encryptedData)
    }

    /**
     * Create the AES-256 key in Android Keystore if it doesn't already exist.
     */
    private fun ensureKeystoreKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setUserAuthenticationRequired(false) // No biometric/PIN needed — AAC accessibility
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    /**
     * Retrieve the Keystore key.
     */
    private fun getKeystoreKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }
}
