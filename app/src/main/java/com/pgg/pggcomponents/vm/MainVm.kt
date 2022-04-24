package com.pgg.pggcomponents.vm

import android.content.Context
import android.content.Intent
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import com.pgg.arouter_api.manager.RouterManager
import com.pgg.common.databinding.ObservableDelegate
import com.pgg.pggcomponents.BR
import com.pgg.pggcomponents.`ARouter$$Group$$app`
import com.pgg.pggcomponents.`ARouter$$Path$$app`

class MainVm: BaseObservable() {

    @get:Bindable
    var title: String by ObservableDelegate(BR.title, "")

    fun jumpOrder(context: Context) {
        RouterManager.build("/order/OrderActivity")
            .withString("name", "123")
            .withInt("age", 12)
            .navigation(context)
    }

    fun jumpPersonal(context: Context) {
        RouterManager.build("/personal/PersonalActivity")
            .navigation(context)
    }
}