package views.common

import csstype.PropertiesBuilder
import web.cssom.TextAlign
import web.cssom.px

object CommonStyles {
  fun PropertiesBuilder.centeredText() {
    paddingLeft = 16.px
    paddingRight = 16.px
    textAlign = TextAlign.center
  }
}
