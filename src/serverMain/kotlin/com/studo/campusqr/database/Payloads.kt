package com.studo.campusqr.database

import com.studo.campusqr.common.*
import com.studo.campusqr.common.utils.LocalizedString
import com.studo.campusqr.extensions.toAustrianTime
import com.studo.katerbase.MongoMainEntry
import com.studo.katerbase.MongoSubEntry
import java.util.*

/**
 * This file contains all types of payloads from the [MainDatabase] in form of Kotlin classes.
 * These classes are allowed to have additional functions and computed properties.
 */
interface ClientPayloadable<out T : ClientPayload> {
  fun toClientClass(language: String): T
}

class BackendUser() : MongoMainEntry(), ClientPayloadable<ClientUser> {
  lateinit var email: String // Use email as primary key. Email can not be changed.
  var passwordHash: String? = null
  lateinit var createdDate: Date
  var createdBy: String? = null // userId (null on ldap)
  lateinit var name: String
  var firstLoginDate: Date? = null
  var type = UserType.MODERATOR
  override fun toClientClass(language: String) = ClientUser(
    id = _id,
    email = email,
    name = name,
    type = type.name,
    firstLoginDate = firstLoginDate?.toAustrianTime("dd.MM.yyyy")
      ?: LocalizedString(
        "Not logged in yet",
        "Noch nicht eingeloggt"
      ).get(language)
  )

  constructor(userId: String, email: String, name: String, type: UserType) : this() {
    this.email = email
    this._id = userId
    this.name = name
    this.createdDate = Date()
    this.type = type
  }

  val isAdmin get() = type == UserType.ADMIN
  val isModerator get() = type == UserType.MODERATOR || isAdmin
}

class BackendLocation : MongoMainEntry(), ClientPayloadable<ClientLocation> {
  lateinit var name: String
  lateinit var createdBy: String // userId
  lateinit var createdDate: Date
  var checkInCount: Int = 0
  var accessType = LocationAccessType.FREE

  override fun toClientClass(language: String) = ClientLocation(
    id = _id,
    name = name,
    checkInCount = checkInCount,
    accessType = accessType.name
  )
}

class BackendAccess : MongoMainEntry(), ClientPayloadable<ClientAccessManagement> {
  lateinit var locationId: String
  lateinit var createdBy: String // userId
  lateinit var createdDate: Date
  lateinit var allowedEmails: List<String>
  lateinit var dateRanges: List<DateRange>
  lateinit var note: String
  lateinit var reason: String

  override fun toClientClass(language: String) = throw NotImplementedError()
}

class CheckIn : MongoMainEntry() {
  lateinit var locationId: String
  lateinit var date: Date
  lateinit var email: String
  lateinit var userAgent: String
  var grantAccessId: String? = null // id of BackendAccess which was used to enter, null if no BackendAccess was used
}

class SessionToken : MongoMainEntry() {
  var userId: String? = null
  var creationDate: Date = Date()
  lateinit var expiryDate: Date

  val isAuthenticated: Boolean get() = !userId.isNullOrEmpty()
}

class Configuration : MongoMainEntry {
  var stringValue: String? = null
  var intValue: Int? = null

  constructor()

  constructor(id: String, value: String) {
    _id = id
    stringValue = value
  }

  constructor(id: String, value: Int) {
    _id = id
    intValue = value
  }

  constructor(id: String, value: Boolean) {
    _id = id
    intValue = if (value) 1 else 0
  }
}

class DateRange(var from: Date, var to: Date) : MongoSubEntry(), ClientPayloadable<ClientDateRange> {
  constructor(dateRange: ClientDateRange) : this(from = Date(dateRange.from.toLong()), to = Date(dateRange.to.toLong()))

  override fun toClientClass(language: String) = toClientClass()
  fun toClientClass() = ClientDateRange(from = from.time.toDouble(), to = to.time.toDouble())
}