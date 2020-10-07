package views.report

import apiBase
import com.studo.campusqr.common.ReportData
import kotlinext.js.js
import kotlinx.browser.window
import react.*
import views.common.spacer
import webcore.DialogButton
import webcore.NetworkManager
import webcore.extensions.launch
import webcore.materialUI.*
import webcore.mbMaterialDialog
import kotlin.js.json

interface ReportTableRowProps : RProps {
  class Config(
    val userLocation: ReportData.UserLocation,
    val showEmailAddress: Boolean,
    val onFilterChanged: () -> Unit
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
    filteredSeats = emptyList()
  }

  private fun saveFilterAndCloseDialog() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<String>(
      "$apiBase/location/${props.config.userLocation.locationId}/editSeatFilter", params = json(
        "seat" to props.config.userLocation.seat,
        "filteredSeats" to state.filteredSeats
      )
    )
    setState {
      if (response == "ok") {
        showProgress = false
        showApplyFilterDialog = false
        props.config.onFilterChanged()
      } else {
        // TODO: Snackbar
        window.alert("Failed")
        console.log(response)
      }
    }
  }

  private fun deleteFilter() = launch {
    setState { showProgress = true }
    val response = NetworkManager.post<String>(
      "$apiBase/location/${props.config.userLocation.locationId}/deleteSeatFilter", params = json(
        "seat" to props.config.userLocation.seat
      )
    )
    setState {
      if (response == "ok") {
        showProgress = false
        showApplyFilterDialog = false
        filteredSeats = emptyList()
        props.config.onFilterChanged()
      } else {
        // TODO: Snackbar
        window.alert("Failed")
        console.log(response)
      }
    }
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
        title = "Add seat filter",
        customContent = {
          typography {
            +"Please select seats, that were close enough or in contact with the infected person."
          }

          spacer(16)

          muiAutocomplete {
            attrs.onChange = { _, target: Array<String>?, _ ->
              setState {
                filteredSeats = target?.map { it.toInt() } ?: emptyList()
              }
            }
            attrs.disableCloseOnSelect = true
            attrs.fullWidth = true
            attrs.multiple = true
            attrs.openOnFocus = true
            attrs.options = state.filterOptions
            attrs.value = state.filteredSeats.map { it.toString() }.toTypedArray()
            attrs.getOptionLabel = { it }
            attrs.renderInput = { params: dynamic ->
              textField {
                attrs.id = params.id
                attrs.InputProps = params.InputProps
                attrs.inputProps = params.inputProps
                attrs.disabled = params.disabled
                attrs.fullWidth = params.fullWidth
                attrs.variant = "outlined"
                attrs.label = "Seat filter"
              }
            }
          }
        },
        buttons = listOf(
          DialogButton("Save", onClick = {
            saveFilterAndCloseDialog()
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
          if (state.filteredSeats.isNotEmpty()) {
            muiTooltip {
              attrs.title = "Selected seats: ${state.filteredSeats.joinToString()}"
              mChip {
                attrs.color = "primary"
                attrs.variant = "outlined"
                attrs.label = "Filter active"
                attrs.onDelete = {
                  deleteFilter()
                }
                attrs.onClick = {
                  setState {
                    showApplyFilterDialog = true
                  }
                }
              }
            }
          } else {
            mChip {
              attrs.variant = "outlined"
              attrs.label = "Add seat filter"
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
  