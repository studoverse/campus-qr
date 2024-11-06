@file:JsModule("@mui/material/locale")
//@file:Suppress("NON_EXTERNAL_DECLARATION_IN_INAPPROPRIATE_FILE")

package webcore

external interface Localization {
  var components: (LocalizationComponents)?
}

external val amET: Localization

external val arEG: Localization

external val arSA: Localization

external val arSD: Localization

external val azAZ: Localization

external val bnBD: Localization

external val beBY: Localization

external val bgBG: Localization

external val caES: Localization

external val csCZ: Localization

external val daDK: Localization

external val deDE: Localization

external val elGR: Localization

external val enUS: Localization

external val esES: Localization

external val etEE: Localization

external val faIR: Localization

external val fiFI: Localization

external val frFR: Localization

external val heIL: Localization

external val hiIN: Localization

external val hrHR: Localization

external val huHU: Localization

external val hyAM: Localization

external val idID: Localization

external val isIS: Localization

external val itIT: Localization

external val jaJP: Localization

external val khKH: Localization

external val koKR: Localization

external val kuCKB: Localization

external val kuLatn: Localization

external val kkKZ: Localization

external val mkMK: Localization

external val myMY: Localization

external val msMS: Localization

external val neNP: Localization

external val nbNO: Localization

external val nnNO: Localization

external val nlNL: Localization

external val plPL: Localization

external val psAF: Localization

external val ptBR: Localization

external val ptPT: Localization

external val roRO: Localization

external val srRS: Localization

external val ruRU: Localization

external val siLK: Localization

external val skSK: Localization

external val svSE: Localization

external val thTH: Localization

external val trTR: Localization

external val tlTL: Localization

external val ukUA: Localization

external val urPK: Localization

external val viVN: Localization

external val zhCN: Localization

external val zhHK: Localization

external val zhTW: Localization

external interface Temp0 {
  var defaultProps: dynamic
}

external interface Temp1 {
  var defaultProps: dynamic
}

external interface Temp2 {
  var defaultProps: dynamic
}

external interface Temp4 {
  var defaultProps: dynamic
}

external interface Temp6 {
  var defaultProps: dynamic
}

external interface Temp8 {
  var defaultProps: dynamic
}

external interface LocalizationComponents {
  var MuiAlert: (Temp0)?
  var MuiBreadcrumbs: (Temp1)?
  var MuiTablePagination: (Temp2)?
  var MuiRating: (Temp4)?
  var MuiAutocomplete: (Temp6)?
  var MuiPagination: (Temp8)?
}

sealed external interface Temp3 {
  companion object {
    @seskar.js.JsValue("labelRowsPerPage")
    val labelRowsPerPage: Temp3

    @seskar.js.JsValue("labelDisplayedRows")
    val labelDisplayedRows: Temp3

    @seskar.js.JsValue("getItemAriaLabel")
    val getItemAriaLabel: Temp3
  }
}

sealed external interface Temp5 {
  companion object {
    @seskar.js.JsValue("emptyLabelText")
    val emptyLabelText: Temp5

    @seskar.js.JsValue("getLabelText")
    val getLabelText: Temp5
  }
}

sealed external interface Temp7 {
  companion object {
    @seskar.js.JsValue("clearText")
    val clearText: Temp7

    @seskar.js.JsValue("closeText")
    val closeText: Temp7

    @seskar.js.JsValue("loadingText")
    val loadingText: Temp7

    @seskar.js.JsValue("noOptionsText")
    val noOptionsText: Temp7

    @seskar.js.JsValue("openText")
    val openText: Temp7
  }
}

sealed external interface Temp9 {
  companion object {
    @seskar.js.JsValue("aria-label")
    val ariaLabel: Temp9

    @seskar.js.JsValue("getItemAriaLabel")
    val getItemAriaLabel: Temp9
  }
}