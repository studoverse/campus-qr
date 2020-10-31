package com.studo.campusqr.database

import com.studo.campusqr.auth.CampusQrAuth
import com.studo.campusqr.auth.getAuthProvider
import com.studo.campusqr.common.UserRole
import com.studo.campusqr.extensions.runOnDb
import com.studo.campusqr.utils.Algorithm
import com.studo.katerbase.sha256
import java.util.*
import kotlin.streams.toList

/**
 * Add configuration default values, but keep values if they are already in the database
 */
suspend fun initialDatabaseSetup() {
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
      insert("emailAccessRegex", "") // Set to e.g. ".+@(student\.)?example\.org" to allow asdf@student.example.org and asdf@example.org
      insert(
        "scanSubtext1",
        "Bitte scannen Sie diesen QR Code beim Betreten des Raumes mit der Kamera-App Ihres Smartphones oder einer QR Code App. Folgen Sie dann den Anweisungen auf der gescannten Website."
      )
      insert(
        "scanSubtext2",
        "Please scan this QR Code when entering the room with the camera app of your smartphone or a QR Code app. Follow then the instructions on the scanned website."
      )
      insert(
        "scanCheckoutSubtext1",
        "Bitte scannen Sie diesen QR Code beim Verlassen des Raumes mit der Kamera-App Ihres Smartphones oder einer QR Code App. Wählen Sie dann \"Auschecken\" auf der gescannten Website."
      )
      insert(
        "scanCheckoutSubtext2",
        "Please scan this QR Code when leaving the room with the camera app of your smartphone or a QR Code app. Then select \"Check out\" on the scanned website."
      )
      insert("showVerificationAnimation", 1)

      insert("baseUrl", "http://127.0.0.1:8070")
      insert("qrCodeBaseUrl", "http://127.0.0.1:8070") // Used for qr code generation
      insert("appName", "Campus QR")

      insert("deleteCheckInDataAfterDays", 4 * 7)

      insert("autoCheckOutMinutes", 3 * 60) // 3 Hours
      insert("transitThresholdSeconds", 2 * 60) // 2 Minutes

      insert("csrfHashSecret", Algorithm.secureRandom.longs(64).toList().joinToString().sha256())

      insert("ldapUrl", "") // "" = ldap disabled. E.g. "ldap://ldap.forumsys.com:389"
      insert("ldapSearchFilter", "(uid=%s,dc=example,dc=com)") // For user authentication. ";"-separated
      insert("ldapApplicationUserPrincipal", "cn=read-only-admin,dc=example,dc=com") // For user disabling via lookup
      insert("ldapApplicationUserCredentials", "password") // For user disabling via lookup
      insert("ldapGroupAttributeName", "memberOf") // LDAP attribute-name of the user
      insert("ldapGroupFilter", "(objectclass=user)")
      insert("ldapGroupRegex", "CN=CampusQR,") // At least one attribute must match this regex to authenticate with success
      insert("ldapPrintDebugLogs", 0)
      insert("ldapTimeoutMs", 10_000)
      insert("ldapUserDisablingIntervalMinutes", 24 * 60)
      insert("ldapDefaultUserType", UserRole.EDIT_OWN_ACCESS.toString()) // For users who sign up via ldap

      insert("storeCheckInUserAgent", 0) // Set to 1 if UserAgent should be stored on checkIn
      insert("checkInIpAddressHeader", "") // Set to "X-Forwarded-For" (or custom) if IP address should be stored on checkIn

      insert("authSharedSecret", "") // Auth via X-Authorization header. If empty, no shared secret access is possible.
    }

    // Create root user
    with(getCollection<BackendUser>()) {
      val rootUser = BackendUser().apply {
        _id = "rootUser"
        email = "admin@example.org"
        passwordHash = Algorithm.hashPassword("admin")
        name = "Root User"
        createdDate = Date()
        createdBy = _id
        roles = UserRole.values().toSet()
      }
      // Only create new user when we have no user yet, and only if we use built-in username/password auth and no thirdparty-auth provider
      if (count() == 0L && getAuthProvider() is CampusQrAuth) {
        insertOne(rootUser, onDuplicateKey = { })
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
