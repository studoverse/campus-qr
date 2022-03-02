package webcore

import kotlinext.js.js
import react.*
import react.dom.html.ReactHTML.span
import webcore.materialUI.*

data class MbSnackbarConfig(
  var message: String = "",
  var show: Boolean = false,
  var position: MbSnackbarAlignment = MbSnackbarAlignment("bottom", "center"),
  var snackbarType: MbSnackbarType? = null,
  var onClose: () -> Unit, // must be provided. managing the state must be handled outside the component
  // provide more than a simple text to show in snackbar
  // it will override previous message variable
  var complexMessage: (RBuilder.() -> ReactElement<*>)? = null
)

data class MbSnackbarAlignment(val vertical: String, val horizontal: String)

enum class MbSnackbarType(val icon: ComponentClass<IconProps>?) {
  SUCCESS(checkCircleIcon),
  ERROR(errorIcon),
  INFO(infoIcon),
  WARNING(warningIcon)
}

external interface MbSnackbarProps : Props {
  var theme: dynamic
  var classes: MbSnackbarClasses
  var config: MbSnackbarConfig
}

class MbSnackbar : RComponent<MbSnackbarProps, State>() {
  override fun RBuilder.render() {
    snackbar {
      attrs.anchorOrigin = js {
        vertical = props.config.position.vertical
        horizontal = props.config.position.horizontal
      }
      attrs.classes = js {
        root = props.classes.root
      }
      attrs.open = props.config.show
      attrs.autoHideDuration = 3000
      attrs.onClose = {
        props.config.onClose()
      }
      snackbarContent {
        attrs {
          className = when (props.config.snackbarType) {
            MbSnackbarType.SUCCESS -> props.classes.success
            MbSnackbarType.ERROR -> props.classes.error
            MbSnackbarType.INFO -> props.classes.info
            MbSnackbarType.WARNING -> props.classes.warning
            null -> ""
          }
          message = span.create {
            // TODO: @mh Test if this is really displayed correctly
            className = props.classes.message
            props.config.snackbarType?.icon?.let { it { className = props.classes.icon } }
            +props.config.message
          }
          props.config.complexMessage?.let { message = it() }
        }
      }
    }
  }
}

external interface MbSnackbarClasses {
  var root: String
  var success: String
  var error: String
  var info: String
  var loading: String
  var warning: String
  var icon: String
  var message: String
  var centerGridItem: String
}

private val style = { theme: dynamic ->
  js {
    root = js {
      marginBottom = "20px"
    }
    success = js {
      backgroundColor = greenColor[500]
    }
    error = js {
      backgroundColor = redColor[500]
    }
    info = js {
      backgroundColor = blueColor[500]
    }
    loading = js {
      backgroundColor = theme.palette.secondary.main
    }
    warning = js {
      color = "black"
      backgroundColor = yellowColor[500]
    }
    icon = js {
      fontSize = 20
      opacity = 0.9
      marginRight = theme.spacing(1)
    }
    message = js {
      display = "flex"
      alignItems = "center"
    }
    centerGridItem = js {
      alignSelf = "center"
    }
  }
}

private val styledSnackbar = withStyles<MbSnackbarProps, MbSnackbar>(style, options = js { withTheme = true })

// you only need to define one snackbar in your page
// control the message, type, positions through state variables
fun RBuilder.mbSnackbar(config: MbSnackbarConfig) = styledSnackbar {
  attrs.config = config
}