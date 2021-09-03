package com.studo.campusqr.common.payloads

import com.studo.campusqr.common.ClientPayload
import kotlinx.serialization.Serializable

@Serializable
class NewUserData(
  val email: String,
  val name: String,
  val password: String,
  val permissions: List<String>,
) : ClientPayload

@Serializable
class EditUserData(
  val userId: String? = null,
  val name: String?,
  val password: String?,
  val permissions: List<String>?,
) : ClientPayload

@Serializable
class DeleteUserData(
  val userId: String
) : ClientPayload