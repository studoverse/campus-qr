// TODO: package app

import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import webcore.materialUI.withStyles

interface QuestAccessManagementRowProps : RProps {
  var classes: QuestAccessManagementRowClasses
}

interface QuestAccessManagementRowState : RState

class QuestAccessManagementRow : RComponent<QuestAccessManagementRowProps, QuestAccessManagementRowState>() {
  override fun RBuilder.render() {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }
}

interface QuestAccessManagementRowClasses {
  // Keep in sync with QuestAccessManagementRowStyle!
}

private val QuestAccessManagementRowStyle = { theme: dynamic ->
  // Keep in sync with QuestAccessManagementRowClasses!
}

private val styled = withStyles<QuestAccessManagementRowProps, QuestAccessManagementRow>(QuestAccessManagementRowStyle)

fun RBuilder.renderQuestAccessManagementRow() = styled {
  // Set component attrs here
}
  