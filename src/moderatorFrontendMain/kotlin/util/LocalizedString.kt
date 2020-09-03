package util

import com.studo.campusqr.common.utils.LocalizedString
import kotlinx.browser.document


object MbLocalizedStringConfig {

  enum class SupportedLanguage(val langCode: String) {
    De("de"),
    En("en");

    companion object {
      fun fromLangCode(lang: String): SupportedLanguage? = values().find { it.langCode == lang }
    }

  }

  private val defaultLanguage = SupportedLanguage.De

  private var cachedCookieLang: SupportedLanguage? = null

  private fun getLangFromCookie(): SupportedLanguage {
    return if (cachedCookieLang == null) {
      //language=JavaScript
      val langFromCookie =
          js("""document.cookie.replace(/(?:(?:^|.*;\s*)MbLang\s*\=\s*([^;]*).*$)|^.*$/, "$1")""") as String
      val newLang = if (langFromCookie.isBlank()) {
        defaultLanguage.langCode
      } else {
        langFromCookie
      }

      val parsed = SupportedLanguage.fromLangCode(newLang) ?: defaultLanguage
      cachedCookieLang = parsed
      parsed
    } else {
      cachedCookieLang!!
    }
  }

  private fun setCookieLang(supportedLanguage: SupportedLanguage) {
    if (cachedCookieLang == null || cachedCookieLang!! != supportedLanguage) {
      document.cookie = "MbLang=${supportedLanguage.langCode}; expires=Fri, 31 Dec 9999 23:59:59 GMT; path=/"
      cachedCookieLang = supportedLanguage
    }
  }

  var selectedLanguage: SupportedLanguage
    get() = getLangFromCookie()
    set(value) {
      setCookieLang(value)
    }
}

fun LocalizedString.get(language: MbLocalizedStringConfig.SupportedLanguage? = MbLocalizedStringConfig.selectedLanguage): String {
  return if (language == MbLocalizedStringConfig.SupportedLanguage.De) {
    de ?: en
  } else {
    en
  }
}