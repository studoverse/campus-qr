package webcore

import app.AppContext
import app.appContext
import com.studo.campusqr.common.utils.LocalizedString
import csstype.*
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import react.*
import util.get
import kotlin.reflect.KClass

external interface MbDialogProps : Props

external interface MbDialogState : State {
  var config: DialogConfig?
}

open class DialogConfig(
  val icon: SvgIconComponent? = null,
  val title: String? = null,
  val text: String? = null,
  val customContent: CustomContent<*>? = null,
  val buttons: List<DialogButton>? = null, // Buttons can also be defined in customContent for more complex behaviour or styling
  val contentMargin: Boolean = true,
  val onClose: (() -> Unit)? = null,
  val scroll: DialogScroll? = DialogScroll.paper,
  val overflow: OverflowForDialog? = null,
  val customDialogConfig: (DialogProps.() -> Unit)? = null,
  val isCancelable: Boolean = true,
) {
  init {
    require(title != null || text != null || customContent != null) { "DialogConfig: At least title or text or customContent must be set" }
    if (icon != null) require(title != null) { "DialogConfig: Title must be set when icon is set" }
  }

  /**
   *  For custom dialog content define an own component and pass it to [customContent]
   *  Reason: State updates only work when the code is exposed to a render function so that a state update can change the dialog
   *  Since dialogs are normally triggered by onClick callbacks the customContent will only be updated if it is wrapped in a separate
   *  component that rerenders after state updates
   */
  class CustomContent<P : Props>(private val component: KClass<out Component<P, *>>, private val setProps: (P.() -> Unit)? = null) {
    fun ChildrenBuilder.renderCustomContent() {
      component.react {
        setProps?.let { this.it() }
      }
    }
  }
}

open class DialogButton(
  val text: String,
  val disabled: Boolean = false,
  val color: ButtonColor? = null, // Will choose neutral (black) color if null (see createTheme)
  val onClick: (() -> Unit)? = null
)

fun positiveButton(
  text: String = "OK",
  disabled: Boolean = false,
  onClick: (() -> Unit)? = null
) = DialogButton(text = text, disabled = disabled, onClick = onClick)

fun negativeButton(
  text: String = LocalizedString("Cancel", "Abbrechen").get(),
  disabled: Boolean = false,
  onClick: (() -> Unit)? = null
) = DialogButton(text = text, disabled = disabled, onClick = onClick)

class MbMaterialDialog : RComponent<MbDialogProps, MbDialogState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(MbMaterialDialog::class) {
    init {
      this.contextType = appContext
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private fun ChildrenBuilder.renderContent(config: DialogConfig) {
    config.text?.let { content ->
      content.split("\n").map { DialogContentText { +it } } // Split into paragraphs
    }
    config.customContent?.let { customContent ->
      Box {
        with(customContent) {
          renderCustomContent()
        }
      }
    }
  }

  override fun ChildrenBuilder.render() {
    val config = state.config ?: return

    Dialog {
      config.scroll?.let { scroll = it }
      fullWidth = true
      sx {
        margin = 12.px
        MuiDialog.paper {
          config.overflow?.let { overflow ->
            overflowY = overflow.overflowY
            overflowX = overflow.overflowX
          }
        }
      }
      open = true
      onClose = { _, _ ->
        if (config.isCancelable) {
          setState {
            this.config = null
          }
        }
        config.onClose?.invoke()
      }
      config.title?.let { title ->
        DialogTitle {
          config.icon?.let { titleIcon ->
            Box {
              sx {
                display = Display.inlineFlex
                verticalAlign = VerticalAlign.middle
                marginRight = 8.px
              }
              titleIcon()
            }
          }
          +title
        }
      }

      if (config.contentMargin) {
        DialogContent {
          renderContent(config)
        }
      } else {
        renderContent(config)
      }

      if (!config.buttons.isNullOrEmpty()) {
        DialogActions {
          config.buttons.forEach { button ->
            Button {
              button.color?.let { color ->
                this.color = color
              }
              disabled = button.disabled
              +button.text
              onClick = {
                button.onClick?.invoke()
                // Close dialog on click
                setState {
                  this.config = null
                }
              }
            }
          }
        }
      }
      config.customDialogConfig?.invoke(this)
    }
  }

  fun showDialog(dialogConfig: DialogConfig) {
    setState { this.config = dialogConfig }
  }

  fun closeDialog() {
    setState { this.config = null }
  }
}

fun ChildrenBuilder.mbMaterialDialog(ref: Ref<MbMaterialDialog>? = null) {
  MbMaterialDialog::class.react {
    if (ref != null) {
      this.ref = ref
    }
  }
}
