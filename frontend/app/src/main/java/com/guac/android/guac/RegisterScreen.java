package com.guac.android.guac;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RegisterScreen extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)
    public KeyPair keyGen(String username) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore");
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        username,
                        KeyProperties.PURPOSE_SIGN)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .setDigests(KeyProperties.DIGEST_SHA256,
                                KeyProperties.DIGEST_SHA384,
                                KeyProperties.DIGEST_SHA512)
                        // Only permit the private key to be used if the user authenticated
                        // within the last five minutes.
                        .build());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(keyPair.getPrivate());
        return keyPair;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);
        waitForInput();
    }

    private void waitForInput(){
        final EditText studentID = (EditText) findViewById(R.id.phone);
        final EditText pass = (EditText) findViewById(R.id.password);
        final Button submit = (Button) findViewById(R.id.button2);
        submit.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                if (studentID.getText().length() == 0 || (pass.getText().toString().length() == 0)) {
                    return;
                } else {
                    Log.println(4, "tagged", studentID.getText() + " " + pass.getText());
                    PackageInfo packageInfo = null;
                    try {

                        keyGen(studentID.getText().toString());
                        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                        keyStore.load(null);
                        PrivateKey privateKey = (PrivateKey) keyStore.getKey(studentID.getText().toString(), null);
                        PublicKey publicKey = keyStore.getCertificate(studentID.getText().toString()).getPublicKey();
                        Log.println(4, "log", String.valueOf(keyStore.size()));
                        /*
                        String s = "hello";
                        Signature sig = Signature.getInstance("SHA256withECDSA");
                        sig.initSign(privateKey);
                        sig.update(s.getBytes());
                        byte[] signed = sig.sign();
                        Signature sig2 = Signature.getInstance("SHA256withECDSA");
                        sig2.initVerify(publicKey);
                        sig2.update(s.getBytes());
                        Boolean boo = sig2.verify(signed);
                        Log.println(4, "boooo", boo.toString());
                        */
                        byte[] publicKeyBytes = Base64.getEncoder().encode(publicKey.getEncoded());
                        String pubKey = new String(publicKeyBytes);
                        postUser(studentID.getText().toString(), pass.getText().toString(), pubKey);
                    }catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (KeyStoreException e) {
                        e.printStackTrace();
                    } catch (CertificateException e) {
                        e.printStackTrace();
                    } catch (UnrecoverableKeyException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (NoSuchProviderException e) {
                        e.printStackTrace();
                    } catch (InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    }
                    finish();
                    startActivity(new Intent(RegisterScreen.this, MainScreen.class));
                }
            }
        });
    }
    private void postUser(final String id, final String pass, final String pubKey) throws IOException, JSONException {
        String urlString = "https://home.piroax.com/volcard/user/add";
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Accept","application/json");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.connect();
        JSONObject jsonParam = new JSONObject();
        jsonParam.put("username", id);
        jsonParam.put("password", pass);
        jsonParam.put("public_key", pubKey);
        Log.println(4, "log", id + " " + pass + " " + pubKey);
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        os.writeBytes(jsonParam.toString());
        os.flush();
        os.close();
        Log.i("STATUS", String.valueOf(conn.getResponseCode()));
        Log.i("MSG" , conn.getResponseMessage());
        conn.disconnect();
    }

}
