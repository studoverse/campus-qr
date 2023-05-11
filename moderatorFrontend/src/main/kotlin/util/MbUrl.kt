package util

import com.studo.campusqr.common.utils.LocalizedString

interface MbUrl {
  val path: String
  val title: LocalizedString
  val requiresAuth: Boolean
  val showWithShell: Boolean
}
