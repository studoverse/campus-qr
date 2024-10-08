package webcore

import react.ChildrenBuilder
import react.Props
import react.State

/**
 * Convenience function for defining customContent in DialogConfig
 * Can only be used for trivial cases where no extra props, state handling or callbacks are required other than a basic ChildrenBuilder block
 * WILL NOT WORK FOR STATE UPDATES IN THE CALL SITE!
 */
// TODO: @mh
/*fun basicCustomContent(block: ChildrenBuilder.() -> Unit) =
  DialogConfig.CustomContent(component = BasicCustomContentDialog::class, setProps = {
    config = BasicCustomContentDialogConfig(block = {
      block()
    })
  })*/

class BasicCustomContentDialogConfig(
  val block: ChildrenBuilder.() -> Unit
)

external interface BasicCustomContentDialogProps : Props {
  var config: BasicCustomContentDialogConfig
}

class BasicCustomContentDialog(props: BasicCustomContentDialogProps) :
  RComponentWithCoroutineScope<BasicCustomContentDialogProps, State>(props) {

  override fun ChildrenBuilder.render() {
    props.config.block(this)
  }
}