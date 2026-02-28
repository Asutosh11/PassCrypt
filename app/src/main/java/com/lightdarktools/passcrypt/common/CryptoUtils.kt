package com.lightdarktools.passcrypt.common

import android.util.Base64
import java.security.SecureRandom
import java.security.spec.KeySpec
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.nio.ByteBuffer

object CryptoUtils {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12
    private const val SALT_LENGTH_BYTE = 16
    private const val PBKDF2_ITERATIONS = 10000
    private const val KEY_LENGTH_BIT = 256

    fun encrypt(data: String, pin: String): String {
        val salt = ByteArray(SALT_LENGTH_BYTE)
        SecureRandom().nextBytes(salt)
        
        val key = deriveKey(pin, salt)
        val iv = ByteArray(IV_LENGTH_BYTE)
        SecureRandom().nextBytes(iv)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        
        val ciphertext = cipher.doFinal(data.toByteArray(Charsets.UTF_8))
        
        // Combine salt + iv + ciphertext
        val result = ByteBuffer.allocate(salt.size + iv.size + ciphertext.size)
            .put(salt)
            .put(iv)
            .put(ciphertext)
            .array()
            
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    fun decrypt(encryptedData: String, pin: String): String {
        val decoded = Base64.decode(encryptedData, Base64.NO_WRAP)
        val buffer = ByteBuffer.wrap(decoded)
        
        val salt = ByteArray(SALT_LENGTH_BYTE)
        buffer.get(salt)
        
        val iv = ByteArray(IV_LENGTH_BYTE)
        buffer.get(iv)
        
        val ciphertext = ByteArray(buffer.remaining())
        buffer.get(ciphertext)
        
        val key = deriveKey(pin, salt)
        
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH_BIT, iv))
        
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }

    private fun deriveKey(pin: String, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec: KeySpec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH_BIT)
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    fun generateQRCode(content: String, size: Int): android.graphics.Bitmap {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }
}
