package com.pgg.common.call.order.drawable

import com.pgg.arouter_api.impl.Call

interface OrderDrawable : Call {
    fun getDrawable(): Int
}