package cs402.guac

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log

/**
 * Created by mhamdaoui on 2017-10-27.
 */
class HostCardEmulatorService: HostApduService() {
    public var LISTEN: Boolean = false;
    companion object {
        val TAG = "Host Card Emulator"
        val STATUS_SUCCESS = "393605bbAAAAAAAAAAAAAAAAAAAAAAAA"
        val STATUS_FAILED = "6F00"
        val CLA_NOT_SUPPORTED = "6E00"
        val INS_NOT_SUPPORTED = "6D00"
        val AID = "A0000002471001"
        val SELECT_INS = "A4"
        val DEFAULT_CLA = "00"
        val MIN_APDU_LENGTH = 12
    }

    fun onStart() {
        super.onDestroy();
        val pm = getPackageManager();
        val name = ComponentName(this, "android.nfc.cardemulation.HostApduService");

        //PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(name, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);



    }


    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "Deactivated: " + reason)
    }

    override fun processCommandApdu(commandApdu: ByteArray?, extras: Bundle?): ByteArray {
        if( LISTEN == false){
            this.onStart();
            return Utils.hexStringToByteArray(STATUS_FAILED);
        }

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
            return Utils.hexStringToByteArray(STATUS_SUCCESS)
        } else {
            return Utils.hexStringToByteArray(STATUS_FAILED)
        }
    }
}