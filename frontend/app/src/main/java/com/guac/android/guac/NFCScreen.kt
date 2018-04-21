package com.guac.android.guac


import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Log
//import com.guac.android.guac.R.id.textView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_nfcscreen.*

@RequiresApi(Build.VERSION_CODES.KITKAT)
class NFCScreen : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    public var status = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfcscreen)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        //val intent = Intent(this, HostCardEmulatorService::class.java)
        //startService(intent)
        //nfcAdapter?.disableReaderMode(this);
        //nfcAdapter = null;
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

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()

        var respString = "00A4040007A0000002471001"

        if(status > 1){
            status = 0
        }

        if(status == 0){
            respString += "00"
        }
        else if(status == 1){
            respString += "01"
        }
        status+=1
        val response = isoDep.transceive(Utils.hexStringToByteArray(
                respString))
        var tmp = response


        runOnUiThread {textView3.append("Card Response: "
                + Utils.hexToAscii(Utils.toHex(tmp)) + " " + status.toString() + " " + respString)}
        isoDep.close()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)
    }
}