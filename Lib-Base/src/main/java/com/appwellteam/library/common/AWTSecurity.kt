package com.appwellteam.library.common

import android.util.Base64

import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Android-Test
 * Created by sambow on 2017/3/22.
 */

@Suppress("unused")
object AWTSecurity {

    @Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
    var AES_KEY = "210l1sjwn9qn3je8cql1n2v3c4io9x8p"

    @Suppress("MemberVisibilityCanBePrivate", "SpellCheckingInspection")
    var AES_IV = "o9g435a8g7s653sg"

    @Throws(Exception::class)
    fun encryptAES256(plaintext: ByteArray): ByteArray {
        val secretSpec = SecretKeySpec(AES_KEY.toByteArray(charset("UTF-8")), "AES")

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretSpec, IvParameterSpec(AES_IV.toByteArray(charset("UTF-8"))))
        return cipher.doFinal(plaintext)
    }

    @Throws(Exception::class)
    fun decryptAES256(encryptedText: ByteArray): ByteArray? {

        val secretSpec = SecretKeySpec(AES_KEY.toByteArray(charset("UTF-8")), "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretSpec, IvParameterSpec(AES_IV.toByteArray(charset("UTF-8"))))

        var decryptedTextBytes: ByteArray? = null

        try {
            decryptedTextBytes = cipher.doFinal(encryptedText)
        } catch (e: IllegalBlockSizeException) {
            e.printStackTrace()
        } catch (e: BadPaddingException) {
            e.printStackTrace()
        }

        return decryptedTextBytes

    }

    @Throws(Exception::class)
    fun encryptAES256WithBase64(plaintext: String): String {
        return Base64.encodeToString(encryptAES256(plaintext.toByteArray(charset("UTF-8"))), Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decryptAES256WithBase64(encryptedText: String): String {
        return String(decryptAES256(Base64.decode(encryptedText, Base64.DEFAULT)) ?: return "")
    }
}
