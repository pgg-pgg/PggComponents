package com.pgg.order.impl

import com.pgg.arouter_annotation.ARouter
import com.pgg.common.call.order.drawable.OrderDrawable
import com.pgg.order.R

@ARouter(path = "/order/getDrawable")
class OrderDrawableImpl : OrderDrawable {
    override fun getDrawable(): Int {
        return R.drawable.ic_launcher_background
    }
}