package com.studo.campusqr.common.utils

class LocalizedString(val en: String, val de: String? = null) {
  fun get(language: String): String = when (language) {
    "de" -> de ?: en
    else -> en
  }

  infix operator fun plus(other: LocalizedString) = LocalizedString(
      en = en + other.en,
      de = de + other.de
  )

  infix operator fun plus(other: String) = LocalizedString(
      en = en + other,
      de = de + other
  )
}

infix operator fun String.plus(other: LocalizedString) = LocalizedString(
    en = this + other.en,
    de = this + other.de
)