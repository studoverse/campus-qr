package webcore

import com.studo.campusqr.common.utils.LocalizedString
import mui.icons.material.Delete
import mui.material.ButtonColor
import mui.material.ButtonVariant
import react.*
import util.get

external interface MbDialogState : State {
  var configs: MutableList<DialogConfig>
}

/**
 * Dialog component for handling all dialogs of a page.
 * Example of a page: News posts management page for an organization in Connect.
 *
 * Only create dialogs in the base component of a page. This enforces having exactly one dialog per page.
 * Example of a base component: NewsPostManagementView.
 * Example of a child component: NewsPostView
 *
 * Usage:
 * 1. Define a ref in the base component of the page where you want to display the dialog: private val dialogRef = createRef<MbDialog>()
 * 2. Call `mbDialog(ref = dialogRef)` in the render function of the base component.
 * 3. Pass the dialog ref as a prop to child components that need the dialog.
 * 4. Call ref.showDialog/closeDialog outside the render cycle (e.g. on a click listener) to manage this [MbDialog] component.
 *    Note that DialogConfig.buttons close the dialog automatically onClick.
 *    If you use [DialogConfig.customContent] and have a custom close action, call ref.closeDialog.
 *
 * How does it work?
 * To support multiple consecutive dialogs that belong together, a new [MbSingleDialog] is created for every `showDialog()`.
 * Only the most recent dialog is shown while the others are not displayed (but still in the DOM to preserve their state).
 * This allows to have a base dialog and corresponding info/error dialogs "on top".
 * UX: Ideally, only 1 dialog is present at the users context. Stacking multiple dialogs on top of each other creates easily confusing experiences.
 */
class MbDialog : RComponent<PropsWithRef<MbDialog>, MbDialogState>() {
  override fun MbDialogState.init() {
    configs = mutableListOf()
  }

  override fun ChildrenBuilder.render() {
    state.configs.forEach { config ->
      @Suppress("DEPRECATION") // Only here inside the forEach we want to render each single dialog individually
      mbSingleDialog(
        config = config,
        hidden = config != state.configs.last(), // Hide all dialogs but the most recent one, so they don't unmount and can be shown again
      )
    }
  }

  fun showDialog(dialogConfig: DialogConfig) {
    setState {
      this.configs.add(
        dialogConfig.copy(
          onClose = {
            dialogConfig.onClose?.invoke()
            setState {
              this.configs.removeLastOrNull() ?: console.error("closeDialog was called although no dialog is open.")
            }
          },
        ),
      )
    }
  }

  // Closes the latest dialog
  fun closeDialog() {
    // onClose itself cannot be null here since it is always set in showDialog().
    state.configs.lastOrNull()?.onClose?.invoke() ?: console.error("closeDialog was called although no dialog is open.")
  }

  fun showDeleteDialog(
    title: String?,
    text: String?,
    deleteButtonText: String,
    onDelete: () -> Unit,
  ) {
    showDialog(
      DialogConfig(
        title = title?.let { dialogTitle ->
          DialogConfig.Title(
            text = dialogTitle,
            icon = Delete,
          )
        },
        text = text,
        buttons = listOf(
          DialogButton(text = LocalizedString(en = "Cancel", de = "Abbrechen").get()),
          DialogButton(
            text = deleteButtonText,
            onClick = onDelete,
            variant = ButtonVariant.contained,
            color = ButtonColor.primary,
          ),
        ),
      )
    )
  }
}

fun ChildrenBuilder.mbDialog(ref: Ref<MbDialog>) {
  MbDialog::class.react {
    this.ref = ref
  }
}
