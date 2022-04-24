package com.pgg.common.call.order.net

/**
 * {"resultcode":"200","reason":"查询成功",
 * "result":{"Country":"美国","Province":"加利福尼亚","City":"","Isp":""},"error_code":0}
 */
data class OrderBean(
    val resultcode: String?,
    val reason: String?,
    val result: Result?,
    val error_code: Int
) {


    data class Result(
        val country: String?,
        val province: String?,
        val city: String?,
        val isp: String?
    ) {

        override fun toString(): String {
            return "Result{" +
                    "Country='" + country + '\'' +
                    ", Province='" + province + '\'' +
                    ", City='" + city + '\'' +
                    ", Isp='" + isp + '\'' +
                    '}'
        }
    }

    override fun toString(): String {
        return "OrderBean{" +
                "resultcode='" + resultcode + '\'' +
                ", reason='" + reason + '\'' +
                ", result=" + result +
                ", error_code=" + error_code +
                '}'
    }
}