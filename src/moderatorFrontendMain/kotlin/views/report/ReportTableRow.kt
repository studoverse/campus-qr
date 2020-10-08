package views.report

import com.studo.campusqr.common.ReportData
import kotlinext.js.js
import react.*
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
  var filterOptions: Array<String>
  var filteredSeats: List<Int>
}

class ReportTableRow(props: ReportTableRowProps) : RComponent<ReportTableRowProps, ReportTableRowState>(props) {

  override fun ReportTableRowState.init(props: ReportTableRowProps) {
    showProgress = false
    showApplyFilterDialog = false
    filterOptions = props.config.userLocation.locationSeatCount?.let { seatCount ->
      (1..seatCount).map { it.toString() }.toTypedArray()
    } ?: emptyArray()
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

          muiAutocomplete {
            attrs.onChange = { _, target: Array<String>?, _ ->
              setState {
                filteredSeats = target?.map { it.toInt() } ?: emptyList()
              }
            }
            attrs.onInputChange = { event, value, _ ->
              // Add values after user pressed a " " or "," for fast input
              if (value.endsWith(" ") || value.endsWith(",")) {
                val seatNumber = value.trim().removeSuffix(",").toIntOrNull()
                if (seatNumber != null && seatNumber.toString() in state.filterOptions && seatNumber !in state.filteredSeats) {
                  setState {
                    filteredSeats += seatNumber
                  }
                }
              }
            }
            attrs.disableCloseOnSelect = true
            attrs.fullWidth = true
            attrs.multiple = true
            attrs.openOnFocus = true
            attrs.options = state.filterOptions
            attrs.value = state.filteredSeats.map { it.toString() }.toTypedArray()
            attrs.getOptionLabel = { it }
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
                attrs.disabled = params.disabled
                attrs.fullWidth = params.fullWidth
                attrs.variant = "outlined"
                attrs.label = Strings.report_checkin_seat_filter.get()
              }
            }
          }
        },
        buttons = listOf(
          DialogButton("Apply", onClick = {
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
      props.config.userLocation.seat?.let { locationSeatNumber ->
        mTableCell {
          +locationSeatNumber.toString()
        }
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
      mTableCell {
        +props.config.userLocation.impactedPeople.toString()
      }
    }
  }
}

interface ReportTableRowClasses {
  // Keep in sync with ReportItemStyle!
}

private val ReportTableRowStyle = { theme: dynamic ->
  // Keep in sync with ReportItemClasses!
  js {
  }
}

private val styled = withStyles<ReportTableRowProps, ReportTableRow>(ReportTableRowStyle)

fun RBuilder.renderReportTableRow(config: ReportTableRowProps.Config) = styled {
  attrs.config = config
}
  