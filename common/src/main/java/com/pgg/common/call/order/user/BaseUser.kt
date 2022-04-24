package com.pgg.common.call.order.user

import java.io.Serializable

/**
 * 例如：用户实体父类
 */
open class BaseUser : Serializable {
    var name: String? = null
    var account: String? = null
    var password: String? = null
    var phoneNumber: String? = null
    var gender = 0
    override fun toString(): String {
        return "BaseUser{" +
                "name='" + name + '\'' +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", gender=" + gender +
                '}'
    }
}