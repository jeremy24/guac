package com.guac.android.guac

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity


@RequiresApi(Build.VERSION_CODES.KITKAT)
class NFCScreen : AppCompatActivity() {
    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfcscreen)

        val abcd = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                finish()
                val newIntent = Intent(context, MainScreen::class.java)
                startActivity(newIntent)
            }
        }
        registerReceiver(abcd, IntentFilter("xyz"));
    }
}