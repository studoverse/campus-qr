package webcore

import com.studo.campusqr.common.utils.LocalizedString
import csstype.Display
import csstype.VerticalAlign
import csstype.px
import kotlinx.js.jso
import mui.icons.material.SvgIconComponent
import mui.material.*
import mui.system.sx
import react.ChildrenBuilder
import react.Props
import react.State
import react.react
import util.get

external interface MbDialogProps : Props {
  var config: MbMaterialDialogConfig
}

external interface MbDialogState : State

class MbMaterialDialogConfig(
  var show: Boolean = false,
  var title: String? = null,
  var titleIcon: SvgIconComponent? = null,
  var textContent: String? = null,
  var customContent: (ChildrenBuilder.() -> Unit)? = null,
  var buttons: List<DialogButton>? = null,
  var onClose: (() -> Unit)? = null,
  var disableAutoFocus: Boolean = false,
)

open class DialogButton(val text: String, val disabled: Boolean = false, val onClick: () -> Unit)

fun positiveButton(text: String = "OK", onClick: () -> Unit) = DialogButton(text = text, onClick = onClick)
fun negativeButton(text: String = LocalizedString("Cancel", "Abbrechen").get(), onClick: () -> Unit) =
  DialogButton(text = text, onClick = onClick)

private class MbMaterialDialog : RComponent<MbDialogProps, MbDialogState>() {

  override fun ChildrenBuilder.render() {
    Dialog {
      disableAutoFocus = props.config.disableAutoFocus
      disableEnforceFocus = props.config.disableAutoFocus
      fullWidth = true
      sx {
        margin = 12.px
      }
      open = props.config.show
      onClose = { event: dynamic, reason: String ->
        props.config.onClose?.invoke()
      }
      props.config.title?.let { title ->
        DialogTitle {
          props.config.titleIcon?.let { titleIcon ->
            Box {
              sx {
                display = Display.inlineFlex
                verticalAlign = VerticalAlign.middle
                marginRight = 8.px
              }
              titleIcon {}
            }
          }
          +title
        }
      }
      DialogContent {
        props.config.textContent?.let { content ->
          DialogContentText { +content }
        }
        Box {
          props.config.customContent?.invoke(this@Box)
        }
      }

      if (props.config.buttons?.isEmpty() == false) {
        DialogActions {
          props.config.buttons?.forEach { button ->
            Button {
              disabled = button.disabled
              +button.text
              onClick = {
                button.onClick()
              }
            }
          }
        }
      }
    }
  }
}

@Deprecated("Use buttons, insead of positiveButton and negativeButtion")
fun ChildrenBuilder.mbMaterialDialog(
  show: Boolean = false,
  title: String? = null,
  titleIcon: SvgIconComponent? = null,
  textContent: String? = null,
  customContent: (ChildrenBuilder.() -> Unit)? = null,
  positiveButton: DialogButton? = positiveButton {},
  negativeButton: DialogButton? = negativeButton {},
  onClose: (() -> Unit)? = null
) = mbMaterialDialog(handler = {
  config = MbMaterialDialogConfig(
    show,
    title,
    titleIcon,
    textContent,
    customContent,
    mutableListOf<DialogButton>().apply {
      positiveButton?.let { this.add(it) }
      negativeButton?.let { this.add(it) }
    },
    onClose,
  )
})

fun ChildrenBuilder.mbMaterialDialog(handler: MbDialogProps.() -> Unit) {
  MbMaterialDialog::class.react {
    +jso(handler)
  }
}
