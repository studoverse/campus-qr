package views.report.reportTableRow

import js.lazy.Lazy
import mui.material.*
import react.*
import util.Strings
import util.get
import views.report.addFilter.AddFilter
import views.report.addFilter.AddFilterConfig
import webcore.*

external interface ReportTableRowProps : Props {
  var config: ReportTableRowConfig
}

@Lazy
val ReportTableRow = FcWithCoroutineScope<ReportTableRowProps> { props, launch ->
  fun renderApplyFilterDialog() {
    props.config.dialogRef.current!!.showDialog(
      DialogConfig(
        title = DialogConfig.Title(text = Strings.report_checkin_add_filter_title.get()),
        customContent = {
          Suspense {
            AddFilter {
              config = AddFilterConfig(
                userLocation = props.config.userLocation,
                dialogRef = props.config.dialogRef,
                onApplyFilterChange = props.config.onApplyFilterChange,
              )
            }
          }
        },
      )
    )
  }

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