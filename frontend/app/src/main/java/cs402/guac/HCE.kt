package cs402.guac

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button


import kotlinx.android.synthetic.main.activity_hce.*
import kotlinx.android.synthetic.main.content_hce.*

class HCE : AppCompatActivity() , NfcAdapter.ReaderCallback{
    private var nfcAdapter: NfcAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hce)
        setSupportActionBar(toolbar)
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        var tmp = findViewById<View>(R.id.button2) as Button
        tmp.setOnClickListener(View.OnClickListener { openhome() } )
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }
    public override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null)
    }
    public override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }
    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        isoDep.connect()
        val response = isoDep.transceive(Utils.hexStringToByteArray(
                "00A4040007A0000002471001"))
        runOnUiThread { textView4.append("\nCard Response: "
                + Utils.toHex(response)) }
        isoDep.close()
    }

    fun openhome(){
        val intent = Intent(this, StartScreen::class.java)
        this.finish()
        startActivity(intent)
    }

}
