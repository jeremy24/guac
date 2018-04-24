import java.security.*;

import java.util.*;
import java.io.*;
import java.security.spec.*;
import java.net.*;
import java.math.BigInteger;


public class Verify
{
    Verify(String[] args)
    throws UnsupportedEncodingException, NoSuchAlgorithmException
    , InvalidKeySpecException, InvalidKeyException, SignatureException
    {
        String b64Ssig = args[0];
        String b64Key = args[1];
        String Message = args[2];

        byte[] sig = Base64.getDecoder().decode(b64Ssig.getBytes("UTF-8"));
        byte[] pubk = Base64.getDecoder().decode(b64Key.getBytes("UTF-8"));
        PublicKey pubK = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(pubk));
        Signature verify = Signature.getInstance("SHA256withECDSA");
        verify.initVerify(pubK);
        verify.update(Message.getBytes("UTF-8"));
        boolean isValid = verify.verify(sig);

        System.exit(isValid ? 9 : 6);
    }



    public static void main(String[] args)
    throws UnsupportedEncodingException, NoSuchAlgorithmException
    , InvalidKeySpecException, InvalidKeyException, SignatureException
    {
        if(args.length != 3)
        {
            System.err.println("usage: Verify Base64Sig Base64Key Message");
            return;
        }
        new Verify(args);
    }
}
