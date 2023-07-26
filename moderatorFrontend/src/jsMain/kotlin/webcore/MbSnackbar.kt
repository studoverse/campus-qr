import app.AppContext
import app.appContextToInject
import csstype.*
import js.core.jso
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

external interface MbSnackbarProps : PropsWithRef<MbSnackbar>

external interface MbSnackbarState : State {
  var config: MbSnackbarConfig?
}

/**
 * Snackbar component for handling all snackbars of the application.
 *
 * The snackbar always closes automatically after [SnackbarProps.autoHideDuration].
 * [MbSnackbar.closeSnackbar] is only needed if you use [MbSnackbarConfig.action] or [MbSnackbarConfig.complexMessage] and
 * have a custom close action where the snackbar should be closed immediately.
 */
class MbSnackbar : RComponent<MbSnackbarProps, MbSnackbarState>() {
  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(MbSnackbar::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val mbAppContext get() = this.asDynamic().context as AppContext

  override fun ChildrenBuilder.render() {
    val config = state.config ?: return

    Snackbar {
      anchorOrigin = jso {
        vertical = config.position.vertical
        horizontal = config.position.horizontal
      }
      sx {
        marginBottom = 20.px
      }
      open = true
      autoHideDuration = 3000 + config.message.count() * 50 + (if (action == null) 0 else 1000) // By experimentation this value feels good.
      onClose = { _, _ ->
        setState { this.config = null }
      }
      SnackbarContent {
        sx {
          when (config.snackbarType) {
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
          config.snackbarType?.icon?.let {
            it {
              sx {
                fontSize = 20.px
                opacity = number(0.9)
                marginRight = 8.px
              }
            }
          }
          +config.message
        }
        config.action?.let { action ->
          this.action = action
        }
        config.complexMessage?.let { message = it() }
      }
    }
  }

  fun showSnackbar(text: String) {
    setState { this.config = MbSnackbarConfig(message = text) }
  }

  fun showSnackbar(config: MbSnackbarConfig) {
    setState { this.config = config }
  }

  fun closeSnackbar() {
    setState { this.config = null }
  }
}

// You only need to define one snackbar in your page
// control the message, type, positions through state variables.
fun ChildrenBuilder.mbSnackbar(ref: Ref<MbSnackbar>? = null) {
  MbSnackbar::class.react {
    if (ref != null) {
      this.ref = ref
    }
  }
}
