package com.guac.android.pos

import android.annotation.SuppressLint
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.os.SystemClock.sleep
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import com.guac.android.pos.R.layout.activity_main
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

@RequiresApi(Build.VERSION_CODES.KITKAT)
class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private var nfcAdapter: NfcAdapter? = null
    private var buttonPressed = false
    private var posType = "NONE"
    private var signature: ByteArray = byteArrayOf(0)
    private var sigB64: String = ""
    private var msg: String = ""

    fun setType(type: String) {
        posType = type
    }

    fun hideStuff() {
        var decorView = getWindow().getDecorView();
        var uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setType("DOOR")
        hideStuff()
        var button: Button = findViewById(R.id.button)
        button.setVisibility(View.VISIBLE)
        button.setBackgroundColor(Color.TRANSPARENT)
        button.setOnClickListener(View.OnClickListener {
            buttonPressed = true
            Log.println(4, "button", "pressed")
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        hideStuff()
        return super.onTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null)
    }

    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    @SuppressLint("ResourceType")
    @RequiresApi(Build.VERSION_CODES.O)
    fun verifySig(response: String, username: String, message: String) {
        val urlString = "https://home.piroax.com/volcard/message/validate"
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        Log.println(4, "verifySig", response + "," + username + "," + message)
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Accept", "application/json")
        conn.doOutput = true
        conn.doInput = true
        conn.connect()
        val jsonParam = JSONObject()
        jsonParam.put("username", username)
        // Incorrect User Test if Button is Pressed
        if( buttonPressed ){
            jsonParam.put("message", "badString")
            buttonPressed = false
        }
        else {
            jsonParam.put("message", message)
        }
        jsonParam.put("signature", response)
        val os = DataOutputStream(conn.outputStream)
        os.writeBytes(jsonParam.toString())
        os.flush()
        os.close()
        if (conn.responseCode.toString() == "200") {
            val bgElement: ConstraintLayout = findViewById(R.id.container)
            bgElement.setBackgroundColor(Color.GREEN)
            sleep(2500)
            bgElement.setBackgroundColor(Color.BLACK)

            Log.println(4, "log", "access granted")
        } else if (conn.responseCode.toString() == "400" || conn.responseCode.toString() == "418") {
            val bgElement: ConstraintLayout = findViewById(R.id.container)
            bgElement.setBackgroundColor(Color.RED)
            sleep(2500)
            bgElement.setBackgroundColor(Color.BLACK)
            Log.println(4, "log", "access denied")
        } else {
            Log.println(4, "log", "error verifying")
        }
        conn.disconnect()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()
        var respString: String = "00A4040007A0000002471001"
        msg = respString
        signature = isoDep.transceive(Utils.hexStringToByteArray(respString))
        var sizeOfSig = signature[0].toInt()
        var sigB64 = Base64.getEncoder().encodeToString(signature.copyOfRange(1, sizeOfSig + 1))
        var user = Utils.hexToAscii(Utils.toHex(signature.copyOfRange(sizeOfSig + 1, signature.size)))
        verifySig(sigB64, user, "hello")
        Log.println(4, "ontagdiscover", sigB64)
        isoDep.close()
    }
}
