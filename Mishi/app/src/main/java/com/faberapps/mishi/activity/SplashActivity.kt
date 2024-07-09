package com.faberapps.mishi.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.faberapps.mishi.MainActivity
import com.faberapps.mishi.R

class SplashActivity : AppCompatActivity() {

    private val timeOutSplashActivity: Long = 1500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intentMain = Intent(this, MainActivity::class.java)
            startActivity(intentMain)
            finish()
        }, timeOutSplashActivity)
    }
}