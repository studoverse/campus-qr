package com.studo.campusqr.extensions

import com.studo.campusqr.common.utils.LocalizedString
import io.ktor.application.*

fun LocalizedString.get(call: ApplicationCall): String = when (call.language) {
  "de" -> de ?: en
  else -> en
}