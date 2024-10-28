package views.common

import react.ChildrenBuilder
import react.Suspense
import util.Strings
import util.get

fun ChildrenBuilder.networkErrorView() {
  Suspense {
    GenericErrorViewFc {
      config = GenericErrorViewConfig(
        title = Strings.network_error.get(),
        subtitle = Strings.network_error_description.get(),
      )
    }
  }
}