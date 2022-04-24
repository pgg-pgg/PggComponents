package com.pgg.order.debug

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pgg.order.OrderActivity
import com.pgg.order.R

class OrderDebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.order_activity_debug)
    }

    fun jumpPersonal(view: View?) {
        val intent = Intent(this, OrderActivity::class.java)
        startActivity(intent)
    }
}