package com.studo.campusqr.common.payloads

import kotlinx.serialization.Serializable

@Serializable
class UserData(
  var appName: String,
  var clientUser: ClientUser? = null, // Null when unauthenticated
  var externalAuthProvider: Boolean = false,
  var liveCheckInsViewEnabled: Boolean,
) : ClientPayload

interface ClientPayload

val UserData.isAuthenticated get() = clientUser != null