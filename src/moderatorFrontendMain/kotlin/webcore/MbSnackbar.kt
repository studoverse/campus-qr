package webcore

import kotlinext.js.js
import react.*
import react.dom.span
import webcore.materialUI.*

interface MbSnackbarProps : RProps {
  data class Config(
      var message: String = "",
      var show: Boolean = false,
      var position: Alignment = Alignment("bottom", "center"),
      var snackbarType: SnackbarType? = null,
      var onClose: () -> Unit, // must be provided. managing the state must be handled outside the component
      // provide more than a simple text to show in snackbar
      // it will override previous message variable
      var complexMessage: (RBuilder.() -> ReactElement)? = null
  )

  data class Alignment(val vertical: String, val horizontal: String)

  enum class SnackbarType(val icon: RClass<IconProps>?) {
    SUCCESS(checkCircleIcon),
    ERROR(errorIcon),
    INFO(infoIcon),
    WARNING(warningIcon)
  }

  var theme: dynamic
  var classes: dynamic
  var config: Config
}

class MbSnackbar : RComponent<MbSnackbarProps, RState>() {
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
            MbSnackbarProps.SnackbarType.SUCCESS -> props.classes.success
            MbSnackbarProps.SnackbarType.ERROR -> props.classes.error
            MbSnackbarProps.SnackbarType.INFO -> props.classes.info
            MbSnackbarProps.SnackbarType.WARNING -> props.classes.warning
            null -> ""
          }
          message = span(classes = props.classes.message) {
            props.config.snackbarType?.icon?.let { it { attrs.className = props.classes.icon } }
            +props.config.message
          }
          props.config.complexMessage?.let { message = it() }
        }
      }
    }
  }
}


private val styles = { theme: dynamic ->
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

private val styledSnackbar = withStyles<MbSnackbarProps, MbSnackbar>(styles, options = js { withTheme = true })

// you only need to define one snackbar in your page
// control the message, type, positions through state variables
fun RBuilder.mbSnackbar(config: MbSnackbarProps.Config) = styledSnackbar {
  attrs.config = config
}