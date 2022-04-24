package com.pgg.arouter_api.manager

import android.content.Context
import android.os.Bundle
import com.pgg.arouter_api.impl.Call
import java.io.Serializable

/**
 * 跳转时 ，用于参数的传递
 */
class BundleManager {
    // Intent传输  携带的值，保存到这里
    var bundle = Bundle()

    // TODO 新增点
    // 底层业务接口
    var call: Call? = null

    // 对外界提供，可以携带参数的方法
    fun withString(key: String, value: String?): BundleManager {
        bundle.putString(key, value)
        return this // 链式调用效果 模仿开源框架
    }

    fun withBoolean(key: String, value: Boolean): BundleManager {
        bundle.putBoolean(key, value)
        return this
    }

    fun withInt(key: String, value: Int): BundleManager {
        bundle.putInt(key, value)
        return this
    }

    fun withSerializable(key: String, `object`: Serializable?): BundleManager {
        bundle.putSerializable(key, `object`)
        return this
    }

    fun withBundle(bundle: Bundle): BundleManager {
        this.bundle = bundle
        return this
    }

    // 直接完成跳转
    fun navigation(context: Context): Call? {
        return RouterManager.navigation(context, this)
    }
}