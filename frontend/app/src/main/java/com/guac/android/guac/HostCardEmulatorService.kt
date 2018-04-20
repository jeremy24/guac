package com.guac.android.guac

import android.nfc.cardemulation.HostApduService
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log

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
        val MIN_APDU_LENGTH = 12
    }

    private var message = "Success"
    var status: Int? = 0
    private var id = "penis"

    public fun incStatus(){
        status = status!! +1
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }

    fun setMessage(msg: String){
        this.message = msg;
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        Log.println(4, "tag", this.status.toString())
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

        if (hexCommandApdu.substring(10, 24) == AID)  {
            if(hexCommandApdu.substring(24,26) == "00"){
                return "Hello".toByteArray()
            }
            if(hexCommandApdu.substring(24,26) == "01"){
                return "benis".toByteArray()
            }

        } else {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }

        return Utils.hexStringToByteArray(STATUS_FAILED)
    }
}