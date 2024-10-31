package views.common

import react.ChildrenBuilder
import react.Suspense
import util.Strings
import util.get
import views.common.genericErrorView.GenericErrorViewConfig
import views.common.genericErrorView.GenericErrorView

fun ChildrenBuilder.networkErrorView() {
  Suspense {
    GenericErrorView {
      config = GenericErrorViewConfig(
        title = Strings.network_error.get(),
        subtitle = Strings.network_error_description.get(),
      )
    }
  }
}