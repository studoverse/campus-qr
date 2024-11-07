#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import react.Props
import webcore.FcWithCoroutineScope
import js.lazy.Lazy

external interface ${NAME}Props : Props {
  var config: ${NAME}Config
}

@Lazy
val ${NAME} = FcWithCoroutineScope<${NAME}Props> { props, launch ->
  val controller = ${NAME}Controller.use${NAME}Controller(launch = launch)
}