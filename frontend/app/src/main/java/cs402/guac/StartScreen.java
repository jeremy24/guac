package cs402.guac;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartScreen extends AppCompatActivity {
    private Button button;
    private Button button3;
    private Button button4;
    private Button button6;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, "android.nfc.cardemulation.HostApduService"), PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        //stopService(new Intent(this, HostCardEmulatorService.class));
        /*
        button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openauth();
            }
        });
        button3 = (Button) findViewById(R.id.button3);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opennfc();
            }
        });
        button4 = (Button) findViewById(R.id.button4);
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openhome();
            }
        });
        button6 = (Button) findViewById(R.id.button6);
        button6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openhce();
            }
        });
//        Intent serviceIntent = new Intent(getApplicationContext(), MyIntentService.class);
//        startService(serviceIntent);
//        Intent serviceIntent2 = new Intent(getApplicationContext(), HostCardEmulatorService.class);
//        startService(serviceIntent2);
*/
    }
    public void openauth(){
        Intent intent = new Intent(this,FingerprintActivity.class);
        this.finish();
        startActivity(intent);
    }

    public void opennfc(){
        Intent intent = new Intent(this,NFC_Activity.class);
        this.finish();
        startActivity(intent);
    }

    public void openhome(){
        Intent intent = new Intent(this,StartScreen.class);
        this.finish();
        startActivity(intent);
    }

    public void openhce(){
        Intent intent = new Intent(this,HCE.class);
        this.finish();
        startActivity(intent);
    }
    
}
