package com.pgg.personal

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.pgg.arouter_annotation.ARouter
import com.pgg.arouter_annotation.Parameter
import com.pgg.arouter_api.manager.ParameterManager
import com.pgg.arouter_api.manager.RouterManager
import com.pgg.common.call.order.drawable.OrderDrawable
import com.pgg.common.call.order.net.OrderAddress

@ARouter(path = "/personal/PersonalActivity")
class PersonalActivity : AppCompatActivity() {

    @Parameter(paramsName = "/order/getDrawable")
    var orderDrawable: OrderDrawable? = null

    @Parameter(paramsName = "/order/getOrderBean")
    var orderAddress: OrderAddress? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal)
        ParameterManager.loadParameter(this)
        orderDrawable?.getDrawable()?.let {
            findViewById<ImageView>(R.id.iv_container).setImageResource(it)
        }

        Thread {
            try {
                val orderBean =
                    orderAddress?.getOrderBean("aa205eeb45aa76c6afe3c52151b52160", "144.34.161.97")
                Log.e("Cons.TAG", "从Personal跨组件到Order，并使用Order网络请求功能：" + orderBean.toString())
            }catch (e: Exception) {
                Log.e("PersonActivity",e.toString())
            }

        }.start()
    }

    fun jumpApp(view: View?) {
        RouterManager.build("/app/MainActivity")
            .navigation(this)
    }

    fun jumpOrder(view: View?) {
        RouterManager.build("/order/OrderActivity")
            .navigation(this)
    }
}