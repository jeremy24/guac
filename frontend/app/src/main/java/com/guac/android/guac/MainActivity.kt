package com.guac.android.guac

import android.content.Intent
import android.nfc.cardemulation.NfcFCardEmulation
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)
        finish()
    }

}
