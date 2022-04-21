package webcore

import csstype.*
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
  var config: MbSnackbarConfig
}

private class MbSnackbar : RComponent<MbSnackbarProps, State>() {
  override fun ChildrenBuilder.render() {
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
              MbSnackbarType.SUCCESS -> backgroundColor = Color(greenColor[500] as String)
              MbSnackbarType.ERROR -> backgroundColor = Color(redColor[500] as String)
              MbSnackbarType.INFO -> backgroundColor = Color(blueColor[500] as String)
              MbSnackbarType.WARNING -> {
                color = NamedColor.black
                backgroundColor = Color(yellowColor[500] as String)
              }
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
                  marginRight = 8.px
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

// you only need to define one snackbar in your page
// control the message, type, positions through state variables
fun ChildrenBuilder.mbSnackbar(config: MbSnackbarConfig) {
  MbSnackbar::class.react {
    this.config = config
  }
}
