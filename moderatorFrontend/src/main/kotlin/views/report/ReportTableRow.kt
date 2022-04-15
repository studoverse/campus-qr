package views.report

import com.studo.campusqr.common.payloads.ReportData
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

external interface ReportTableRowState : State {
  var showProgress: Boolean
  var showApplyFilterDialog: Boolean
  var filterOptions: List<Int>
  var filteredSeats: List<Int>
}

@Suppress("UPPER_BOUND_VIOLATED")
private class ReportTableRow(props: ReportTableRowProps) : RComponent<ReportTableRowProps, ReportTableRowState>(props) {

  override fun ReportTableRowState.init(props: ReportTableRowProps) {
    showProgress = false
    showApplyFilterDialog = false
    filterOptions = props.config.userLocation.locationSeatCount?.let { seatCount ->
      (1..seatCount).map { it }.filter { it != props.config.userLocation.seat }
    } ?: emptyList()
    filteredSeats = props.config.userLocation.filteredSeats?.toList() ?: emptyList()
  }

  override fun componentWillReceiveProps(nextProps: ReportTableRowProps) {
    setState { init(nextProps) }
  }

  private fun ChildrenBuilder.renderApplyFilterDialog() {
    if (state.showApplyFilterDialog) {
      mbMaterialDialog(
        config = MbMaterialDialogConfig(
          onClose = {
            setState {
              showApplyFilterDialog = false
            }
          },
          show = true,
          title = Strings.report_checkin_add_filter_title.get(),
          customContent = {
            Typography {
              +Strings.report_checkin_add_filter_content.get()
            }

            spacer(16)

            Box {
              sx {
                // Make sure that dialog's apply button doesn't get overlaid by autocomplete's dropdown
                width = 100.pct - 70.px
              }
              Autocomplete<AutocompleteProps<String>> {
                onChange = { _, target: Array<String>?, _, _ ->
                  setState {
                    filteredSeats = target?.map { it.toInt() } ?: emptyList()
                  }
                }
                onInputChange = { _, value, _ ->
                  // Add values after user pressed a " " or "," for fast input
                  if (value.endsWith(" ") || value.endsWith(",")) {
                    val seatNumber = value.trim().removeSuffix(",").toIntOrNull()
                    if (seatNumber != null && seatNumber in state.filterOptions && seatNumber !in state.filteredSeats) {
                      setState {
                        filteredSeats = filteredSeats + seatNumber
                      }
                    }
                  }
                }
                disableCloseOnSelect = true
                fullWidth = true
                multiple = true
                openOnFocus = true
                options = state.filterOptions.map { it.toString() }.toTypedArray()
                value = state.filteredSeats.map { it.toString() }.toTypedArray()
                getOptionLabel = { it }
                renderInput = { params ->
                  TextField.create {
                    +params
                    variant = FormControlVariant.outlined
                    label = Strings.report_checkin_seat_filter.get().toReactNode()
                  }
                }
              }
            }

          },
          buttons = listOf(
            DialogButton(Strings.apply.get(), onClick = {
              setState { showApplyFilterDialog = false }
              with(props.config) {
                onApplyFilterChange(userLocation, state.filteredSeats)
              }
            })
          )
        )
      )
    }
  }

  override fun ChildrenBuilder.render() {
    renderApplyFilterDialog()
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
                setState {
                  showApplyFilterDialog = true
                }
              }
            }
          } else {
            Chip {
              variant = ChipVariant.outlined
              label = Strings.report_checkin_add_filter_title.get().toReactNode()
              onClick = {
                setState {
                  showApplyFilterDialog = true
                }
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
