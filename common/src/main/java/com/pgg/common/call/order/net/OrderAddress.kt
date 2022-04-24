package com.pgg.common.call.order.net

import com.pgg.arouter_api.impl.Call
import com.pgg.common.call.order.net.OrderBean
import java.io.IOException

/**
 * 订单模块对外暴露接口，其他模块可以获取返回业务数据
 */
interface OrderAddress : Call {
    @Throws(IOException::class)
    fun getOrderBean(key: String, ip: String): OrderBean?
}