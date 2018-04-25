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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_nfcscreen.*
import java.security.*
//import java.security.KeyStore
//import java.security.PrivateKey
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.SecretKey

@RequiresApi(Build.VERSION_CODES.KITKAT)
class NFCScreen : AppCompatActivity() {//, NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    public var status = 0

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainScreen::class.java)
        startActivity(intent)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nfcscreen)
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        val userID: String = Utils.getUserID()
        keyStore.load(null)
        val privateKey = keyStore.getKey(userID, null) as PrivateKey
        val publicKey = keyStore.getCertificate(userID).publicKey
        var sign: String
        sign = "hello"//"00A4040007A0000002471001"

        var sig: Signature = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey)
        sig.update(sign.toByteArray())//Utils.hexStringToByteArray(sign))//Utils.hexStringToByteArray(hexCommandApdu.substring(0,24)))//.toByte())
        var signed: ByteArray = sig.sign()
        var userBytes = Utils.getUserID().toByteArray()
        var newArray: ByteArray = ByteArray(0)
        newArray.plus(signed.size.toByte()).plus(signed).plus(Utils.getUserID().toByteArray())
        /*
        //nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        var hexCommandApdu: String = "hello"
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        val userID: String = Utils.getUserID()
        keyStore.load(null)
        val privateKey: Key = keyStore.getKey(userID, null) as PrivateKey
        val publicKey = keyStore.getCertificate(userID).publicKey

        var sig: Signature = Signature.getInstance("SHA256withECDSA")
        sig.initSign(privateKey as PrivateKey)
        sig.update(hexCommandApdu.toByteArray())//Utils.hexStringToByteArray(hexCommandApdu))//.toByte())
        var signed: ByteArray = sig.sign()


        var sig2: Signature = Signature.getInstance("SHA256withECDSA")
        val publicKeyBytes = Base64.getEncoder().encode(publicKey.encoded)
        val pubKey = String(publicKeyBytes)
        Log.println(4, "tag", pubKey)

        val pubKeyDecode = Base64.getDecoder().decode(pubKey)
        var x: X509EncodedKeySpec = X509EncodedKeySpec(pubKeyDecode)
        var pKey: PublicKey = KeyFactory.getInstance("EC").generatePublic(x)
        sig2.initVerify(pKey)
        sig2.update("hello".toByteArray())
        Log.println(4, "test", pubKey + " " + sig2.verify(signed).toString())



        //val intent = Intent(this, HostCardEmulatorService::class.java)
        //startService(intent)
        //nfcAdapter?.disableReaderMode(this);
        //nfcAdapter = null;
    }
    /*
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
*/
    */

    }
}