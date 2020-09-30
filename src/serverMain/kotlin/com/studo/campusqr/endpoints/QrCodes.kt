package com.studo.campusqr.endpoints

import com.studo.campusqr.common.ClientLocation
import com.studo.campusqr.common.utils.LocalizedString
import com.studo.campusqr.database.getConfigs
import com.studo.campusqr.extensions.get
import com.studo.campusqr.extensions.language
import com.studo.campusqr.extensions.respondForbidden
import com.studo.campusqr.utils.AuthenticatedApplicationCall
import io.ktor.html.*
import io.ktor.http.*
import kotlinx.html.*

suspend fun AuthenticatedApplicationCall.viewSingleQrCode() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  val location = parameters["id"]?.let { getLocation(it) }?.toClientClass(language)
  val configs = getConfigs(language)

  respondHtml(HttpStatusCode.OK) {
    lang = language
    headTemplate("View Code", js = "viewQR/qrcode.min.js", css = "viewQR/styles.css", async = false) {
      meta(name = "qrCodeBaseUrl", content = configs.getValue("qrCodeBaseUrl"))
    }

    body {
      if (location == null) {
        p {
          +LocalizedString(
            "This location does not exist. Please go back and try again.",
            "Diese Location existiert nicht, bitte gehe zurück und versuche es erneut."
          ).get(this@viewSingleQrCode)
        }
      } else {
        renderQrCodes(listOf(location), configs, language)
      }
    }
  }
}

suspend fun AuthenticatedApplicationCall.viewAllQrCodes() {
  if (!sessionToken.isAuthenticated) {
    respondForbidden()
    return
  }

  val locations = getAllLocations(language)
  val configs = getConfigs(language)

  respondHtml(HttpStatusCode.OK) {
    lang = language
    headTemplate("View Code", js = "viewQR/qrcode.min.js", css = "viewQR/styles.css", async = false) {
      meta(name = "qrCodeBaseUrl", content = configs.getValue("qrCodeBaseUrl"))
    }

    body {
      if (locations.isEmpty()) {
        p {
          +LocalizedString(
            "No locations have been added yet. Go back to create some.",
            "Es sind noch keine Orte eingetragen. Gehen Sie zurück um welche zu erstellen."
          ).get(this@viewAllQrCodes)
        }
      } else {
        renderQrCodes(locations, configs, language)
      }
    }
  }
}

fun BODY.renderQrCodes(locations: List<ClientLocation>, configs: Map<String, String>, language: String) {
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
            "Die QR Codes werden auf verschiedene Seiten aufgeteilt und dieser Hinweis wird nicht mitgedruckt."
      ).get(language)
    }
    p {
      id = "loading-text"
      +LocalizedString("Loading... ", "Lädt... ").get(language)
    }
  }
  div("hidden") {
    id = "all-codes"
    for (location in locations) {
      renderLocation(location, configs)
    }
  }
  script {
    src = "/static/viewQR/generate.js"
  }
}


fun DIV.renderLocation(location: ClientLocation, configs: Map<String, String>) {
  fun renderLocationInternal(name: String, id: String) {
    div("page") {
      div("header") {
        h1 {
          +name
        }
        p {
          +"Check In"
        }
      }
      div("qrcode") {
        this.id = id
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

  if (location.seatCount != null) {
    for (seat in 1..location.seatCount) {
      val paddedSeat = seat.toString().padStart(location.seatCount.toString().length, '0')
      renderLocationInternal("${location.name} #$paddedSeat", "${location.id}-$seat")
    }
  } else {
    renderLocationInternal(location.name, location.id)
  }
}