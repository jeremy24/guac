package com.guac.android.pos

import android.util.Log
import java.security.KeyStore

/**
 * Created by mhamdaoui on 2017-10-27.
 */
class Utils {

    companion object {
        private val HEX_CHARS = "0123456789ABCDEF"
        fun hexStringToByteArray(data: String) : ByteArray {

            val result = ByteArray(data.length / 2)

            for (i in 0 until data.length step 2) {
                val firstIndex = HEX_CHARS.indexOf(data[i]);
                val secondIndex = HEX_CHARS.indexOf(data[i + 1]);

                val octet = firstIndex.shl(4).or(secondIndex)
                result.set(i.shr(1), octet.toByte())
            }

            return result
        }

        private val HEX_CHARS_ARRAY = "0123456789ABCDEF".toCharArray()
        fun toHex(byteArray: ByteArray) : String {
            val result = StringBuffer()

            byteArray.forEach {
                val octet = it.toInt()
                val firstIndex = (octet and 0xF0).ushr(4)
                val secondIndex = octet and 0x0F
                result.append(HEX_CHARS_ARRAY[firstIndex])
                result.append(HEX_CHARS_ARRAY[secondIndex])
            }

            return result.toString()
        }

        fun hexToAscii(hexStr: String): String {
            val output = StringBuilder("")

            var i = 0
            while (i < hexStr.length) {
                val str = hexStr.substring(i, i + 2)
                output.append(Integer.parseInt(str, 16).toChar())
                i += 2
            }

            return output.toString()
        }
        fun getUserID(): String{
            try {
                val keyStore = KeyStore.getInstance("AndroidKeyStore")
                keyStore.load(null)
                val e = keyStore.aliases()
                var tmp = e.nextElement()
                while (e.hasMoreElements()) {
                    if (tmp === "yourKey") {
                        e.nextElement()
                        continue
                    }
                    tmp = e.nextElement()
                }
                return tmp
            }
            catch(e: Exception){
                var s: String = e.printStackTrace().toString()
                Log.println(4, "penis", s)
            }
            return "FAILURE"
        }
    }
}