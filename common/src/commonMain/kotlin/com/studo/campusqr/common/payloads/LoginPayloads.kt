package com.studo.campusqr.common.payloads

import com.studo.campusqr.common.ClientPayload
import kotlinx.serialization.Serializable

@Serializable
class MailLoginData(
  val email: String,
  val password: String
) : ClientPayload