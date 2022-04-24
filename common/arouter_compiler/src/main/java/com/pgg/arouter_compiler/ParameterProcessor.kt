package com.pgg.arouter_compiler

import com.google.auto.service.AutoService
import com.pgg.arouter_annotation.Parameter
import com.pgg.arouter_compiler.utils.ProcessorConfig
import com.squareup.kotlinpoet.*
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

// AutoService是固定的写法，加个注解即可
// 通过auto-service中的@AutoService可以自动生成AutoService注解处理器，用来注册
// 用来生成 META-INF/services/javax.annotation.processing.Processor 文件
@AutoService(Processor::class)
// 允许/支持的注解类型，让注解处理器处理
@SupportedAnnotationTypes(value = [ProcessorConfig.PARAMETER_PACKAGE])
// 注解处理器接收的参数
@SupportedOptions(value = [ProcessorConfig.APT_PACKAGE, ProcessorConfig.OPTIONS])
// 指定JDK编译版本
@SupportedSourceVersion(SourceVersion.RELEASE_8)
class ParameterProcessor : AbstractProcessor() {

    // 操作Element的工具类（类，函数，属性，其实都是Element）
    private lateinit var elementTool: Elements

    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
    private lateinit var typeTool: Types

    // Message用来打印 日志相关信息
    private lateinit var messager: Messager

    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
    private lateinit var filer: Filer

    // 临时map存储，用来存放被@Parameter注解的属性集合，生成类文件时遍历
    // key:类节点, value:被@Parameter注解的属性集合
    private val tempParameterMap = HashMap<TypeElement, ArrayList<Element>>()
    private lateinit var aptPackage: String

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        elementTool = processingEnv?.elementUtils ?: return
        typeTool = processingEnv.typeUtils ?: return
        messager = processingEnv.messager ?: return
        filer = processingEnv.filer ?: return
        aptPackage = processingEnv.options?.get(ProcessorConfig.APT_PACKAGE) ?: ""
    }

    override fun process(
        annotations: MutableSet<out TypeElement>?,
        roundEnv: RoundEnvironment?
    ): Boolean {
        if (!annotations.isNullOrEmpty()) {
            // 获取所有被 @Parameter 注解的 元素（属性）集合
            val elements = roundEnv?.getElementsAnnotatedWith(Parameter::class.java) ?: return false
            elements.forEach { element -> // element == name， sex,  age
                //获取父节点的类型 com.pgg.pggcomponents.MainActivity
                val enclosingElement = element.enclosingElement as TypeElement

                if (tempParameterMap.containsKey(enclosingElement)) {
                    tempParameterMap[enclosingElement]!!.add(element)
                } else {
                    tempParameterMap[enclosingElement] = arrayListOf(element)
                }
            }

            if (tempParameterMap.isNullOrEmpty()) return true
            val parameterGenerator = ClassName(
                ProcessorConfig.AROUTER_API_PACKAGE,
                ProcessorConfig.AROUTER_AIP_PARAMETER_GET
            )
            tempParameterMap.forEach { (typeElement, elementList) ->
                createParameterFile(typeElement, elementList)
            }
        }
        return false
    }

    private fun createParameterFile(typeElement: TypeElement, elementList: List<Element>) {
        /*
        模版
            class `Order_MainActivity$$Parameter` : ParameterGenerator {
                override fun generateParameter(targetObject: Any) {
                     val t = targetObject as MainActivity
                     t.name =  t.intent.getStringExtra("name")
                     t.orderDrawable = RouterManager.build("/order/getDrawable").navigation(t) as OrderDrawable
                     t.iUser = RouterManager.build("/order/getUserInfo").navigation(t) as IUser
                     t.student = t.intent.getSerializableExtra("student") as Student?
                }
            }
        */
        val callTypeMirror = elementTool.getTypeElement(ProcessorConfig.CALL).asType()
        val funcBuilder = FunSpec.builder(ProcessorConfig.PARAMETER_METHOD_NAME)
            .addParameter(ProcessorConfig.PARAMETER_NAME, Any::class)
            .addModifiers(KModifier.OVERRIDE)
            .addStatement(
                "val %N = %N as %T",
                ProcessorConfig.PARAMETER_METHOD_VAR,
                ProcessorConfig.PARAMETER_NAME,
                typeElement
            )
        elementList.forEach { element ->
            // 获取 TypeKind 枚举类型的序列号
            val typeMirror = element.asType()
            val typeKind = typeMirror.kind.ordinal
            // 获取属性名  name  age  sex
            val fieldName = element.simpleName.toString()
            val paramsName =
                element.getAnnotation(Parameter::class.java).paramsName.ifBlank { fieldName }
            val leftContent = "${ProcessorConfig.PARAMETER_METHOD_VAR}.$fieldName"
            val methodContentBuilder = StringBuilder()
            methodContentBuilder.append("$leftContent = ${ProcessorConfig.PARAMETER_METHOD_VAR}.intent.")
            // TypeKind 枚举类型不包含String
            when (typeKind) {
                TypeKind.INT.ordinal -> {
                    // t.s = t.getIntent().getIntExtra("age", t.age);
                    methodContentBuilder.append("getIntExtra(%S, $leftContent)")
                }
                TypeKind.BOOLEAN.ordinal -> {
                    // t.s = t.getIntent().getBooleanExtra("isSuccess", t.age);
                    methodContentBuilder.append("getBooleanExtra(%S, $leftContent)")
                }
                else -> {
                    // String 类型，没有序列号的提供 需要我们自己完成
                    // t.name =  t.intent.getStringExtra("name")
                    // typeMirror.toString() java.lang.String
                    when {
                        typeMirror.toString() == ProcessorConfig.STRING_PACKAGE -> {
                            methodContentBuilder.append("getStringExtra(%S)")
                        }
                        typeTool.isSubtype(typeMirror, callTypeMirror) -> {
                            //call 类型
                            //t.orderDrawable = RouterManager.build("/order/getDrawable").navigation(t) as OrderDrawable
                            funcBuilder.addStatement(
                                "$leftContent = %T.build(%S).navigation(t) as %T",
                                ClassName(
                                    ProcessorConfig.AROUTER_MANAGER_PACKAGE,
                                    ProcessorConfig.ROUTER_MANAGER
                                ),
                                paramsName,
                                typeMirror
                            )
                            return@forEach
                        }
                        else -> {
                            // 对象的传输
                            //t.student = t.intent.getSerializableExtra("student") as Student?
                            funcBuilder.addStatement(
                                "$leftContent = t.intent.getSerializableExtra(%S) as %T",
                                paramsName,
                                typeMirror
                            )
                            return@forEach
                        }
                    }
                }
            }
            funcBuilder.addStatement(methodContentBuilder.toString(), paramsName)
        }
        val finalClassName = typeElement.simpleName.toString() + ProcessorConfig.PARAMETER_FILE_NAME
        FileSpec.builder(aptPackage, finalClassName)
            .addType(
                TypeSpec.classBuilder(finalClassName)
                    .addFunction(funcBuilder.build())
                    .addSuperinterface(
                        ClassName(
                            ProcessorConfig.AROUTER_API_PACKAGE,
                            ProcessorConfig.AROUTER_AIP_PARAMETER_GET
                        )
                    )
                    .build()
            )
            .build()
            .writeTo(filer)
    }


    private fun printNote(note: String?) {
        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>ParameterProcessor$note")
    }

    private fun printError(note: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, "e ParameterProcessor: $note")
    }
}