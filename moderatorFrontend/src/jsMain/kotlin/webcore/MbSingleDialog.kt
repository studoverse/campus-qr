package webcore

import web.cssom.*
import com.studo.campusqr.common.utils.LocalizedString
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.Breakpoint
import mui.system.sx
import react.*
import react.invoke
import util.get

external interface MbSingleDialogProps<T : MbSingleDialogRef> : PropsWithRef<T> {
  var config: DialogConfig?
  var hidden: Boolean
}

external interface MbSingleDialogRef {
  fun closeDialog()
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
  /**
   *  For stateful dialog content define an own component and pass it to [customContent].
   *  Reason: State updates only work when the code is exposed to a render function so that a state update can change the dialog.
   *  Since dialogs are normally triggered by onClick callbacks the customContent will only be updated if it is wrapped in a separate
   *  component that rerenders after state updates.
   *  For a custom close action in [customContent], call ref.closeDialog.
   */
  val customContent: (ChildrenBuilder.() -> Unit)? = null, // For a custom close action in customContent call ref.closeDialog
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

@Deprecated("Use MbDialogFc to handle multiple dialogs on top", ReplaceWith("MbDialogFc"))
val MbSingleDialogFc = FcRefWithCoroutineScope<MbSingleDialogProps<MbSingleDialogRef>> { props, launch ->
  var open: Boolean by useState(props.config != null) // If true, the dialog is shown (Dialog is not displayed but still mounted)

  fun ChildrenBuilder.splitText(content: String) {
    content.split("\n").map {
      DialogContentText { +it }
    } // Split into paragraphs
  }

  fun ChildrenBuilder.renderContent(config: DialogConfig) {
    config.text?.let { content ->
      splitText(content)
    }
    config.customContent?.let { customContent ->
      customContent()
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
    open = false
  }

  // Use the useImperativeHandle hook to expose the methods via the ref.
  useImperativeHandle(ref = props.ref, dependencies = *arrayOf(props.config)) { // TODO: @mh Should this be an array or just the values?
    object : MbSingleDialogRef {
      override fun closeDialog() {
        closeDialog()
      }
    }
  }

  val config = props.config
  if (config != null) {
    Dialog {
      config.scroll?.let { scroll = it }
      fullWidth = config.widthConfig.fullWidth
      maxWidth = config.widthConfig.maxWidth
      sx {
        if (props.hidden) display = None.none
        margin = 12.px
        dialogClasses.paper {
          padding = 8.px
          config.overflow?.let { overflow ->
            overflowY = overflow.overflowY
            overflowX = overflow.overflowX
          }
        }
      }
      this.open = open
      onClose = { _, _ ->
        if (config.isCancelable) {
          config.onClose?.invoke()
          open = false
        }
      }
      config.title?.let { (title, icon) ->
        DialogTitle {
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
                open = false
              }
            }
          }
        }
      }
      config.customDialogConfig?.invoke(this)
    }
  }
}
