package views.report

import app.AppContext
import app.GlobalCss.flex
import app.appContext
import com.studo.campusqr.common.payloads.ReportData
import csstype.Auto
import csstype.minus
import csstype.pct
import csstype.px
import mui.material.*
import mui.system.sx
import react.*
import util.Strings
import util.get
import views.common.spacer
import webcore.*

class ReportTableRowConfig(
  val userLocation: ReportData.UserLocation,
  val showEmailAddress: Boolean,
  val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
  val onDeleteFilter: (userLocation: ReportData.UserLocation) -> Unit
)

external interface ReportTableRowProps : Props {
  var config: ReportTableRowConfig
}

external interface ReportTableRowState : State

@Suppress("UPPER_BOUND_VIOLATED")
private class ReportTableRow(props: ReportTableRowProps) : RComponent<ReportTableRowProps, ReportTableRowState>(props) {

  // Inject AppContext, so that we can use it in the whole class, see https://reactjs.org/docs/context.html#classcontexttype
  companion object : RStatics<dynamic, dynamic, dynamic, dynamic>(ReportTableRow::class) {
    init {
      this.contextType = appContext
    }
  }

  private val appContext get() = this.asDynamic().context as AppContext

  override fun componentWillReceiveProps(nextProps: ReportTableRowProps) {
    setState { init(nextProps) }
  }

  private fun renderApplyFilterDialog() {
    appContext.showDialog(
      DialogConfig(
        title = Strings.report_checkin_add_filter_title.get(),
        customContent = DialogConfig.CustomContent(AddFilterDialog::class) {
          config = AddFilterDialogConfig(
            userLocation = props.config.userLocation,
            onApplyFilterChange = props.config.onApplyFilterChange,
          )
        },
      )
    )
  }

  override fun ChildrenBuilder.render() {
    TableRow {
      if (props.config.showEmailAddress) {
        TableCell {
          +props.config.userLocation.email
        }
      }
      TableCell {
        +props.config.userLocation.date
      }
      TableCell {
        +props.config.userLocation.locationName
      }
      TableCell {
        +(props.config.userLocation.seat?.toString() ?: "-")
      }
      TableCell {
        +props.config.userLocation.potentialContacts.toString()
      }
      TableCell {
        props.config.userLocation.locationSeatCount?.let {
          // Seat of infected person doesn't make sense to include in the filter
          val currentFilteredSeats = props.config.userLocation.filteredSeats
            ?.toList()
            ?.filter { it != props.config.userLocation.seat }
            ?: emptyList()
          if (currentFilteredSeats.isNotEmpty()) {
            Chip {
              color = ChipColor.primary
              variant = ChipVariant.outlined
              label = "${Strings.report_checkin_seat_filter.get()}: ${currentFilteredSeats.joinToString()}".toReactNode()
              onDelete = {
                with(props.config) {
                  onDeleteFilter(userLocation)
                }
              }
              onClick = {
                renderApplyFilterDialog()
              }
            }
          } else {
            Chip {
              variant = ChipVariant.outlined
              label = Strings.report_checkin_add_filter_title.get().toReactNode()
              onClick = {
                renderApplyFilterDialog()
              }
            }
          }
        }
      }
    }
  }
}

fun ChildrenBuilder.renderReportTableRow(config: ReportTableRowConfig) {
  ReportTableRow::class.react {
    this.config = config
  }
}
