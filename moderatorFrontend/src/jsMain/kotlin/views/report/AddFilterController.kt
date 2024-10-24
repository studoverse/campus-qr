package views.report

import mui.base.AutocompleteChangeDetails
import mui.base.AutocompleteChangeReason
import mui.base.AutocompleteInputChangeReason
import react.dom.events.SyntheticEvent
import webcore.AutocompleteOnChange
import webcore.Launch

data class AddFilterController(
  val filterOptions: List<Int>,
  val filteredSeats: List<Int>,
  val autocompleteOnChange: AutocompleteOnChange<String>,
  val autocompleteOnInputChange: (event: SyntheticEvent<*, *>, value: String, reason: AutocompleteInputChangeReason) -> Unit,
) {
  companion object {
    fun useAddFilterController(launch: Launch, props: AddFilterProps): AddFilterController {
      var filterOptions: List<Int> = props.config.userLocation.locationSeatCount?.let { seatCount ->
        (1..seatCount).map { it }.filter { it != props.config.userLocation.seat }
      } ?: emptyList()
      var filteredSeats: List<Int> = props.config.userLocation.filteredSeats?.toList() ?: emptyList()

      fun autocompleteOnChange(
        event: SyntheticEvent<*, *>,
        value: Any?,
        reason: AutocompleteChangeReason,
        details: AutocompleteChangeDetails<String>?,
      ) {
        @Suppress("UNCHECKED_CAST")
        value as Array<String>
        filteredSeats = value.map { it.toInt() }
      }

      fun autocompleteOnInputChange(
        event: SyntheticEvent<*, *>,
        value: String,
        reason: AutocompleteInputChangeReason,
      ) {
        // Add values after user pressed a " " or "," for fast input
        if (value.endsWith(" ") || value.endsWith(",")) {
          val seatNumber = value.trim().removeSuffix(",").toIntOrNull()
          if (seatNumber != null && seatNumber in filterOptions && seatNumber !in filteredSeats) {
            filteredSeats = filteredSeats + seatNumber
          }
        }
      }

      return AddFilterController(
        filterOptions = filterOptions,
        filteredSeats = filteredSeats,
        autocompleteOnChange = ::autocompleteOnChange,
        autocompleteOnInputChange = ::autocompleteOnInputChange,
      )
    }
  }
}