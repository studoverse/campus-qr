import csstype.*
import js.objects.unsafeJso
import mui.icons.material.*
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML.span
import webcore.*
import web.cssom.*

class MbSnackbarConfig(
  var message: String = "",
  var position: Alignment = Alignment(SnackbarOriginVertical.bottom, SnackbarOriginHorizontal.center),
  var snackbarType: Type? = null,
  var action: ReactElement<*>? = null,
  var complexMessage: (ChildrenBuilder.() -> ReactElement<*>)? = null
) {
  class Alignment(val vertical: SnackbarOriginVertical, val horizontal: SnackbarOriginHorizontal)

  enum class Type(val icon: SvgIconComponent?) {
    SUCCESS(CheckCircle),
    ERROR(Error),
    INFO(Info),
    WARNING(Warning)
  }
}

external interface MbSnackbarRef {
  fun showSnackbarText(text: String)
  fun showSnackbar(newConfig: MbSnackbarConfig)
  fun closeSnackbar()
}

external interface MbSnackbarProps<T : MbSnackbarRef> : PropsWithRef<T>

/**
 * Snackbar component for handling all snackbars of the application.
 *
 * You only need to define one snackbar in your page.
 * Control the message, type, positions through state variables.
 *
 * The snackbar always closes automatically after [SnackbarProps.autoHideDuration].
 * [MbSnackbar.closeSnackbar] is only needed if you use [MbSnackbarConfig.action] or [MbSnackbarConfig.complexMessage] and
 * have a custom close action where the snackbar should be closed immediately.
 */
val MbSnackbar = FcRefWithCoroutineScope<MbSnackbarProps<MbSnackbarRef>> { props, launch ->
  var config: MbSnackbarConfig? by useState { null }

  fun showSnackbar(text: String) {
    config = MbSnackbarConfig(message = text)
  }

  fun showSnackbar(newConfig: MbSnackbarConfig) {
    config = newConfig
  }

  fun closeSnackbar() {
    config = null
  }

  // Use the useImperativeHandle hook to expose the methods via the ref.
  useImperativeHandle(ref = props.ref, config) {
    object : MbSnackbarRef {
      override fun showSnackbarText(text: String) {
        showSnackbar(text)
      }

      override fun showSnackbar(newConfig: MbSnackbarConfig) {
        showSnackbar(newConfig)
      }

      override fun closeSnackbar() {
        closeSnackbar()
      }
    }
  }

  val currentConfig = config
  if (currentConfig != null) {
    Snackbar {
      anchorOrigin = unsafeJso {
        vertical = currentConfig.position.vertical
        horizontal = currentConfig.position.horizontal
      }
      sx {
        marginBottom = 20.px
      }
      open = true
      autoHideDuration =
        3000 + currentConfig.message.count() * 50 + (if (action == null) 0 else 1000) // By experimentation this value feels good.
      onClose = { _, _ ->
        config = null
      }
      SnackbarContent {
        sx {
          when (currentConfig.snackbarType) {
            MbSnackbarConfig.Type.SUCCESS -> backgroundColor = Color(greenColor[500] as String)
            MbSnackbarConfig.Type.ERROR -> backgroundColor = Color(redColor[500] as String)
            MbSnackbarConfig.Type.INFO -> backgroundColor = Color(blueColor[500] as String)
            MbSnackbarConfig.Type.WARNING -> {
              color = NamedColor.black
              backgroundColor = Color(yellowColor[500] as String)
            }

            null -> Unit
          }
        }
        message = Box.create {
          component = span
          sx {
            display = Display.flex
            alignItems = AlignItems.center
            whiteSpace = WhiteSpace.preLine
          }
          currentConfig.snackbarType?.icon?.let {
            it {
              sx {
                fontSize = 20.px
                opacity = number(0.9)
                marginRight = 8.px
              }
            }
          }
          +currentConfig.message
        }
        currentConfig.action?.let { action ->
          this.action = action
        }
        currentConfig.complexMessage?.let { message = it() }
      }
    }
  }
}
