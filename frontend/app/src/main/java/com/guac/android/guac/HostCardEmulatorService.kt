package com.guac.android.guac

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.util.*

@RequiresApi(Build.VERSION_CODES.KITKAT)
/**
 * Created by mhamdaoui on 2017-10-27.
 */
class HostCardEmulatorService: HostApduService() {

    companion object {
        val TAG = "Host Card Emulator"
        val STATUS_SUCCESS = "Hello"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID = "A0000002471001"
        val SELECT_INS = "A4"
        val DEFAULT_CLA = "00"
        val MIN_APDU_LENGTH = 0
    }

    private var message = "Success"
    var status: Int? = 0
    private lateinit var username: String
    private var nfc: NfcAdapter? = null


    public fun incStatus(){
        status = status!! +1
    }

    override fun onCreate() {
        super.onCreate()
        /*
        val broadCastReceiver = object : BroadcastReceiver() {
            override fun onReceive(contxt: Context?, intent: Intent?) {

                if (intent != null) {
                    when (intent.action) {
                        intent.getStringExtra("username") -> username
                    }
                }
            }
        }
        var intentFilter: IntentFilter = IntentFilter()
        intentFilter.addAction("getting_data")
        registerReceiver(broadCastReceiver, intentFilter)
        */
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }

    fun setMessage(msg: String){
        this.message = msg;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {

        if (commandApdu == null) {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        val hexCommandApdu = Utils.toHex(commandApdu)
        if (hexCommandApdu.length < MIN_APDU_LENGTH) {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }


        if (hexCommandApdu.substring(0, 2) != DEFAULT_CLA) {
            return Utils.hexStringToByteArray(CLA_NOT_SUPPORTED)
        }

        if (hexCommandApdu.substring(2, 4) != SELECT_INS) {
            return Utils.hexStringToByteArray(INS_NOT_SUPPORTED)
        }

        Log.println(4, "log", hexCommandApdu.substring(11,13))
        if (hexCommandApdu.substring(10,24) == AID)  {
            if(hexCommandApdu.substring(24,26) == "00") {
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
                newArray = newArray.plus(signed.size.toByte()).plus(signed).plus(Utils.getUserID().toByteArray())
                /*
                var sig2: Signature = Signature.getInstance("SHA256withECDSA")
                val publicKeyBytes = Base64.getEncoder().encode(publicKey.encoded)
                val pubKey = String(publicKeyBytes)
                Log.println(4, "tag", pubKey)
                sig2.initVerify(publicKey)
                sig2.update(Utils.hexStringToByteArray(sign))
                var boo: Boolean = sig2.verify(signed)
                Log.println(4, "tag", boo.toString() + " " + userID)*/
                return newArray//signed
            }
            if(hexCommandApdu.substring(24,26) == "11"){
                return Utils.getUserID().toByteArray()
            }

        } else {
            return "fail".toByteArray()//Utils.hexStringToByteArray(STATUS_FAILED)
        }

        return "fail".toByteArray()//Utils.hexStringToByteArray(STATUS_FAILED)
    }
}