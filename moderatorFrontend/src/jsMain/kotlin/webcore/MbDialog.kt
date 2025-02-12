@file:OptIn(InternalOnClose::class)

package webcore

import com.studo.campusqr.common.utils.LocalizedString
import mui.icons.material.Delete
import mui.material.ButtonColor
import mui.material.ButtonVariant
import react.*
import util.get

external interface MbDialogRef {
  fun showDialog(dialogConfig: DialogConfig)
  fun closeDialog(): OnCloseFinished?
  fun showDeleteDialog(
    title: String?,
    text: String?,
    deleteButtonText: String,
    onDelete: () -> Unit,
  )
}

external interface MbDialogProps<T : MbDialogRef> : PropsWithRef<T>

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
val MbDialog = FcRefWithCoroutineScope<MbDialogProps<MbDialogRef>> { props, launch ->
  var (configs, setConfigs) = useState(mutableListOf<DialogConfig>())
  // Opening a new dialog requires that the current dialog is closed before, otherwise the new dialog is closed instantly.
  // To achieve that, the onClose promise (for DialogButton::onClick in MbSingleDialog) is fulfilled after the state update of "configs",
  // which can then trigger the button onClick. Also, the user defined onClose function is executed after the "configs" state update.
  var onCloseUpdateDoneRef = useRef<(Unit) -> Unit>()

  fun showDialog(dialogConfig: DialogConfig) {
    setConfigs { oldConfigs ->
      dialogConfig.onCloseInternal = {
        val promise = OnCloseFinished(executor = { fulfilled, rejected ->
          onCloseUpdateDoneRef.current = {
            dialogConfig.onClose?.invoke() // Invoke user defined onClose function
            fulfilled(Unit)
          }
        })
        // Due to function closures, we need to use the setter function to get the current state.
        setConfigs { previousConfigs ->
          if (previousConfigs.isNotEmpty()) {
            (previousConfigs - previousConfigs.last()).toMutableList()
          } else {
            console.error("closeDialog was called although no dialog is open.")
            previousConfigs
          }
        }
        promise
      }
      (oldConfigs + dialogConfig).toMutableList()
    }
  }

  // Closes the latest dialog
  fun closeDialog(): OnCloseFinished? {
    // onClose itself cannot be null here since it is always set in showDialog().
    when (val promise = configs.lastOrNull()?.onCloseInternal?.invoke()) {
      null -> {
        console.error("closeDialog was called although no dialog is open.")
        return null
      }

      else -> {
        return promise
      }
    }
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

  // Use the useImperativeHandle hook to expose the methods via the ref.
  useImperativeHandle(ref = props.ref, configs) {
    object : MbDialogRef {
      override fun showDialog(dialogConfig: DialogConfig) {
        showDialog(dialogConfig)
      }

      override fun closeDialog(): OnCloseFinished? {
        return closeDialog()
      }

      override fun showDeleteDialog(
        title: String?,
        text: String?,
        deleteButtonText: String,
        onDelete: () -> Unit,
      ) {
        showDeleteDialog(
          title = title,
          text = text,
          deleteButtonText = deleteButtonText,
          onDelete = onDelete,
        )
      }
    }
  }

  useEffect(configs) {
    if (onCloseUpdateDoneRef.current != null) {
      onCloseUpdateDoneRef.current!!.invoke(Unit)
      onCloseUpdateDoneRef.current = null // Reset
    }
  }

  configs.forEach { config ->
    @Suppress("DEPRECATION") // Only here inside the forEach we want to render each single dialog individually
    MbSingleDialog {
      this.config = config.copy( // Copy so that onClose of the original config stays the same.
        // onClose is already called in the useEffect above, so we don't want to call it inside the MbSingleDialog another time.
        onClose = null
      )
      hidden = config != configs.last() // Hide all dialogs but the most recent one, so they don't unmount and can be shown again
    }
  }
}
