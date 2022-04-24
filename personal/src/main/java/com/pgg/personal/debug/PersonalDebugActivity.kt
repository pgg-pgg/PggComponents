package com.pgg.personal.debug

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.pgg.personal.PersonalActivity
import com.pgg.personal.R

class PersonalDebugActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.personal_activity_debug)
    }

    fun jump(view: View?) {
        val intent = Intent(this, PersonalActivity::class.java)
        startActivity(intent)
    }
}