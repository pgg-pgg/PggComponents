package com.pgg.arouter_compiler.utils

object ProcessorConfig {
    // @ARouter注解 的 包名 + 类名
    const val AROUTER_PACKAGE = "com.pgg.arouter_annotation.ARouter"

    // 接收参数的TAG标记
    const val OPTIONS = "moduleName" //目的是接收 每个module名称

    const val APT_PACKAGE = "packageNameForAPT" //目的是接收 包名（APT 存放的包名）

    // String全类名
    const val STRING_PACKAGE = "java.lang.String"

    // Activity全类名
    const val ACTIVITY_PACKAGE = "android.app.Activity"

    // ARouter api 包名
    const val AROUTER_API_PACKAGE = "com.pgg.arouter_api.impl"
    const val AROUTER_MANAGER_PACKAGE = "com.pgg.arouter_api.manager"

    // ARouter api 的 ARouterGroup 高层标准
    const val AROUTER_API_GROUP = "ARouterGroup"

    // ARouter api 的 ARouterPath 高层标准
    const val AROUTER_API_PATH = "ARouterPath"

    // 路由组，中的 Path 里面的 方法名
    const val PATH_METHOD_NAME = "getPathMap"

    // 路由组，中的 Group 里面的 方法名
    const val GROUP_METHOD_NAME = "getGroupMap"

    // 路由组，中的 Path 里面 的 变量名 1
    const val PATH_VAR1 = "pathMap"

    // 路由组，中的 Group 里面 的 变量名 1
    const val GROUP_VAR1 = "groupMap"

    // 路由组，PATH 最终要生成的 文件名
    const val PATH_FILE_NAME = "ARouter$\$Path$$"

    // 路由组，GROUP 最终要生成的 文件名
    const val GROUP_FILE_NAME = "ARouter$\$Group$$"


    // @Parameter注解 的 包名 + 类名
    const val PARAMETER_PACKAGE = "com.pgg.arouter_annotation.Parameter"

    // ARouter api 的 ParameterGenerator 高层标准
    const val AROUTER_AIP_PARAMETER_GET = "ParameterGenerator"

    // ARouter api 的 generateParameter 方法参数的名字
    const val PARAMETER_NAME = "targetObject"

    // ARouter api 的 generateParameter 方法的名字
    const val PARAMETER_METHOD_NAME = "generateParameter"

    const val PARAMETER_METHOD_VAR = "t"


    // ARouter aip 的 ParmeterGet 的 生成文件名称 $$Parameter
    const val PARAMETER_FILE_NAME = "$\$Parameter"

    // String全类名
    const val STRING = "java.lang.String"

    // ARouter api 的 Call 高层标准
    const val CALL = "$AROUTER_API_PACKAGE.Call"

    // RouterManager类名
    const val ROUTER_MANAGER = "RouterManager"

}