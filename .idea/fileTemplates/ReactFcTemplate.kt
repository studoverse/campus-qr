#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import react.Props
import webcore.FcWithCoroutineScope

external interface ${NAME}Props : Props {
  var config: ${NAME}Config
}

val ${NAME} = FcWithCoroutineScope<${NAME}Props> { props, launch ->
  val controller = ${NAME}Controller.use${NAME}Controller(launch = launch)
}