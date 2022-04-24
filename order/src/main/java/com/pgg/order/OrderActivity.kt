package com.pgg.order

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pgg.arouter_annotation.ARouter
import com.pgg.arouter_annotation.Parameter
import com.pgg.arouter_api.manager.ParameterManager
import com.pgg.arouter_api.manager.RouterManager

@ARouter(path = "/order/OrderActivity")
class OrderActivity : AppCompatActivity() {

    @Parameter
    var name: String? = null
    @Parameter
    var age: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order)
        ParameterManager.loadParameter(this)
        Toast.makeText(this, "接受到的参数为 name = $name age = $age", Toast.LENGTH_SHORT).show()
    }

    fun jumpApp(view: View?) {
        RouterManager.build("/app/MainActivity")
            .navigation(this)
    }

    fun jumpPersonal(view: View?) {
        RouterManager.build("/personal/PersonalActivity")
            .navigation(this)
    }
}