package webcore

import web.cssom.*
import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.utils.LocalizedString
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.Breakpoint
import mui.system.PropsWithSx
import mui.system.sx
import react.*
import util.get
import kotlin.reflect.KClass

external interface MbSingleDialogProps : PropsWithRef<MbSingleDialog> {
  var config: DialogConfig?
  var hidden: Boolean
}

external interface MbSingleDialogState : State {
  var open: Boolean // If true, the dialog is shown (Dialog is not displayed but still mounted)
}

/**
 * The MUI Dialog has a quite confusing design to define the width, so this class bundles the related properties.
 * This is just a wrapper and not another abstraction on top to avoid having two patterns.
 */
data class WidthConfig(
  /** If true, the dialog stretches to maxWidth */
  val fullWidth: Boolean = true,

  /**
   * Determine the max-width of the dialog.
   * The dialog width grows with the size of the screen.
   * null disables maxWidth to enable a real fullWidth dialog.
   */
  val maxWidth: Breakpoint? = Breakpoint.sm,
)

data class DialogConfig(
  val title: Title? = null,
  val text: String? = null,
  val customContent: CustomContent<*>? = null, // For a custom close action in customContent call ref.closeDialog
  // Buttons can also be defined in customContent for more complex behaviour or styling
  // DialogConfig.buttons close the dialog automatically onClick.
  val buttons: List<DialogButton>? = null,
  val contentMargin: Boolean = true,
  val onClose: (() -> Unit)? = null,
  val scroll: DialogScroll? = DialogScroll.paper,
  val overflow: OverflowForDialog? = null,
  val customDialogConfig: (DialogProps.() -> Unit)? = null,
  val isCancelable: Boolean = true,
  val widthConfig: WidthConfig = WidthConfig(),
) {
  init {
    require(title != null || text != null || customContent != null) { "DialogConfig: At least title or text or customContent must be set" }
  }


  data class Title(
    val text: String,
    val icon: SvgIconComponent? = null, // No icon if null
  )

  /**
   *  For custom dialog content define an own component and pass it to [customContent]
   *  Reason: State updates only work when the code is exposed to a render function so that a state update can change the dialog
   *  Since dialogs are normally triggered by onClick callbacks the customContent will only be updated if it is wrapped in a separate
   *  component that rerenders after state updates.
   *  For a custom close action in [customContent], call ref.closeDialog.
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
  val variant: ButtonVariant? = null, // Default variant if null
  val onClick: (() -> Unit)? = null
)

fun positiveButton(
  text: String = "OK",
  disabled: Boolean = false,
  onClick: (() -> Unit)? = null
) = DialogButton(text = text, disabled = disabled, onClick = onClick, variant = ButtonVariant.contained, color = ButtonColor.primary)

fun negativeButton(
  text: String = LocalizedString("Cancel", "Abbrechen").get(),
  disabled: Boolean = false,
  onClick: (() -> Unit)? = null
) = DialogButton(text = text, disabled = disabled, onClick = onClick)

fun okayButton(
  text: String = LocalizedString(en = "Okay", de = "Okay").get(),
  disabled: Boolean = false,
  onClick: (() -> Unit)? = null
) = DialogButton(text = text, disabled = disabled, onClick = onClick)

class MbSingleDialog(props: MbSingleDialogProps) : RComponent<MbSingleDialogProps, MbSingleDialogState>(props) {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(MbSingleDialog::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val mbAppContext get() = this.asDynamic().context as AppContext

  override fun MbSingleDialogState.init(props: MbSingleDialogProps) {
    open = props.config != null
  }

  private fun ChildrenBuilder.splitText(content: String) {
    content.split("\n").map {
      DialogContentText { +it }
    } // Split into paragraphs
  }

  private fun ChildrenBuilder.renderContent(config: DialogConfig) {
    config.text?.let { content ->
      splitText(content)
    }
    config.customContent?.let { customContent ->
      with(customContent) {
        renderCustomContent()
      }
    }
  }

  override fun ChildrenBuilder.render() {
    val config = props.config ?: return

    Dialog {
      config.scroll?.let { scroll = it }
      fullWidth = config.widthConfig.fullWidth
      maxWidth = config.widthConfig.maxWidth
      sx {
        if (props.hidden) display = None.none
        margin = 12.px
        MuiDialog.paper {
          padding = 8.px
          config.overflow?.let { overflow ->
            overflowY = overflow.overflowY
            overflowX = overflow.overflowX
          }
        }
      }
      open = state.open
      onClose = { _, _ ->
        if (config.isCancelable) {
          config.onClose?.invoke()
          setState {
            open = false
          }
        }
      }
      config.title?.let { (title, icon) ->
        DialogTitle {
          // TODO: @mh DialogTitleProps does not extend PropsWithSx. See: https://github.com/JetBrains/kotlin-wrappers/issues/2004
          @Suppress("UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
          this as PropsWithSx
          sx {
            display = Display.flex
            alignItems = AlignItems.center
            whiteSpace = WhiteSpace.preLine // Handle \n
          }
          icon?.let { titleIcon ->
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
              button.variant?.let { variant = it }
              +button.text
              onClick = {
                // Close dialog on click
                config.onClose?.invoke()
                // onClick can possibly open another dialog, so it must be called after onClose removed this dialog.
                button.onClick?.invoke()
                setState {
                  open = false
                }
              }
            }
          }
        }
      }
      config.customDialogConfig?.invoke(this)
    }
  }

  /**
   * Only used to close the dialog in [DialogConfig.customContent]
   */
  fun closeDialog() {
    when (val config = props.config) {
      null -> console.error("closeDialog was called although no dialog is open.")
      else -> config.onClose?.invoke()
    }
    setState {
      open = false
    }
  }
}

@Deprecated("Use mbDialog to handle multiple dialogs on top", ReplaceWith("mbDialog"))
fun ChildrenBuilder.mbSingleDialog(config: DialogConfig? = null, ref: Ref<MbSingleDialog>? = null, hidden: Boolean = false) {
  MbSingleDialog::class.react {
    this.config = config
    this.hidden = hidden
    if (ref != null) {
      this.ref = ref
    }
  }
}
