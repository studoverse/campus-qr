package com.studo.campusqr.endpoints

import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.utils.LocalizedString
import com.studo.campusqr.database.getConfigs
import com.studo.campusqr.extensions.get
import com.studo.campusqr.extensions.language
import com.studo.campusqr.extensions.respondForbidden
import com.studo.campusqr.utils.getSessionToken
import com.studo.campusqr.utils.isAuthenticated
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import kotlinx.html.*

suspend fun ApplicationCall.viewSingleQrCode() {
  if (!getSessionToken().isAuthenticated) {
    respondForbidden()
    return
  }

  val location = parameters["id"]?.let { getLocation(it) }?.toClientClass(language)
  val configs = getConfigs(language)

  respondHtml(HttpStatusCode.OK) {
    lang = language
    headTemplate("View Code", js = "viewQR/qrcode.min.js", css = "viewQR/styles.css", async = false)

    body {
      if (location == null) {
        p {
          +LocalizedString(
              "This location does not exist. Please go back and try again.",
              "Diese Location existiert nicht, bitte gehe zurück und versuche es erneut.").get(this@viewSingleQrCode)
        }
      } else {
        noScript {
          +"You need to enable JavaScript to run this app."
        }
        header("noprint") {
          h2 {
            +"QR Code"
          }
          p {
            +LocalizedString(
              "This website is suited for printing. This hint will not be printed.",
              "Diese Website ist zum Drucken geeignet. Dieser Hinweis wird nicht gedruckt."
            ).get(this@viewSingleQrCode)
          }
        }
        renderLocation(location, configs)
        script {
          src = "/static/viewQR/generate.js"
        }
      }
    }
  }
}

suspend fun ApplicationCall.viewAllQrCodes() {
  if (!getSessionToken().isAuthenticated) {
    respondForbidden()
    return
  }

  val locations = getAllLocations(language)
  val configs = getConfigs(language)

  respondHtml(HttpStatusCode.OK) {
    lang = language
    headTemplate("View Code", js = "viewQR/qrcode.min.js", css = "viewQR/styles.css", async = false)

    body {
      if (locations.isEmpty()) {
        p {
          +LocalizedString(
              "You have no locations yet. Go back and add some!",
              "Es sind noch keine Locations eingetragen. Geh zurück und erstelle ein paar!"
          ).get(this@viewAllQrCodes)
        }
      } else {
        noScript {
          +"You need to enable JavaScript to run this app."
        }
        header("noprint") {
          h2 {
            +"QR Codes"
          }
          p {
            +LocalizedString(
                "This website is suited for printing. " +
                    "The QR codes will be put on separate pages and this hint will not be printed.",
                "Diese Website ist zum Drucken geeignet. " +
                    "Die QR Codes werden auf verschiedene Seiten aufgeteilt und dieser Hinweis wird nicht mitgedruckt.")
                .get(this@viewAllQrCodes)
          }
        }
        for (location in locations) {
          renderLocation(location, configs)
        }
        script {
          src = "/static/viewQR/generate.js"
        }
      }
    }
  }
}

fun BODY.renderLocation(location: ClientLocation, configs: Map<String, String>) {
  div("page") {
    div("header") {
      p {
        span {
          +location.name
        }
      }
      p {
        +"Check In"
      }
    }
    div("qrcode") {
      id = location.id
    }
    div("footer") {
      p {
        +configs.getValue("scanSubtext1")
      }
      p {
        +configs.getValue("scanSubtext2")
      }
    }
  }
  div("break") {}
}