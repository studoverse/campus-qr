package com.studo.campusqr.endpoints

import com.studo.campusqr.common.utils.LocalizedString
import io.ktor.application.*
import io.ktor.html.*
import io.ktor.http.*
import kotlinx.html.*
import com.studo.campusqr.extensions.get

suspend fun ApplicationCall.index() {
  respondHtml(HttpStatusCode.OK) {
    headTemplate(null, js = "index/index.js", css = "index/index.css")
    body {
      div("content") {
        div("icon") {
          img {
            src = "/static/images/logo_campusqr.png"
            alt = "logo"
          }
        }
        h1("header") {
          +LocalizedString("Campus QR").get(this@index)
        }
        div("what") {
          p {
            +LocalizedString(
              "Campus QR is an open source solution for attendance tracking and infection tracing in the environment of a university.",
              "Campus QR ist eine Open Source Lösung für Anwesenheits- und Infektionstracking für Universitäten und Hochschulen."
            ).get(this@index)
          }
        }
        p {
          +LocalizedString("To view more information about Campus QR, ", "Um mehr über Campus QR zu erfahren, ").get(
            this@index
          )
          a {
            href = "https://github.com/studo-app/campus-qr"
            target = "_blank"
            +LocalizedString("click here", "klicken Sie hier").get(this@index)
          }
          +"."
        }
        a {
          href = "/admin"
          div {
            classes = setOf("actionlink")
            +"Moderator Dashboard"
          }
        }
      }
      div("background") {}
      footer {
        span {
          +"Created by "
          a {
            href = "https://studo.com"
            target = "_blank"
            +"Studo"
          }
        }
      }
    }
  }
}

fun HTML.headTemplate(subtitle: String?, js: String, css: String, async: Boolean = true, block: HEAD.() -> Unit = {}) {
  head {
    meta {
      charset = "utf-8"
    }
    style { unsafe { +"html { visibility: hidden }" } }
    styleLink(url = "https://fonts.googleapis.com/css2?family=Roboto:ital,wght@0,400;0,700;1,400&display=swap")
    styleLink(url = "/static/normalize.min.css")
    styleLink(url = "/static/$css")
    script(src = "/static/$js") { // Library
      this.async = async
    }
    title {
      +("Campus QR" + (subtitle?.let { ": $it" } ?: ""))
    }
    meta {
      name = "viewport"
      content = "width=device-width, initial-scale=1, shrink-to-fit=no"
    }
    meta {
      name = "theme-color"
      content = "#2196F3"
    }
    link {
      rel = "icon"
      type = "image/png"
      sizes = "64x64"
      href = "/static/images/favicon.png"
    }
    block()
  }
}