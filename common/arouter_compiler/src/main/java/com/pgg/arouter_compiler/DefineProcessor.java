//package com.pgg.arouter_compiler;
//
//import com.google.auto.service.AutoService;
//import com.pgg.arouter_annotation.ARouter;
//import com.pgg.arouter_annotation.bean.RouterBean;
//import com.pgg.arouter_compiler.utils.ProcessorConfig;
//import com.pgg.arouter_compiler.utils.ProcessorUtils;
//import com.squareup.javapoet.ClassName;
//import com.squareup.javapoet.JavaFile;
//import com.squareup.javapoet.MethodSpec;
//import com.squareup.javapoet.ParameterizedTypeName;
//import com.squareup.javapoet.TypeName;
//import com.squareup.javapoet.TypeSpec;
//import com.squareup.javapoet.WildcardTypeName;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//
//import javax.annotation.processing.AbstractProcessor;
//import javax.annotation.processing.Filer;
//import javax.annotation.processing.Messager;
//import javax.annotation.processing.ProcessingEnvironment;
//import javax.annotation.processing.Processor;
//import javax.annotation.processing.RoundEnvironment;
//import javax.annotation.processing.SupportedAnnotationTypes;
//import javax.annotation.processing.SupportedOptions;
//import javax.annotation.processing.SupportedSourceVersion;
//import javax.lang.model.SourceVersion;
//import javax.lang.model.element.Element;
//import javax.lang.model.element.Modifier;
//import javax.lang.model.element.TypeElement;
//import javax.lang.model.type.TypeMirror;
//import javax.lang.model.util.Elements;
//import javax.lang.model.util.Types;
//import javax.tools.Diagnostic;
//
//@AutoService(Processor.class)
//@SupportedAnnotationTypes({ProcessorConfig.AROUTER_PACKAGE})
//@SupportedOptions({ProcessorConfig.OPTIONS, ProcessorConfig.APT_PACKAGE})
//@SupportedSourceVersion(SourceVersion.RELEASE_8)
//public class DefineProcessor extends AbstractProcessor {
//
//    // 操作Element的工具类（类，函数，属性，其实都是Element）
//    private Elements elementTool;
//
//    // type(类信息)的工具类，包含用于操作TypeMirror的工具方法
//    private Types typeTool;
//
//    // Message用来打印 日志相关信息
//    private Messager messager;
//
//    // 文件生成器， 类 资源 等，就是最终要生成的文件 是需要Filer来完成的
//    private Filer filer;
//
//    private String options; // 各个模块传递过来的模块名 例如：app order personal
//    private String aptPackage; // 各个模块传递过来的目录 用于统一存放 apt生成的文件
//
//    // 仓库一 Path  缓存一
//    // Map<"personal", List<RouterBean>>
//    private Map<String, List<RouterBean>> mAllPathMap = new HashMap<>(); // 目前是一个
//
//    // 仓库二 Group 缓存二
//    // Map<"personal", "ARouter$$Path$$personal.class">
//    private Map<String, String> mAllGroupMap = new HashMap<>();
//
//    @Override
//    public synchronized void init(ProcessingEnvironment processingEnv) {
//        super.init(processingEnv);
//        elementTool = processingEnv.getElementUtils();
//        typeTool = processingEnv.getTypeUtils();
//        messager = processingEnv.getMessager();
//        filer = processingEnv.getFiler();
//        // 只有接受到 App壳 传递过来的书籍，才能证明我们的 APT环境搭建完成
//        options = processingEnv.getOptions().get(ProcessorConfig.OPTIONS);
//        aptPackage = processingEnv.getOptions().get(ProcessorConfig.APT_PACKAGE);
//        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> options:" + options);
//        messager.printMessage(Diagnostic.Kind.NOTE, ">>>>>>>>>>>>>>>>>>>>>> aptPackage:" + aptPackage);
//        if (options != null && aptPackage != null) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境搭建完成....");
//        } else {
//            messager.printMessage(Diagnostic.Kind.NOTE, "APT 环境有问题，请检查 options 与 aptPackage 为null...");
//        }
//    }
//
//    @Override
//    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
//
//        if (set.isEmpty()) {
//            messager.printMessage(Diagnostic.Kind.NOTE, "并没有发现 被@ARouter注解的地方呀");
//            return false; // 没有机会处理
//        }
//
//        // 获取所有被 @ARouter 注解的 元素集合
//        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ARouter.class);
//
//        // 通过Element工具类，获取Activity，Callback类型
//        TypeElement activityType = elementTool.getTypeElement(ProcessorConfig.ACTIVITY_PACKAGE);
//        // 显示类信息（获取被注解的节点，类节点）这也叫自描述 Mirror
//        TypeMirror activityMirror = activityType.asType();
//
//        for (Element element : elements) {
//            // 获取简单类名，例如：MainActivity
//            String className = element.getSimpleName().toString();
//            // 打印出 就证明APT没有问题
//            messager.printMessage(Diagnostic.Kind.NOTE, "被@ARetuer注解的类有：" + className);
//            //获取注解
//            ARouter aRouter = element.getAnnotation(ARouter.class);
//            //创建RouterBean
//            RouterBean.Builder beanBuild = new RouterBean.Builder()
//                    .addGroup(aRouter.group())
//                    .addPath(aRouter.path())
//                    .addElement(element);
//            TypeMirror elementMirror = element.asType();
//            if (typeTool.isSubtype(elementMirror, activityMirror)) {
//                beanBuild.addType(RouterBean.TypeEnum.ACTIVITY);
//            } else {
//                // 不匹配抛出异常，这里谨慎使用！考虑维护问题
//                throw new RuntimeException("@ARouter注解目前仅限用于RouterBean.TypeEnum内定义的类之上");
//            }
//            RouterBean bean = beanBuild.build();
//
//            if (checkRouterPath(bean)) {
//                messager.printMessage(Diagnostic.Kind.NOTE, "RouterBean Check Success:" + bean.toString());
//                List<RouterBean> routerBeans = mAllPathMap.get(bean.getGroup());
//                // 如果从Map中找不到key为：bean.getGroup()的数据，就新建List集合再添加进Map
//                if (routerBeans == null) { // 仓库一 没有东西
//                    routerBeans = new ArrayList<>();
//                    routerBeans.add(bean);
//                    mAllPathMap.put(bean.getGroup(), routerBeans);// 加入仓库一
//                } else {
//                    routerBeans.add(bean);
//                }
//            } else {
//                messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
//            }
//        }
//
//        messager.printMessage(Diagnostic.Kind.NOTE, "pathType" + ProcessorConfig.AROUTER_API_PATH);
//
//        // mAllPathMap 里面有值了
//        // 定义（生成类文件实现的接口） 有 Path Group
//        TypeElement pathType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_PATH); // ARouterPath描述
//        TypeElement groupType = elementTool.getTypeElement(ProcessorConfig.AROUTER_API_GROUP); // ARouterGroup描述
//
//        //1. 创建path
//        try {
//            createPathFile(pathType); // 生成 Path类
//        } catch (Exception e) {
//            e.printStackTrace();
//            messager.printMessage(Diagnostic.Kind.NOTE, "在生成PATH模板时，异常了 e:" + e.getMessage());
//        }
//
//        //2. 创建group
//        try {
//            createGroupFile(groupType, pathType);
//        } catch (IOException e) {
//            e.printStackTrace();
//            messager.printMessage(Diagnostic.Kind.NOTE, "在生成GROUP模板时，异常了 e:" + e.getMessage());
//        }
//
//        return true;
//    }
//
//    /**
//     * 创建Path类
//     *
//     * @param pathType ARouterPath接口描述
//     * @throws IOException
//     */
//    private void createPathFile(TypeElement pathType) throws Exception {
//        /* 模版
//            public class ARouter$$Path$$app implements ARouterPath {
//                 @Override
//                 public Map<String, RouterBean> getPathMap() {
//                    Map<String, RouterBean> pathMap = new HashMap<>();
//                    pathMap.put("/app/Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Main2Activity.class, "/app/Main2Activity", "app"));
//                    pathMap.put("/app/MainActivity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, MainActivity.class, "/app/MainActivity", "app"));
//                    return pathMap;
//                }
//            }
//         */
//
//        // 判断 map仓库中，是否有需要生成的文件
//        if (ProcessorUtils.isEmpty(mAllPathMap)) {
//            return; // 连缓存一 仓库一 里面 值都没有 不用干活了
//        }
//
//        //先搞定返回值 Map<String, RouterBean>
//        TypeName parameterizedTypeName = ParameterizedTypeName.get(
//                ClassName.get(Map.class),
//                ClassName.get(String.class),
//                ClassName.get(RouterBean.class)
//        );
//
//        //遍历mAllPathMap， 为每一个模块生成对应的path文件
//        for (Map.Entry<String, List<RouterBean>> entry : mAllPathMap.entrySet()) {
//            //1. 先写方法
//            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(ProcessorConfig.PATH_METHOD_NAME)
//                    .addModifiers(Modifier.PUBLIC) // public修饰符
//                    .addAnnotation(Override.class) // 给方法上添加注解  @Override
//                    .returns(parameterizedTypeName); // 把Map<String, RouterBean> 加入方法返回
//
//            //给方法添加这一行Map<String, RouterBean> pathMap = new HashMap<>();
//            methodBuilder.addStatement("$T<$T,$T> $N = new $T<>()",
//                    ClassName.get(Map.class),
//                    ClassName.get(String.class),
//                    ClassName.get(RouterBean.class),
//                    ProcessorConfig.PATH_VAR1,
//                    ClassName.get(HashMap.class)
//            );
//            //循环添加
//            //pathMap.put("/app/Main2Activity", RouterBean.create(RouterBean.TypeEnum.ACTIVITY, Main2Activity.class, "/app/Main2Activity", "app"));
//            for (RouterBean bean : entry.getValue()) {
//                methodBuilder.addStatement("$N.put($S, $T.create($T.$L,$T.class,$S,$S))",
//                        ProcessorConfig.PATH_VAR1,
//                        bean.getPath(),
//                        ClassName.get(RouterBean.class),
//                        ClassName.get(RouterBean.TypeEnum.class),
//                        bean.getTypeEnum(),
//                        ClassName.get((TypeElement) Objects.requireNonNull(bean.getElement())),
//                        bean.getPath(),
//                        bean.getGroup()
//                );
//            }
//
//            //添加 return pathMap;
//            methodBuilder.addStatement("return $N", ProcessorConfig.PATH_VAR1);
//
//            // 最终生成的类文件名  ARouter$$Path$$personal
//            String finalClassName = ProcessorConfig.PATH_FILE_NAME + entry.getKey();
//
//            messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由Path类文件：" +
//                    aptPackage + "." + finalClassName);
//
//            //2.生成类
//            TypeSpec pathClass = TypeSpec.classBuilder(finalClassName)
//                    .addSuperinterface(ClassName.get(pathType))
//                    .addModifiers(Modifier.PUBLIC)
//                    .addMethod(methodBuilder.build())
//                    .build();
//            messager.printMessage(Diagnostic.Kind.NOTE, "pathClass：" +
//                    pathClass.toString());
//
//            //3.构建文件
//            //public class ARouter$$Path$$app implements ARouterPath
//            JavaFile.builder(aptPackage, pathClass)
//                    .build()
//                    .writeTo(filer);
//            // 仓库二 缓存二  非常重要一步，注意：PATH 路径文件生成出来了，才能赋值路由组mAllGroupMap
//            mAllGroupMap.put(entry.getKey(), finalClassName);
//        }
//
//    }
//
//    /**
//     * 创建group类
//     *
//     * @param groupType ARouterGroup接口描述
//     * @param pathType  ARouterPath接口描述
//     * @throws IOException
//     */
//    private void createGroupFile(TypeElement groupType, TypeElement pathType) throws IOException {
//        /*
//        模版
//        public class ARouter$$Group$$app implements ARouterGroup {
//             @Override
//             public Map<String, Class<? extends ARouterPath>> getGroupMap() {
//                 Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
//                 groupMap.put("app", ARouter$$Path$$app.class);
//                 return groupMap;
//            }
//        }
//         */
//
//        if (mAllGroupMap.isEmpty()) {
//            return;
//        }
//
//        //搞定返回值 Map<String, Class<? extends ARouterPath>>
//        ParameterizedTypeName groupReturns = ParameterizedTypeName.get(
//                ClassName.get(Map.class),
//                ClassName.get(String.class),
//                ParameterizedTypeName.get(ClassName.get(Class.class),
//                        WildcardTypeName.subtypeOf(ClassName.get(pathType))
//                )
//        );
//
//        //1.生成方法
//        //public Map<String, Class<? extends ARouterPath>> getGroupMap()
//        MethodSpec.Builder groupMethodBuild = MethodSpec.methodBuilder(ProcessorConfig.GROUP_METHOD_NAME)
//                .returns(groupReturns)
//                .addAnnotation(Override.class)
//                .addModifiers(Modifier.PUBLIC);
//
//        //添加 Map<String, Class<? extends ARouterPath>> groupMap = new HashMap<>();
//        groupMethodBuild.addStatement("$T<$T, $T> $N = new $T<>()",
//                ClassName.get(Map.class),
//                ClassName.get(String.class),
//                ParameterizedTypeName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(pathType)),
//                ProcessorConfig.GROUP_VAR1,
//                ClassName.get(HashMap.class)
//                );
//
//        //循环添加
//        //groupMap.put("app", ARouter$$Path$$app.class);
//        for (Map.Entry<String, String> entry : mAllGroupMap.entrySet()) {
//            groupMethodBuild.addStatement("$N.put($S, $T.class",
//                    ProcessorConfig.GROUP_VAR1,
//                    entry.getKey(),
//                    ClassName.get(aptPackage, entry.getValue())
//                    );
//        }
//        //添加 return groupMap;
//        groupMethodBuild.addStatement("return $N", ProcessorConfig.GROUP_VAR1);
//
//        // 最终生成的类文件名 ARouter$$Group$$ + personal
//        String finalClassName = ProcessorConfig.GROUP_FILE_NAME + options;
//
//        messager.printMessage(Diagnostic.Kind.NOTE, "APT生成路由组Group类文件：" +
//                aptPackage + "." + finalClassName);
//
//        //2.生成类
//        //public class ARouter$$Group$$app implements ARouterGroup
//        TypeSpec groupClass = TypeSpec.classBuilder(finalClassName)
//                .addMethod(groupMethodBuild.build())
//                .addModifiers(Modifier.PUBLIC)
//                .addSuperinterface(ClassName.get(groupType))
//                .build();
//
//        //3.生成文件
//        JavaFile.builder(aptPackage, groupClass)
//                .build()
//                .writeTo(filer);
//
//    }
//
//    /**
//     * 校验@ARouter注解的值，如果group未填写就从必填项path中截取数据
//     *
//     * @param bean 路由详细信息，最终实体封装类
//     */
//    private boolean checkRouterPath(RouterBean bean) {
//        String group = bean.getGroup(); //"app"   "order"   "personal"
//        String path = bean.getPath(); //"/app/MainActivity"   "/order/OrderActivity"   "/personal/PersonalActivity"
//        // 校验
//        // @ARouter注解中的path值，必须要以 / 开头（模仿阿里Arouter规范）
//        if (ProcessorUtils.isEmpty(path) || !path.startsWith("/")) {
//            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的path值，必须要以 / 开头");
//            return false;
//        }
//        // 比如开发者代码为：path = "/MainActivity"，最后一个 / 符号必然在字符串第1位
//        if (path.lastIndexOf("/") == 0) {
//            // 架构师定义规范，让开发者遵循
//            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解未按规范配置，如：/app/MainActivity");
//            return false;
//        }
//        // 从第一个 / 到第二个 / 中间截取，如：/app/MainActivity 截取出 app,order,personal 作为group
//        String finalGroup = path.substring(1, path.indexOf("/", 1));
//        // @ARouter注解中的group有赋值情况
//        if (!ProcessorUtils.isEmpty(group) && !group.equals(options)) {
//            // 架构师定义规范，让开发者遵循
//            messager.printMessage(Diagnostic.Kind.ERROR, "@ARouter注解中的group值必须和子模块名一致！");
//            return false;
//        } else {
//            bean.setGroup(finalGroup);
//        }
//
//        // 如果真的返回ture   RouterBean.group  xxxxx 赋值成功 没有问题
//        return true;
//    }
//}
