#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}
#end

import webcore.Launch

data class ${NAME}(
  
) {
  companion object {
    fun use(launch: Launch): ${NAME} {
      
      return ${NAME}(
        
      )
    }
  }
}