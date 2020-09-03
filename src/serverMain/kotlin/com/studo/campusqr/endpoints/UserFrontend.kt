package com.studo.campusqr.endpoints

import com.studo.campusqr.common.utils.LocalizedString
import com.studo.campusqr.database.Configuration
import com.studo.campusqr.database.MainDatabase
import com.studo.campusqr.extensions.get
import com.studo.campusqr.extensions.language
import com.studo.campusqr.extensions.runOnDb
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import kotlinx.html.*

suspend fun ApplicationCall.userFrontend() {
  val locationId = parameters["l"]
  val locationName = locationId?.let { id -> getLocationOrNull(id)?.name }
  val language = this.language

  val configs = runOnDb {
    MainDatabase.getCollection<Configuration>().find()
      .filter { it.stringValue != null }
      .associateBy(
        keySelector = { it._id.substringBefore("_$language") },
        valueTransform = { it.stringValue!! })
  }

  respondHtml(HttpStatusCode.OK) {
    lang = language
    headTemplate("Check In", css = "userFrontend/userFrontend.css", js = "userFrontend/userFrontend.js")
    body {
      noScript {
        +"You need to enable JavaScript to run this app."
      }

      if (locationName == null) {
        // Location not found
        div {
          id = "overlay"
          img {
            src = "/static/userFrontend/reload.svg"
          }
          p {
            +LocalizedString(
              "Please scan the QR code again.",
              "Bitte scannen Sie den QR Code erneut."
            ).get(this@userFrontend)
          }
        }
      } else {
        div("hidden") {
          id = "overlay"
          img {
            src = "/static/userFrontend/reload.svg"
          }
          +LocalizedString(
            "Please close this page and scan the QR code again.",
            "Bitte schließen Sie dieses Fenster und scannen Sie den QR Code erneut."
          ).get(this@userFrontend)
        }
        div("header") {
          img {
            src = configs.getValue("logoUrl")
            alt = "Logo"
          }
          h2 {
            +"Campus Check-in"
          }
        }
        div("content") {
          div {
            id = "form"
            div("location-wrapper") {
              div("icon-wrap") {
                img {
                  src = "/static/userFrontend/locationIcon.svg"
                }
              }
              span("location") {
                +LocalizedString(
                  "Location: ",
                  "Ort: "
                ).get(this@userFrontend)
                b { +locationName }
              }
            }

            div("form-email") {
              input {
                id = "email-input"
                name = "email"
                placeholder = configs.getValue("emailPlaceholder") // "user@example.com"
                type = InputType.email
                autoFocus = true
              }
            }
            button(classes = "submit") {
              id = "submit-button"
              type = ButtonType.button
              +LocalizedString("Check in at $locationName", "Check in bei $locationName").get(this@userFrontend)
            }
            div("form-acceptTos") {
              checkBoxInput {
                name = "accept-tos"
                id = "accept-tos-checkbox"
              }
              div {
                label {
                  htmlFor = "accept-tos-checkbox"
                  val tosText = configs.getValue("userTosText")
                  if (tosText.contains("<") && tosText.contains(">")) {
                    +tosText.substringBefore("<")
                    a {
                      target = "_blank"
                      href = configs.getValue("userTosUrl")
                      +tosText.substringAfter("<").substringBefore(">")
                    }
                    +tosText.substringAfter(">")
                  } else {
                    a {
                      target = "_blank"
                      href = configs.getValue("userTosUrl")
                      +tosText
                    }
                  }
                }
              }
            }
          }
          div("result hidden") {
            id = "result-ok"
            div("icon-wrap") {
              img {
                src = "/static/userFrontend/smileIcon.svg"
              }
            }
            span {
              +LocalizedString("Check-in successful!", "Check-in erfolgreich!").get(this@userFrontend)
            }
          }
          span("result hidden") {
            id = "result-fail"
            +LocalizedString(
              "Error! Please try again.",
              "Fehler! Bitte versuche es erneut."
            ).get(this@userFrontend)
          }
        }
      }
      footer {
        val userFooterAdditionalInfoUrl = configs.getValue("userFooterAdditionalInfoUrl")
        if (userFooterAdditionalInfoUrl.isNotEmpty()) {
          a {
            id = "what-if-infected"
            target = "_blank"
            href = userFooterAdditionalInfoUrl
            +configs.getValue("userFooterAdditionalInfoText")
          }
        }
        button {
          id = "lang-select"
          value = when (language) {
            "en" -> "de"
            else -> "en"
          }
          +when (language) {
            "en" -> "Auf Deutsch ansehen"
            else -> "Switch to English"
          }
        }
        a {
          target = "_blank"
          href = configs.getValue("imprintUrl")
          +configs.getValue("imprintText")
        }
      }
    }
  }
}