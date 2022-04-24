package com.pgg.arouter_api.manager

import android.content.Context
import android.util.LruCache
import com.pgg.arouter_api.impl.ParameterGenerator

object ParameterManager {

    // LRU缓存 key=类名      value=参数加载接口
    private val cache = LruCache<String, ParameterGenerator>(100)
    private const val FILE_SUFFIX_NAME = "$\$Parameter" // 为了这个效果：Order_MainActivity + $$Parameter

    fun loadParameter(context: Context) {
        val name = context::class.java.simpleName
        if (cache[name] == null) {
            val clazz = Class.forName("${context.packageName}.${context::class.java.simpleName}$FILE_SUFFIX_NAME")
            cache.put(name, clazz.newInstance() as ParameterGenerator)
        }
        cache[name].generateParameter(context)
    }
}