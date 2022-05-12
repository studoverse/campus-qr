package webcore

import app.AppContext
import app.appContextToInject
import csstype.*
import kotlinx.js.jso
import mui.icons.material.*
import mui.material.*
import mui.system.sx
import react.*
import react.dom.html.ReactHTML

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

external interface MbSnackbarProps : Props

external interface MbSnackbarState : State {
  var config: MbSnackbarConfig?
}

class MbSnackbar : RComponent<MbSnackbarProps, MbSnackbarState>() {
  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(MbSnackbar::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

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
      autoHideDuration = 3000
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
          component = ReactHTML.span
          sx {
            display = Display.flex
            alignItems = AlignItems.center
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