package com.pgg.order.impl

import com.pgg.arouter_annotation.ARouter
import com.pgg.common.call.order.user.IUser
import com.pgg.common.call.order.user.BaseUser
import com.pgg.order.model.UserInfo

@ARouter(path = "/order/getUserInfo")
class OrderUserImpl : IUser {
    override fun getUserInfo(): BaseUser {
        // 我order模块，具体的Bean，由我自己
        val userInfo = UserInfo()
        userInfo.name = "Derry"
        userInfo.account = "154325354"
        userInfo.password = "1234567890"
        userInfo.vipLevel = 999
        return userInfo
    }
}