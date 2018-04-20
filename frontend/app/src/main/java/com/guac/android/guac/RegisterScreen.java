package com.guac.android.guac;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class RegisterScreen extends AppCompatActivity {

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
            public void onClick(View v) {
                if (studentID.getText().length() == 0 || (pass.getText().toString().length() == 0)) {
                    return;
                } else {
                    Log.println(4, "tagged", studentID.getText() + " " + pass.getText());
                    try {
                        postUser(studentID.getText().toString(), pass.getText().toString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    startActivity(new Intent(RegisterScreen.this, MainScreen.class));
                }
            }
        });
    }
    private void postUser(final String id, final String pass) throws IOException, JSONException {
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
        jsonParam.put("public_key", "pubKey");
        DataOutputStream os = new DataOutputStream(conn.getOutputStream());
        os.writeBytes(jsonParam.toString());
        os.flush();
        os.close();
        Log.i("STATUS", String.valueOf(conn.getResponseCode()));
        Log.i("MSG" , conn.getResponseMessage());
        conn.disconnect();
    }

}
