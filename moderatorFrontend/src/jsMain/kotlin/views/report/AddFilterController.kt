package views.report

import webcore.AutocompleteOnChange
import webcore.AutocompleteOnInputChange
import webcore.ButtonOnClick
import webcore.Launch

data class AddFilterController(
  val filterOptions: List<Int>,
  val filteredSeats: List<Int>,
  val autocompleteOnChange: AutocompleteOnChange<String>,
  val autocompleteOnInputChange: AutocompleteOnInputChange,
  val applyButtonOnClick: ButtonOnClick,
) {
  companion object {
    fun useAddFilterController(launch: Launch, config: AddFilterConfig): AddFilterController {
      var filterOptions: List<Int> = config.userLocation.locationSeatCount?.let { seatCount ->
        (1..seatCount).map { it }.filter { it != config.userLocation.seat }
      } ?: emptyList()
      var filteredSeats: List<Int> = config.userLocation.filteredSeats?.toList() ?: emptyList()

      val autocompleteOnChange: AutocompleteOnChange<String> = { _, value, _, _ ->
        @Suppress("UNCHECKED_CAST")
        value as Array<String>
        filteredSeats = value.map { it.toInt() }
      }

      val autocompleteOnInputChange: AutocompleteOnInputChange = { _, value, _ ->
        // Add values after user pressed a " " or "," for fast input
        if (value.endsWith(" ") || value.endsWith(",")) {
          val seatNumber = value.trim().removeSuffix(",").toIntOrNull()
          if (seatNumber != null && seatNumber in filterOptions && seatNumber !in filteredSeats) {
            filteredSeats = filteredSeats + seatNumber
          }
        }
      }

      val applyButtonOnClick: ButtonOnClick = {
        with(config) {
          onApplyFilterChange(userLocation, filteredSeats)
        }
        config.dialogRef.current!!.closeDialog()
      }

      return AddFilterController(
        filterOptions = filterOptions,
        filteredSeats = filteredSeats,
        autocompleteOnChange = autocompleteOnChange,
        autocompleteOnInputChange = autocompleteOnInputChange,
        applyButtonOnClick = applyButtonOnClick,
      )
    }
  }
}