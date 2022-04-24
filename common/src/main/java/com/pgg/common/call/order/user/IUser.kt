package com.pgg.common.call.order.user

import com.pgg.arouter_api.impl.Call
import com.pgg.common.call.order.user.BaseUser

interface IUser : Call {
    /**
     * @return 根据不同子模块的具体实现，调用得到不同的结果
     */
    fun getUserInfo(): BaseUser
}