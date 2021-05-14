package com.studo.campusqr.utils

import org.apache.commons.codec.binary.Hex
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Algorithm {
  val secureRandom = SecureRandom()
  private val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")

  // Similar looking characters omitted
  private const val characterSet = "23456789abcdefghkmnpqrstwxyzABCDEFGHKMNPQRSTWXYZ"

  private const val pwKeyLen = 64 * 8 // 64 byte
  private const val pbeIterations = 2000

  fun hashPassword(password: String): String {
    val salt = (1..10).joinToString(separator = "", transform = { characterSet[secureRandom.nextInt(characterSet.length)].toString() })
    val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), pbeIterations, pwKeyLen)
    val hash = Hex.encodeHexString(keyFactory.generateSecret(spec).encoded)

    return "$pbeIterations:$salt:$hash"
  }

  fun validatePassword(password: String, passwordHash: String): Boolean {
    val (iterations, salt, hash) = passwordHash.split(':')
    val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), iterations.toInt(), pwKeyLen)
    val secret = Hex.encodeHexString(keyFactory.generateSecret(spec).encoded)

    return (secret == hash)
  }
}