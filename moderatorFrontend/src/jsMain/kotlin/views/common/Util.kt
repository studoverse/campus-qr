package views.common

import csstype.PropertiesBuilder
import web.cssom.TextAlign
import web.cssom.px

// TODO: @mh Move to scoped class
fun PropertiesBuilder.centeredText() {
  paddingLeft = 16.px
  paddingRight = 16.px
  textAlign = TextAlign.center
}