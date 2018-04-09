package cs402.guac;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartScreen extends AppCompatActivity {
    private Button button;
    private Button button3;
    private Button button4;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
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
    }
    public void openauth(){
        Intent intent = new Intent(this,MainActivity.class);
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
    
}
