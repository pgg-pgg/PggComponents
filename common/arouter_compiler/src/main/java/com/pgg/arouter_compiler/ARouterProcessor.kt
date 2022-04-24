package com.pgg.arouter_compiler

import com.google.auto.service.AutoService
import com.pgg.arouter_annotation.ARouter
import com.pgg.arouter_annotation.bean.RouterBean
import com.pgg.arouter_compiler.utils.ProcessorConfig
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

// AutoService是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor::class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes(value = [ProcessorConfig.AROUTER_PACKAGE])
// 注解处理器接收的参数
@SupportedOptions(value = [ProcessorConfig.APT_PACKAGE, ProcessorConfig.OPTIONS])
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ARouterProcessor : AbstractProcessor() {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private lateinit var elementTool: Elements

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private lateinit var typeTool: Types

    // Message用来打印 日志相关信息
    private lateinit var messager: Messager

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private lateinit var filer: Filer

    private lateinit var options: String
    private lateinit var aptPackage: String

    // 仓库一 Path  记录每一组内所有RouterBean的集合
    // Map<"personal", List<RouterBean>>
    private val mAllPathMap = HashMap<String, ArrayList<RouterBean>>() // 目前是一个


    // 仓库二 Group 缓存二
    // Map<"personal", "ARouter$$Path$$personal.class">
    private val mAllGroupMap = HashMap<String, String>()


    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)

        elementTool = processingEnv?.elementUtils ?: return
        typeTool = processingEnv.typeUtils ?: return
        messager = processingEnv.messager ?: return
        filer = processingEnv.filer ?: return
        // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
        options = processingEnv.options?.get(ProcessorConfig.OPTIONS) ?: ""
        aptPackage = processingEnv.options?.get(ProcessorConfig.APT_PACKAGE) ?: ""
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {

        if (annotations.isNullOrEmpty()) {
            printNote("没有发现被@ARouter注解的类")
            return false // 没有机会处理
        }
        val elements = roundEnv?.getElementsAnnotatedWith(ARouter::class.java) ?: return false
        val activityTypeMirror =
            elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE).asType()

        val callTypeMirror = elementTool.getTypeElement(ProcessorConfig.CALL).asType()

        elements.forEach { element ->
            printNote("被注解的类${element.simpleName.toString()}")
            val aRouter = element.getAnnotation(ARouter::class.java)
            val beanBuilder = RouterBean.Builder()
                .addElement(element)
                .addGroup(aRouter.group)
                .addPath(aRouter.path)
            val elementMirror = element.asType()
            when {
                typeTool.isSubtype(elementMirror, activityTypeMirror) -> {
                    beanBuilder.addType(RouterBean.TypeEnum.ACTIVITY)
                }
                typeTool.isSubtype(elementMirror, callTypeMirror) -> {
                    beanBuilder.addType(RouterBean.TypeEnum.CALL)
                }
                else -> {
                    printError("@ARouter注解目前仅限用于Activity和Call之上")
                }
            }
            val routerBean = beanBuilder.build()
            if (checkRouterPath(routerBean)) {
                // 赋值 mAllPathMap 集合里面去
                var routerBeans = mAllPathMap[routerBean.group]
                // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
                if (routerBeans.isNullOrEmpty()) { // 仓库一 没有东西
                    routerBeans = ArrayList()
                    routerBeans.add(routerBean)
                    mAllPathMap[routerBean.group] = routerBeans // 加入仓库一
                } else {
                    routerBeans.add(routerBean)
                }
            } else {
                printError("@ARouter注解未按规范配置，如：/app/MainActivity")
            }
        }
        val pathType =
            ClassName(ProcessorConfig.AROUTER_API_PACKAGE, ProcessorConfig.AROUTER_API_PATH)
        val groupType =
            ClassName(ProcessorConfig.AROUTER_API_PACKAGE, ProcessorConfig.AROUTER_API_GROUP)
        try {
            createPathFile(pathType)
        } catch (e: Exception) {
            printError(e.toString())
        }

        try {
            createGroupFile(groupType, pathType)
        } catch (e: Exception) {
            printError(e.toString())
        }
        return true
    }

    /**
     * 创建path文件
     */
    private fun createPathFile(pathType: ClassName) {
        /*
        模版
            class `ARouter$$Path$$app` : ARouterPath {
                    override fun getPathMap(): Map<String, RouterBean> {
                        val pathMap = HashMap<String, RouterBean>()
                         pathMap["/app/Main2Activity"] = RouterBean.create(
                                    RouterBean.TypeEnum.ACTIVITY,
                                    HashMap::class.java,
                                    "/app/Main2Activity",
                                    "app"
                              )
                        pathMap["/app/MainActivity"] = RouterBean.create(
                                    RouterBean.TypeEnum.ACTIVITY,
                                    HashMap::class.java,
                                    "/app/MainActivity",
                                    "app"
                            )
                         return pathMap
                    }
            }
         */

        //1. 生成方法  override fun getPathMap(): Map<String, RouterBean>
        //返回值
        val returnType = Map::class.parameterizedBy(String::class, RouterBean::class)
        mAllPathMap.forEach { (k, beans) ->
            val funcBuilder = FunSpec.builder(ProcessorConfig.PATH_METHOD_NAME)
                .addModifiers(KModifier.OVERRIDE)
                .returns(returnType)
            //val pathMap = HashMap<String, RouterBean>()
            funcBuilder.addStatement(
                "val %N = %T<%T,%T>()",
                ProcessorConfig.PATH_VAR1,
                HashMap::class,
                String::class,
                RouterBean::class
            )
            /*
            pathMap["/app/Main2Activity"] = RouterBean.create(
                RouterBean.TypeEnum.ACTIVITY,
                HashMap::class.java,
                "/app/Main2Activity",
                "app"
            )
            */
            beans.forEach { bean ->
                funcBuilder.addStatement(
                    "%N[%S] = %T.create(%T.%L,%T::class.java,%S,%S)",
                    ProcessorConfig.PATH_VAR1,
                    bean.path,
                    RouterBean::class,
                    RouterBean.TypeEnum::class,
                    bean.typeEnum,
                    bean.element as TypeElement,
                    bean.path,
                    bean.group
                )
            }
            //return pathMap
            funcBuilder.addStatement("return %N", ProcessorConfig.PATH_VAR1)

            //2. 生成类 class `ARouter$$Path$$app` : ARouterPath
            val finalName = ProcessorConfig.PATH_FILE_NAME + options
            val typeClass = TypeSpec.classBuilder(finalName)
                .addFunction(funcBuilder.build())
                .addSuperinterface(pathType)
                .build()

            FileSpec.builder(aptPackage, finalName)
                .addType(typeClass)
                .build()
                .writeTo(filer)

            // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
            mAllGroupMap[k] = finalName
        }
    }


    /**
     * 创建group文件
     */
    private fun createGroupFile(groupType: ClassName, pathType: ClassName) {
        /*
        模版
        class `ARouter$$Group$$personal` : ARouterGroup {
            override fun getGroupMap(): Map<String, Class<out ARouterPath?>> {
                val groupMap = HashMap<String, Class<out ARouterPath?>>()
                groupMap["personal"] = ARouterPath::class.java
                return groupMap
            }
        }
         */
        //返回值 Map<String, Class<out ARouterPath?>>
        val returnType = Map::class.asTypeName().parameterizedBy(
            String::class.asTypeName(),
            Class::class.asTypeName().parameterizedBy(
                WildcardTypeName.producerOf(pathType)
            ),
        )
        val funcBuilder = FunSpec.builder(ProcessorConfig.GROUP_METHOD_NAME)
            .addModifiers(KModifier.OVERRIDE)
            .returns(returnType)
        //val groupMap = HashMap<String, Class<out ARouterPath?>>()
        funcBuilder.addStatement(
            "val %N = %T<%T,%T<out %T>>()",
            ProcessorConfig.GROUP_VAR1,
            HashMap::class,
            String::class,
            Class::class,
            pathType
        )
        //groupMap["personal"] = ARouterPath::class.java
        mAllGroupMap.forEach { (k, v) ->
            funcBuilder.addStatement(
                "%N[%S] = %T::class.java",
                ProcessorConfig.GROUP_VAR1,
                k,
                ClassName(aptPackage, v)
            )
        }
        //return groupMap
        funcBuilder.addStatement("return %N", ProcessorConfig.GROUP_VAR1)

        // 最终生成的类文件名 ARouter$$Group$$ + personal
        val finalClassName = ProcessorConfig.GROUP_FILE_NAME + options
        FileSpec.builder(aptPackage, finalClassName)
            .addType(
                TypeSpec.classBuilder(finalClassName)
                    .addSuperinterface(groupType)
                    .addFunction(funcBuilder.build())
                    .build()
            )
            .build()
            .writeTo(filer)
    }

    private fun checkRouterPath(bean: RouterBean): Boolean {
        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
        if (bean.path.isBlank() || !bean.path.startsWith("/")) {
            printError("@ARouter注解中的path值，必须要以 / 开头")
            return false
        }

        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
        if (bean.path.lastIndexOf("/") == 0) {
            // 架构师定义规范，让开发者遵循
            printError("@ARouter注解未按规范配置，如：/app/MainActivity")
            return false
        }

        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
        val finalGroup: String = bean.path.substring(1, bean.path.indexOf("/", 1))

        // app,order,personal == options
        // @ARouter注解中的group有赋值情况
        if (bean.group.isNotBlank() && bean.group != options) {
            // 架构师定义规范，让开发者遵循
            printError("@ARouter注解中的group值必须和子模块名一致！")
            return false
        } else {
            bean.group = finalGroup
        }
        // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
        return true
    }

    private fun printNote(note: String?) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>$note")
    }

    private fun printError(note: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, "e: $note")
    }
}