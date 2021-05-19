package webcore

import com.studo.campusqr.common.utils.LocalizedString
import kotlinext.js.js
import kotlinx.html.DIV
import react.*
import react.dom.RDOMBuilder
import react.dom.div
import util.get
import webcore.materialUI.*

interface MbDialogProps : RProps {
  var classes: dynamic
  var config: MbMaterialDialogConfig
}

interface MbDialogState : RState

class MbMaterialDialogConfig(
  var show: Boolean,
  var title: String?,
  var titleIcon: RClass<IconProps>?,
  var textContent: String?,
  var customContent: (RDOMBuilder<DIV>.() -> Unit)?,
  var buttons: List<DialogButton>?,
  var onClose: (() -> Unit)?,
  var disableAutoFocus: Boolean
)

open class DialogButton(val text: String, val disabled: Boolean = false, val onClick: () -> Unit)

fun positiveButton(text: String = "OK", onClick: () -> Unit) = DialogButton(text = text, onClick = onClick)
fun negativeButton(text: String = LocalizedString("Cancel", "Abbrechen").get(), onClick: () -> Unit) =
  DialogButton(text = text, onClick = onClick)

class MbMaterialDialog : RComponent<MbDialogProps, MbDialogState>() {

  override fun RBuilder.render() {
    muiDialog {
      attrs.disableAutoFocus = props.config.disableAutoFocus
      attrs.disableEnforceFocus = props.config.disableAutoFocus
      attrs.fullWidth = true
      attrs.classes = js {
        this.paper = props.classes.dialogStyle
      }
      attrs.open = props.config.show
      attrs.onClose = {
        props.config.onClose?.invoke()
      }
      props.config.title?.let { title ->
        muiDialogTitle {
          props.config.titleIcon?.let { titleIcon ->
            div(props.classes.dialogIconStyle as String) {
              titleIcon {}
            }
          }
          +title
        }
      }
      muiDialogContent {
        props.config.textContent?.let { content ->
          muiDialogContentText { +content }
        }
        div {
          props.config.customContent?.invoke(this@div)
        }
      }

      if (props.config.buttons?.isNullOrEmpty() == false) {
        muiDialogActions {
          props.config.buttons?.forEach { button ->
            muiButton {
              attrs.disabled = button.disabled
              +button.text
              attrs.onClick = {
                button.onClick()
              }
            }
          }
        }
      }
    }
  }
}

private val style = { _: dynamic ->
  js {
    dialogStyle = js {
      margin = "12px"
    }
    dialogIconStyle = js {
      display = "inline-flex"
      verticalAlign = "middle"
      marginRight = 8
    }
  }
}

private val styledMbMaterialDialog = withStyles<MbDialogProps, MbMaterialDialog>(style)

fun RBuilder.mbMaterialDialog(
  show: Boolean = false,
  title: String? = null,
  titleIcon: RClass<IconProps>? = null,
  textContent: String? = null,
  customContent: (RDOMBuilder<DIV>.() -> Unit)? = null,
  buttons: List<DialogButton>? = null,
  onClose: (() -> Unit)? = null,
  disableAutoFocus: Boolean = false
) = styledMbMaterialDialog {
  attrs.config = MbMaterialDialogConfig(
    show,
    title,
    titleIcon,
    textContent,
    customContent,
    buttons,
    onClose,
    disableAutoFocus
  )
}

@Deprecated("Use buttons, insead of positiveButton and negativeButtion")
fun RBuilder.mbMaterialDialog(
  show: Boolean = false,
  title: String? = null,
  titleIcon: RClass<IconProps>? = null,
  textContent: String? = null,
  customContent: (RDOMBuilder<DIV>.() -> Unit)? = null,
  positiveButton: DialogButton? = positiveButton {},
  negativeButton: DialogButton? = negativeButton {},
  onClose: (() -> Unit)? = null
) = mbMaterialDialog(
  show,
  title,
  titleIcon,
  textContent,
  customContent,
  mutableListOf<DialogButton>().apply {
    positiveButton?.let { this.add(it) }
    negativeButton?.let { this.add(it) }
  },
  onClose
)