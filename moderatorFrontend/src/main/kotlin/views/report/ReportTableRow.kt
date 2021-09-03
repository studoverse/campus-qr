package views.report

import com.studo.campusqr.common.payloads.ReportData
import kotlinext.js.js
import react.*
import react.dom.div
import util.Strings
import util.get
import views.common.spacer
import webcore.DialogButton
import webcore.materialUI.*
import webcore.mbMaterialDialog

interface ReportTableRowProps : RProps {
  class Config(
    val userLocation: ReportData.UserLocation,
    val showEmailAddress: Boolean,
    val onApplyFilterChange: (userLocation: ReportData.UserLocation, filteredSeats: List<Int>) -> Unit,
    val onDeleteFilter: (userLocation: ReportData.UserLocation) -> Unit
  )

  var config: Config
  var classes: ReportTableRowClasses
}

interface ReportTableRowState : RState {
  var showProgress: Boolean
  var showApplyFilterDialog: Boolean
  var filterOptions: List<Int>
  var filteredSeats: List<Int>
}

class ReportTableRow(props: ReportTableRowProps) : RComponent<ReportTableRowProps, ReportTableRowState>(props) {

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

  private fun RBuilder.renderApplyFilterDialog() {
    if (state.showApplyFilterDialog) {
      mbMaterialDialog(
        onClose = {
          setState {
            showApplyFilterDialog = false
          }
        },
        show = true,
        title = Strings.report_checkin_add_filter_title.get(),
        customContent = {
          typography {
            +Strings.report_checkin_add_filter_content.get()
          }

          spacer(16)

          div(props.classes.autocompleteWrapper) {
            muiAutocomplete {
              attrs.onChange = { _, target: Array<String>?, _ ->
                setState {
                  filteredSeats = target?.map { it.toInt() } ?: emptyList()
                }
              }
              attrs.onInputChange = { _, value, _ ->
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
              attrs.disableCloseOnSelect = true
              attrs.fullWidth = true
              attrs.multiple = true
              attrs.openOnFocus = true
              attrs.options = state.filterOptions.map { it.toString() }.toTypedArray()
              attrs.value = state.filteredSeats.map { it.toString() }.toTypedArray()
              attrs.getOptionLabel = { it as String }
              attrs.renderOption = { option, state ->
                Fragment {
                  mCheckbox {
                    attrs.color = "primary"
                    attrs.checked = state.selected as Boolean
                  }
                  +(option as String)
                }
              }
              attrs.renderInput = { params: dynamic ->
                textField {
                  attrs.id = params.id
                  attrs.InputProps = params.InputProps
                  attrs.inputProps = params.inputProps
                  attrs.disabled = params.disabled as Boolean
                  attrs.fullWidth = params.fullWidth as Boolean
                  attrs.variant = "outlined"
                  attrs.label = Strings.report_checkin_seat_filter.get()
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
    }
  }

  override fun RBuilder.render() {
    renderApplyFilterDialog()
    mTableRow {
      if (props.config.showEmailAddress) {
        mTableCell {
          +props.config.userLocation.email
        }
      }
      mTableCell {
        +props.config.userLocation.date
      }
      mTableCell {
        +props.config.userLocation.locationName
      }
      mTableCell {
        +(props.config.userLocation.seat?.toString() ?: "-")
      }
      mTableCell {
        +props.config.userLocation.potentialContacts.toString()
      }
      mTableCell {
        props.config.userLocation.locationSeatCount?.let {
          val currentFilteredSeats = props.config.userLocation.filteredSeats?.toList() ?: emptyList()
          if (currentFilteredSeats.isNotEmpty()) {
            mChip {
              attrs.color = "primary"
              attrs.variant = "outlined"
              attrs.label = "${Strings.report_checkin_seat_filter.get()}: ${currentFilteredSeats.joinToString()}"
              attrs.onDelete = {
                with(props.config) {
                  onDeleteFilter(userLocation)
                }
              }
              attrs.onClick = {
                setState {
                  showApplyFilterDialog = true
                }
              }
            }
          } else {
            mChip {
              attrs.variant = "outlined"
              attrs.label = Strings.report_checkin_add_filter_title.get()
              attrs.onClick = {
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

interface ReportTableRowClasses {
  var autocompleteWrapper: String
}

private val style = { _: dynamic ->
  js {
    autocompleteWrapper = js {
      // Make sure that dialog's apply button doesn't get overlaid by autocomplete's dropdown
      width = "calc(100% - 70px)"
    }
  }
}

private val styled = withStyles<ReportTableRowProps, ReportTableRow>(style)

fun RBuilder.renderReportTableRow(config: ReportTableRowProps.Config) = styled {
  attrs.config = config
}
  