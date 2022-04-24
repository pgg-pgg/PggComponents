package com.pgg.order.services

import retrofit2.http.POST
import retrofit2.http.FormUrlEncoded
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Field

/**
 * // 相当于是图片资源
 * 订单模块特有业务，其他模块最好不要干涉
 */
interface OrderServices {
    @POST("/ip/ipNew")
    @FormUrlEncoded
    fun get(@Field("ip") ip: String, @Field("key") key: String): Call<ResponseBody>
}