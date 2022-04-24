package com.pgg.order.impl

import com.pgg.arouter_annotation.ARouter
import com.pgg.common.call.order.net.OrderAddress
import com.pgg.common.call.order.net.OrderBean
import retrofit2.Retrofit
import com.pgg.order.impl.OrderAddressImpl
import com.pgg.order.services.OrderServices
import okhttp3.ResponseBody
import com.alibaba.fastjson.JSON
import retrofit2.Call
import java.io.IOException

/**
 * 订单模块对外暴露接口实现类，其他模块可以获取返回数据
 */
@ARouter(path = "/order/getOrderBean") // /order/getOrderBean
class OrderAddressImpl : OrderAddress {
    // 暴漏给 各个模块使用
    @Throws(IOException::class)
    override fun getOrderBean(key: String, ip: String): OrderBean? {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .build()
        val host = retrofit.create(OrderServices::class.java)

        // Retrofit GET同步请求
        val call = host.get(ip, key)
        val response = call.execute()
        if (response.body() != null) {
            val jsonObject = JSON.parseObject(response.body()!!.string())
            val orderBean = jsonObject.toJavaObject(OrderBean::class.java)
            println("order订单组件中独有的网络请求功能：解析后结果 >>> $orderBean")
            return orderBean
        }
        return null
    }

    companion object {
        private const val BASE_URL = "https://apis.juhe.cn/"
    }
}