package views.common.ToolbarView

import app.RouteContext
import mui.material.ButtonVariant
import util.Url

class ToolbarButton(
  val text: String,
  val variant: ButtonVariant, // outlined|contained
  val onClick: (routeContext: RouteContext) -> Unit
)

class ToolbarViewConfig(
  val title: String,
  val backButtonUrl: Url? = null,
  val buttons: List<ToolbarButton> = emptyList()
)