package com.pgg.order.model

import com.pgg.common.call.order.user.BaseUser

// 相当于是图片资源 丢了一个Bean进来
class UserInfo : BaseUser() {
    var token: String? = null
    var vipLevel = 0
}