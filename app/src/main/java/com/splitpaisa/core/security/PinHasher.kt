package com.splitpaisa.core.security

import java.security.MessageDigest
import java.security.SecureRandom

object PinHasher {
    private val random = SecureRandom()

    fun randomSalt(): ByteArray = ByteArray(16).also { random.nextBytes(it) }

    fun hash(pin: String, salt: ByteArray): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        md.update(pin.toByteArray())
        return md.digest().joinToString(separator = "") { "%02x".format(it) }
    }
}
