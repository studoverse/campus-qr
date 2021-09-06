package com.studo.campusqr.common.payloads

import kotlinx.serialization.Serializable

// Used to mark payloads that are sent between server and client
// All ClientPayload implementations are required to be @Serializable
interface ClientPayload

@Serializable
class UserData(
  var appName: String,
  var clientUser: ClientUser? = null, // Null when unauthenticated
  var externalAuthProvider: Boolean = false,
  var liveCheckInsViewEnabled: Boolean,
) : ClientPayload

val UserData.isAuthenticated get() = clientUser != null