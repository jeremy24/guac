package com.guac.android.pos

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.Log
import org.json.JSONObject
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.*
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

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        setType("DOOR")
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
        jsonParam.put("signature", response)
        val os = DataOutputStream(conn.outputStream)
        os.writeBytes(jsonParam.toString())
        os.flush()
        os.close()
        Log.i("STATUS", conn.responseCode.toString())
        Log.i("MSG", conn.responseMessage)
        if( conn.responseCode.toString() == "200"){
            Log.println(4,"log", "access granted")
        }
        else if( conn.responseCode.toString() == "400"){
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
            sigB64 = Base64.getEncoder().encodeToString(signature)
            Log.println(4, "ontagdiscover", sigB64)
            transactionID += 1
        }
        else{
            var replyString = "00A4040007A0000002471001" + "11"
            val name: ByteArray = isoDep.transceive(Utils.hexStringToByteArray(replyString))
            Log.println(4, "log", sigB64 + "," + Utils.hexToAscii(Utils.toHex(name)) + "," + msg)

            var by: ByteArray = Base64.getDecoder().decode(sigB64)


            verifySig(sigB64, Utils.hexToAscii(Utils.toHex(name)), msg)
            transactionID = 0
        }
        isoDep.close()
    }
}
