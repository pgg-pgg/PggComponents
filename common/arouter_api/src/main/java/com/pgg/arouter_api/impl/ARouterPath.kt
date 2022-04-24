package com.pgg.arouter_api.impl

import com.pgg.arouter_annotation.bean.RouterBean

/**
 * 其实就是 路由组 Group 对应的 ---- 详细Path加载数据接口 ARouterPath
 * 例如：order分组 对应 ---- 有那些类需要加载（Order_MainActivity  Order_MainActivity2 ...）
 *
 *
 * TODO
 * key:   /app/MainActivity1
 * value:  RouterBean(MainActivity1.class)
 *
 */
interface ARouterPath {
    /**
     * 例如：order分组下有这些信息，personal分组下有这些信息
     *
     * @return key:"/order/Order_MainActivity"   或  "/personal/Personal_MainActivity"
     * value: RouterBean==Order_MainActivity.class 或 RouterBean=Personal_MainActivity.class
     */
    fun getPathMap(): Map<String, RouterBean>
}