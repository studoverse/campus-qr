package views.accessManagement.accessManagementOverview

import app.AppContext
import app.appContextToInject
import com.studo.campusqr.common.payloads.AccessManagementData
import com.studo.campusqr.common.payloads.ClientAccessManagement
import com.studo.campusqr.common.payloads.ClientLocation
import mui.material.*
import react.*
import util.*
import views.accessManagement.AccessManagementDetailsConfig
import views.accessManagement.AddLocation
import views.common.*
import webcore.*
import webcore.extensions.launch
import webcore.extensions.toRoute

external interface ListAccessManagementProps : Props {
  var locationId: String?
}

external interface ListAccessManagementState : State {
  var accessManagementList: List<ClientAccessManagement>?
  var clientLocation: ClientLocation?
  var showAccessManagementImportDialog: Boolean
  var loadingAccessManagementList: Boolean
}

private class ListAccessManagement : RComponent<ListAccessManagementProps, ListAccessManagementState>() {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(ListAccessManagement::class) {
    init {
      this.contextType = appContextToInject
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  private val dialogRef = createRef<MbDialog>()

  override fun ListAccessManagementState.init() {
    accessManagementList = null
    clientLocation = null
    showAccessManagementImportDialog = false
    loadingAccessManagementList = false
  }

  private fun fetchAccessManagementList() = launch {
    setState { loadingAccessManagementList = true }
    val params = props.locationId?.let { "?locationId=$it" } ?: ""
    val response = NetworkManager.get<AccessManagementData>("$apiBase/access/list$params")
    setState {
      accessManagementList = response?.accessManagement?.toList()
      clientLocation = response?.clientLocation
      loadingAccessManagementList = false
    }
  }

  override fun componentDidUpdate(
    prevProps: ListAccessManagementProps, prevState: ListAccessManagementState,
    snapshot: Any
  ) {
    if (prevProps.locationId != props.locationId) {
      setState {
        init()
      }
      fetchAccessManagementList()
    }
  }

  override fun componentDidMount() {
    fetchAccessManagementList()
  }

  private fun renderAddAccessManagementDialog() = dialogRef.current!!.showDialog(
    DialogConfig(
      title = DialogConfig.Title(text = Strings.access_control_create.get()),
      customContent = DialogConfig.CustomContent(AddLocation::class) {
        config = AccessManagementDetailsConfig.Create(
          locationId = props.locationId,
          dialogRef = dialogRef,
          onCreated = {
            fetchAccessManagementList()
          }
        )
      },
    )
  )

  override fun ChildrenBuilder.render() {
    mbDialog(ref = dialogRef)
    renderToolbarView(
      config = ToolbarViewConfig(
        title = StringBuilder().apply {
          append(Strings.access_control.get())
          append(" - ")
          if (state.clientLocation == null) {
            append(Strings.access_control_my.get())
          } else {
            append(state.clientLocation!!.name)
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
    )

    renderMbLinearProgress(show = state.loadingAccessManagementList)

    when {
      state.accessManagementList?.isNotEmpty() == true -> Table {
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
          state.accessManagementList!!.forEach { accessManagement ->
            renderAccessManagementRow(
              config = AccessManagementTableRowConfig(
                accessManagement = accessManagement,
                dialogRef = dialogRef,
                onOperationFinished = { operation, success ->
                  val snackbarText = if (success) {
                    fetchAccessManagementList()
                    when (operation) {
                      AccessManagementTableRowOperation.Edit -> Strings.access_control_edited_successfully.get()
                      AccessManagementTableRowOperation.Duplicate -> Strings.access_control_duplicated_successfully.get()
                      AccessManagementTableRowOperation.Delete -> Strings.access_control_deleted_successfully.get()
                    }
                  } else {
                    Strings.error_try_again.get()
                  }
                  appContext.showSnackbar(snackbarText)
                }
              )
            )
          }
        }
      }
      state.accessManagementList == null && !state.loadingAccessManagementList -> networkErrorView()
      !state.loadingAccessManagementList -> genericErrorView(
        title = Strings.access_control_not_configured_yet.get(),
        subtitle = Strings.access_control_not_configured_yet_subtitle.get()
      )
    }
  }
}

fun ChildrenBuilder.renderAccessManagementList(locationId: String?) {
  ListAccessManagement::class.react {
    this.locationId = locationId
  }
}
