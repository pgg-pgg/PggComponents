package com.pgg.arouter_api.manager

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.util.LruCache
import com.pgg.arouter_annotation.bean.RouterBean
import com.pgg.arouter_api.impl.ARouterGroup
import com.pgg.arouter_api.impl.ARouterPath
import com.pgg.arouter_api.impl.Call

/**
 * 整个目标
 * 第一步：查找 ARouter$$Group$$personal ---> ARouter$$Path$$personal
 * 第二步：使用 ARouter$$Group$$personal ---> ARouter$$Path$$personal
 */
object RouterManager {
    private var group: String = ""
    private var path: String = ""

    // 提供性能  LRU缓存
    private var groupLruCache: LruCache<String, ARouterGroup> = LruCache(100)
    private var pathLruCache: LruCache<String, ARouterPath> = LruCache(100)
    private const val FILE_GROUP_NAME = "ARouter$\$Group$$"

    /***
     * @param path 例如：/order/Order_MainActivity
     * * @return
     */
    fun build(path: String): BundleManager {
        require(!(TextUtils.isEmpty(path) || !path.startsWith("/"))) { "不按常理出牌 path乱搞的啊，正确写法：如 /order/Order_MainActivity" }

        require(path.lastIndexOf("/") != 0) {  // 只写了一个 /
            "不按常理出牌 path乱搞的啊，正确写法：如 /order/Order_MainActivity"
        }

        // 截取组名  /order/Order_MainActivity  finalGroup=order
        val finalGroup = path.substring(1, path.indexOf("/", 1)) // finalGroup = order
        require(!TextUtils.isEmpty(finalGroup)) { "不按常理出牌 path乱搞的啊，正确写法：如 /order/Order_MainActivity" }
        // 证明没有问题，没有抛出异常
        this.path = path // 最终的效果：如 /order/Order_MainActivity
        this.group = finalGroup // 例如：order，personal
        return BundleManager() // Builder设计模式 之前是写里面的， 现在写外面吧
    }

    fun navigation(context: Context, bundleManager: BundleManager): Call? {
        // 例如：寻找 ARouter$$Group$$personal  寻址   ARouter$$Group$$order   ARouter$$Group$$app
        val groupClassName = context.packageName + "." + FILE_GROUP_NAME + group
        //第一步 读取路由组Group类文件
        var loadGroup = groupLruCache[group]
        if (loadGroup == null) {
            // 加载APT路由组Group类文件 例如：ARouter$$Group$$order
            val aClass = Class.forName(groupClassName)
            // 初始化类文件
            loadGroup = aClass.newInstance() as ARouterGroup
            // 保存到缓存
            groupLruCache.put(group, loadGroup)
        }
        if (loadGroup.getGroupMap().isEmpty()) {
            throw RuntimeException("路由表Group报废了...") // Group这个类 加载失败
        }

        // 第二步 读取路由Path类文件
        var loadPath = pathLruCache[path]
        if (null == loadPath) { // 缓存里面没有东东 Path
            // 1.invoke loadGroup
            // 2.Map<String, Class<? extends ARouterLoadPath>>
            val clazz = loadGroup.getGroupMap()[group]!!

            // 3.从map里面获取 ARouter$$Path$$personal.class
            loadPath = clazz.newInstance()

            // 保存到缓存
            pathLruCache.put(path, loadPath)
        }

        // 第三步 跳转
        if (loadPath != null) { // 健壮
            if (loadPath.getPathMap().isEmpty()) { // pathMap.get("key") == null
                throw java.lang.RuntimeException("路由表Path报废了...")
            }

            // 最后才执行操作
            val routerBean: RouterBean? = loadPath.getPathMap()[path]
            if (routerBean != null) {
                when (routerBean.typeEnum) {
                    RouterBean.TypeEnum.ACTIVITY -> {
                        val intent = Intent(
                            context,
                            routerBean.clazz
                        ) // 例如：getClazz == Order_MainActivity.class
                        intent.putExtras(bundleManager.bundle) // 携带参数
                        // context.startActivity(intent, bundleManager.getBundle()); // 大部分手机有问题，没有任何反应
                        context.startActivity(intent)
                    }
                    RouterBean.TypeEnum.CALL -> {
                        // OrderAddressImpl.class  OrderBean getOrderBean
                        val clazz: Class<*>? = routerBean.clazz // OrderUserImpl BaseUser实体
                        val call: Call? = clazz?.newInstance() as? Call
                        bundleManager.call = call
                        return bundleManager.call
                    }
                }
            }
        }
        return null
    }
}