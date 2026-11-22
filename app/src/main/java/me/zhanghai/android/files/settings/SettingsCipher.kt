/*
 * Copyright (c) 2026 Material Files contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package me.zhanghai.android.files.settings

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts sensitive settings (such as saved server credentials) with an AES-GCM key kept in the
 * Android Keystore, so that the key cannot be extracted from the device.
 *
 * Output format: 1 byte version, 12 bytes IV, then the ciphertext with its authentication tag.
 */
internal object SettingsCipher {
    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "settings_encryption_key"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val VERSION: Byte = 1
    private const val IV_SIZE = 12
    private const val TAG_SIZE_BITS = 128

    private val keyLock = Any()

    @Throws(GeneralSecurityException::class)
    fun encrypt(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        if (iv.size != IV_SIZE) {
            throw GeneralSecurityException("Unexpected IV size ${iv.size}")
        }
        val ciphertext = cipher.doFinal(plaintext)
        return ByteArray(1 + IV_SIZE + ciphertext.size).also {
            it[0] = VERSION
            iv.copyInto(it, 1)
            ciphertext.copyInto(it, 1 + IV_SIZE)
        }
    }

    @Throws(GeneralSecurityException::class)
    fun decrypt(data: ByteArray): ByteArray {
        if (data.size < 1 + IV_SIZE || data[0] != VERSION) {
            throw GeneralSecurityException("Unsupported encrypted data")
        }
        val key = getKey() ?: throw GeneralSecurityException("Missing settings encryption key")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_SIZE_BITS, data, 1, IV_SIZE)
        )
        return cipher.doFinal(data, 1 + IV_SIZE, data.size - 1 - IV_SIZE)
    }

    @Throws(GeneralSecurityException::class)
    private fun getKey(): SecretKey? {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply { load(null) }
        return keyStore.getKey(KEY_ALIAS, null) as SecretKey?
    }

    @Throws(GeneralSecurityException::class)
    private fun getOrCreateKey(): SecretKey =
        synchronized(keyLock) {
            getKey() ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
                .apply {
                    init(
                        KeyGenParameterSpec.Builder(
                            KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                        )
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build()
                    )
                }
                .generateKey()
        }
}
