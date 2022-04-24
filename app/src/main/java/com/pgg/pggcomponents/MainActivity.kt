package com.pgg.pggcomponents

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.pgg.arouter_annotation.ARouter
import com.pgg.arouter_annotation.Parameter
import com.pgg.arouter_api.manager.ParameterManager
import com.pgg.common.call.order.drawable.OrderDrawable
import com.pgg.common.call.order.user.IUser
import com.pgg.pggcomponents.databinding.ActivityMainBinding
import com.pgg.pggcomponents.vm.MainVm

@ARouter(path = "/app/MainActivity")
class MainActivity : AppCompatActivity() {
    private val vm = MainVm()

    @Parameter(paramsName = "/order/getDrawable")
    var orderDrawable: OrderDrawable? = null

    @Parameter(paramsName = "/order/getUserInfo")
    var iUser: IUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        ParameterManager.loadParameter(this)
        vm.title = "组件化首页"
        binding.vm = vm
    }
}