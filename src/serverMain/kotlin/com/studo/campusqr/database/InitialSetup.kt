package com.studo.campusqr.database

import com.studo.campusqr.common.UserType
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.serverScope
import com.studo.campusqr.utils.Algorithm
import com.studo.katerbase.equal
import com.studo.katerbase.sha256
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.streams.toList

const val DEMO_MODE = 1

/**
 * Add configuration default values, but keep values if they are already in the database
 */
suspend fun initialDatabaseSetup() {
  val now = Date()

  runOnDb {
    with(getCollection<Configuration>()) {
      fun insert(id: String, value: String) {
        insertOne(
          Configuration(id, value),
          onDuplicateKey = { }) // Don't do anything if Configuration is already in database
      }

      fun insert(id: String, value: Int) {
        insertOne(
          Configuration(id, value),
          onDuplicateKey = { }) // Don't do anything if Configuration is already in database
      }

      insert("userTosText_en", "I agree to the processing of my data according to the <privacy policy>.")
      insert("userTosText_de", "Ich stimme der Verarbeitung meiner Daten gemäß der <Datenschutzerklärung> zu.")
      insert("userTosUrl_en", "https://example.org")
      insert("userTosUrl_de", "https://example.org")
      insert("imprintText_en", "Imprint & Privacy")
      insert("imprintText_de", "Impressum & Datenschutz")
      insert("imprintUrl_en", "https://example.org")
      insert("imprintUrl_de", "https://example.org")
      insert("logoUrl_en", "/static/userFrontend/universityIcon.svg")
      insert("logoUrl_de", "/static/userFrontend/universityIcon.svg")
      insert("userAppName_en", "Campus QR Corona Tracking")
      insert("userAppName_de", "Campus QR Corona Tracking")
      insert("userFooterAdditionalInfoUrl", "")
      insert("userFooterAdditionalInfoText_en", "")
      insert("userFooterAdditionalInfoText_de", "")
      insert("emailPlaceholder_en", "Your university email address")
      insert("emailPlaceholder_de", "Deine Hochschul E-Mail Adresse")
      insert(
        "scanSubtext1",
        "Bitte scannen Sie diesen QR Code beim Betreten des Raumes mit der Kamera-App Ihres Smartphones oder einer QR Code App. Folgen Sie dann den Anweisungen auf der gescannten Website."
      )
      insert(
        "scanSubtext2",
        "Please scan this QR Code when entering the room with the camera app of your smartphone or a QR Code app. Follow then the instructions on the scanned website."
      )

      insert("baseUrl", "http://127.0.0.1:8070")
      insert("appName", "Campus QR")
      insertOne(Configuration("demoMode", DEMO_MODE), upsert = true)

      insert("deleteCheckInDataAfterDays", 4 * 7)

      insert("previousInfectionHours", 3)
      insert("nextInfectionHours", 12)

      insert("csrfHashSecret", Algorithm.secureRandom.longs(64).toList().joinToString().sha256())

      insert("ldapUrl", "") // "" = ldap disabled. E.g. "ldap://ldap.forumsys.com:389"
      insert("ldapSearchFilter", "(uid=%s,dc=example,dc=com)") // For user authentication
      insert("ldapApplicationUserPrincipal", "cn=read-only-admin,dc=example,dc=com") // For user disabling via lookup
      insert("ldapApplicationUserCredentials", "password") // For user disabling via lookup
      insert("ldapUserDisablingIntervalMinutes", 24 * 60)
    }


    // Create root or demo user
    with(getCollection<BackendUser>()) {
      if (DEMO_MODE == 1) {
        // Delete root user so there is no other way to log in
        // TODO decide if we should just drop all users if in demo mode?
        deleteOne(BackendUser::_id equal "rootUser")
        val user =
          BackendUser().apply {
            _id = "demoUser"
            email = "demo@example.org"
            passwordHash = Algorithm.hashPassword("demo")
            name = "Demo User"
            createdDate = now
            createdBy = _id
            type = UserType.MODERATOR
          }
        insertOne(user, upsert = true)
      } else {
        deleteOne(BackendUser::_id equal "demoUser")
        if (count() == 0L) {
          val user =
            BackendUser().apply {
              _id = "rootUser"
              email = "admin@example.org"
              passwordHash = Algorithm.hashPassword("admin")
              name = "Root User"
              createdDate = now
              createdBy = _id
              type = UserType.ADMIN
            }
          insertOne(user, onDuplicateKey = { })
        }
      }

      if (DEMO_MODE == 1) {
        resetLocationsPeriodic()
      }
    }
  }
}

// Resets locations db on start and every 24 hours
suspend fun resetLocationsPeriodic() = serverScope.launch {
  suspend fun resetLocations() {
    val now = Date()

    runOnDb {
      with(getCollection<BackendLocation>()) {
        drop()

        fun createLocation(name: String): BackendLocation {
          return BackendLocation().apply {
            _id = name
            this.name = name
            createdBy = ""
            createdDate = now
          }
        }

        (300..320).map { createLocation("Room $it") }.forEach {
          insertOne(it, onDuplicateKey = {})
        }
      }
    }
  }

  var errors = 0

  while (true) {
    try {
      resetLocations()
      delay(timeMillis = 24 * 60 * 60 * 1000L) // 24 hours
      errors = 0
    } catch (e: Exception) {
      println("ERROR resetting locations database. E: $e") // Don't crash on short-term database connection problems

      errors += 1
      if (errors >= 2) {
        Exception("Could not reset database for two days in a row.")
      }
    }
  }
}

suspend fun getConfigs(language: String): Map<String, String> {
  return runOnDb {
    getCollection<Configuration>().find()
      .filter { it.stringValue != null }
      .associateBy(
        keySelector = { it._id.substringBefore("_$language") },
        valueTransform = { it.stringValue!! })
  }
}