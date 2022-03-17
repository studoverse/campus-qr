package webcore

import app.themeContext
import csstype.AlignItems
import csstype.Display
import csstype.number
import csstype.px
import kotlinext.js.js
import kotlinx.js.jso
import mui.icons.material.*
import mui.material.*
import mui.system.sx
import react.*

data class MbSnackbarConfig(
  var message: String = "",
  var show: Boolean = false,
  var position: MbSnackbarAlignment = MbSnackbarAlignment(SnackbarOriginVertical.bottom, SnackbarOriginHorizontal.center),
  var snackbarType: MbSnackbarType? = null,
  var onClose: () -> Unit, // must be provided. managing the state must be handled outside the component
  // provide more than a simple text to show in snackbar
  // it will override previous message variable
  var complexMessage: (ChildrenBuilder.() -> ReactElement<*>)? = null
)

data class MbSnackbarAlignment(val vertical: SnackbarOriginVertical, val horizontal: SnackbarOriginHorizontal)

enum class MbSnackbarType(val icon: SvgIconComponent?) {
  SUCCESS(CheckCircle),
  ERROR(Error),
  INFO(Info),
  WARNING(Warning)
}

external interface MbSnackbarProps : Props {
  var theme: dynamic
  var classes: dynamic
  var config: MbSnackbarConfig
}

class MbSnackbar : RComponent<MbSnackbarProps, State>() {
  override fun ChildrenBuilder.render() {
    themeContext.Consumer {
      children = { theme ->
        Fragment.create {
          Snackbar {
            anchorOrigin = jso {
              vertical = props.config.position.vertical
              horizontal = props.config.position.horizontal
            }
            sx {
              marginBottom = 20.px
            }
            open = props.config.show
            autoHideDuration = 3000
            onClose = { event, reason ->
              props.config.onClose()
            }
            SnackbarContent {
              sx {
                when (props.config.snackbarType) {
                  // TODO: @mh What is greenColor[500] etc. ?
                  MbSnackbarType.SUCCESS -> props.classes.success as String
                  MbSnackbarType.ERROR -> props.classes.error as String
                  MbSnackbarType.INFO -> props.classes.info as String
                  MbSnackbarType.WARNING -> props.classes.warning as String
                  null -> ""
                }
              }
              message = Box.create {
                sx {
                  display = Display.flex
                  alignItems = AlignItems.center
                }
                props.config.snackbarType?.icon?.let {
                  it {
                    sx {
                      fontSize = 20.px
                      opacity = number(0.9)
                      marginRight = theme.spacing(1)
                    }
                  }
                }
                +props.config.message
              }
              props.config.complexMessage?.let { message = it() }

            }
          }
        }
      }
    }
  }
}


private val style = { theme: dynamic ->
  js {
    success = js {
      backgroundColor = greenColor[500]
    }
    error = js {
      backgroundColor = redColor[500]
    }
    info = js {
      backgroundColor = blueColor[500]
    }
    warning = js {
      color = "black"
      backgroundColor = yellowColor[500]
    }
  }
}

// you only need to define one snackbar in your page
// control the message, type, positions through state variables
fun ChildrenBuilder.mbSnackbar(handler: MbSnackbarProps.() -> Unit) {
  MbSnackbar::class.react {
    +jso(handler)
  }
}
