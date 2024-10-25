package views.accessManagement.accessManagementOverview

import app.appContextToInject
import mui.material.*
import react.*
import util.*
import views.accessManagement.accessManagementDetails.AccessManagementDetailsConfig
import views.accessManagement.accessManagementDetails.AccessManagementDetailsFc
import views.accessManagement.accessManagementOverview.accessManagementRow.AccessManagementTableRowConfig
import views.accessManagement.accessManagementOverview.accessManagementRow.AccessManagementTableRowFc
import views.accessManagement.accessManagementOverview.accessManagementRow.AccessManagementTableRowOperation
import views.common.*
import webcore.*
import webcore.extensions.toRoute
import js.lazy.Lazy

external interface ListAccessManagementProps : Props {
  var locationId: String?
}

@Lazy
val AccessManagementListFc = FcWithCoroutineScope<ListAccessManagementProps> { props, launch ->
  val accessManagementController = ListAccessManagementController.useListAccessManagementController(
    locationId = props.locationId,
    launch = launch,
  )
  val appContext = useContext(appContextToInject)!!
  val dialogRef = useRef<MbDialogRef>()

  fun renderAddAccessManagementDialog() = dialogRef.current!!.showDialog(
    dialogConfig = DialogConfig(
      title = DialogConfig.Title(text = Strings.access_control_create.get()),
      customContent = {
        Suspense {
          AccessManagementDetailsFc {
            config = AccessManagementDetailsConfig.Create(
              locationId = props.locationId,
              dialogRef = dialogRef,
              onCreated = {
                accessManagementController.fetchAccessManagementList()
              }
            )
          }
        }
      },
    )
  )

  MbDialogFc { ref = dialogRef }
  Suspense {
    ToolbarViewFc {
      config = ToolbarViewConfig(
        title = StringBuilder().apply {
          append(Strings.access_control.get())
          append(" - ")
          if (accessManagementController.clientLocation == null) {
            append(Strings.access_control_my.get())
          } else {
            append(accessManagementController.clientLocation.name)
          }
        }.toString(),
        buttons = listOf(
          ToolbarButton(
            text = Strings.access_control_export.get(),
            variant = ButtonVariant.outlined,
            onClick = { routeContext ->
              if (props.locationId == null) {
                routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LIST_EXPORT.toRoute()!!)
              } else {
                routeContext.pushRoute(Url.ACCESS_MANAGEMENT_LOCATION_LIST_EXPORT.toRoute(pathParams = mapOf("id" to props.locationId!!))!!)
              }
            }
          ),
          ToolbarButton(
            text = Strings.access_control_create.get(),
            variant = ButtonVariant.contained,
            onClick = {
              renderAddAccessManagementDialog()
            }
          )
        )
      )
    }
  }

  MbLinearProgressFc { show = accessManagementController.loadingAccessManagementList }

  when {
    accessManagementController.accessManagementList?.isNotEmpty() == true -> Table {
      TableHead {
        TableRow {
          TableCell { +Strings.location_name.get() }
          TableCell { +Strings.access_control_time_slots.get() }
          TableCell { +Strings.access_control_permitted_people.get() }
          TableCell { +Strings.access_control_note.get() }
          TableCell { +Strings.actions.get() }
        }
      }
      TableBody {
        Suspense {
          accessManagementController.accessManagementList.forEach { accessManagement ->
            AccessManagementTableRowFc {
              config = AccessManagementTableRowConfig(
                accessManagement = accessManagement,
                dialogRef = dialogRef,
                onOperationFinished = { operation, success ->
                  val snackbarText = if (success) {
                    accessManagementController.fetchAccessManagementList()
                    when (operation) {
                      AccessManagementTableRowOperation.Edit -> Strings.access_control_edited_successfully.get()
                      AccessManagementTableRowOperation.Duplicate -> Strings.access_control_duplicated_successfully.get()
                      AccessManagementTableRowOperation.Delete -> Strings.access_control_deleted_successfully.get()
                    }
                  } else {
                    Strings.error_try_again.get()
                  }
                  appContext.showSnackbarText(snackbarText)
                }
              )
            }
          }
        }
      }
    }

    accessManagementController.accessManagementList == null && !accessManagementController.loadingAccessManagementList -> networkErrorView()
    !accessManagementController.loadingAccessManagementList -> {
      Suspense {
        GenericErrorView.GenericErrorViewFc {
          config = GenericErrorView.GenericErrorViewConfig(
            title = Strings.access_control_not_configured_yet.get(),
            subtitle = Strings.access_control_not_configured_yet_subtitle.get(),
          )
        }
      }
    }
  }
}
