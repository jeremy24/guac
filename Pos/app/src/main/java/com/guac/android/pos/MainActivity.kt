package com.guac.android.pos

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.SystemClock.sleep
import android.support.annotation.RequiresApi
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.guac.android.pos.R.layout.activity_main
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.*
import java.security.spec.X509EncodedKeySpec
import java.util.*

@RequiresApi(Build.VERSION_CODES.KITKAT)
class MainActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {
    private var nfcAdapter: NfcAdapter? = null
    private var posID = 0
    private var transactionID = 0
    private var posType = "NONE"
    private var signature: ByteArray = byteArrayOf(0)
    private var sigB64: String = ""
    private var msg: String = ""

    fun setType(type: String){
        posType = type
    }

    fun hideStuff(){
        var decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        var uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setType("DOOR")
        hideStuff()
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
    fun verifySig(response: String, username: String, message: String){
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
        jsonParam.put("message", message)
        //jsonParam.put("message", message)
        jsonParam.put("signature", response)
        val os = DataOutputStream(conn.outputStream)
        os.writeBytes(jsonParam.toString())
        os.flush()
        os.close()
        Log.i("STATUS", conn.responseCode.toString())
        Log.i("MSG", conn.responseMessage)
//
//        // TEST
//        var pubKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEKU2KLVLZ70xGq8oRmjB7YTDVSY8wNdeMJ4xcJw5cmQJSJRrMS9tsZkdmsk88H+a/9q1ISvMA99RcIxIUWhKmXQ=="
//        var sig: Signature = Signature.getInstance("SHA256withECDSA")
//        var pKeybyte: ByteArray = Base64.getDecoder().decode(pubKey)
//        var x: X509EncodedKeySpec = X509EncodedKeySpec(pKeybyte)
//        var pKey: PublicKey = KeyFactory.getInstance("EC").generatePublic(x)
//        sig.initVerify(pKey)
//        sig.update(message.toByteArray())
        //sig.update(Utils.hexStringToByteArray(message))
//        Log.println(4, "CHECK", sig.verify(response.toByteArray()).toString())
        if( conn.responseCode.toString() == "200"){
            val bgElement: ConstraintLayout = findViewById(R.id.container)
            bgElement.setBackgroundColor(Color.GREEN)
            sleep(2500)
            bgElement.setBackgroundColor(Color.BLACK)

            Log.println(4,"log", "access granted")
        }
        else if( conn.responseCode.toString() == "400" || conn.responseCode.toString() == "418"){
            val bgElement: ConstraintLayout = findViewById(R.id.container)
            bgElement.setBackgroundColor(Color.RED)
            sleep(2500)
            bgElement.setBackgroundColor(Color.BLACK)
            Log.println(4, "log", "access denied")
        }
        else{
            Log.println(4, "log", "error verifying")
        }
        conn.disconnect()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()
        var respString = ""
        if(transactionID == 0){
            respString = "00A4040007A0000002471001" + "00"
            msg = respString
            signature = isoDep.transceive(Utils.hexStringToByteArray(respString))
            var sizeOfSig = signature[0].toInt()
            var sigB64 = Base64.getEncoder().encodeToString(signature.copyOfRange(1,sizeOfSig+1))
            var user = Utils.hexToAscii(Utils.toHex(signature.copyOfRange(sizeOfSig+1, signature.size)))
            verifySig(sigB64, user, "hello")

            //sigB64 = Base64.getEncoder().encodeToString(signature)
            Log.println(4, "ontagdiscover", sigB64)



            //transactionID += 1
        }
        else{
            var replyString = "00A4040007A0000002471001" + "11"
            val name: ByteArray = isoDep.transceive(Utils.hexStringToByteArray(replyString))
            Log.println(4, "log", sigB64 + "," + Utils.hexToAscii(Utils.toHex(name)) + "," + msg)

            var by: ByteArray = Base64.getDecoder().decode(sigB64)


            verifySig(sigB64, Utils.hexToAscii(Utils.toHex(name)), "hello")
            transactionID = 0
        }
        isoDep.close()
    }
}
