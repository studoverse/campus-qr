package com.studo.campusqr.common.payloads

import kotlinx.serialization.Serializable

@Serializable
class MailLoginData(
  val email: String,
  val password: String
) : ClientPayload