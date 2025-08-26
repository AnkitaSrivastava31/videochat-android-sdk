package com.example.sampleapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videochat.VideoChatClient;  // from sdk

public class MainActivity extends AppCompatActivity {

    private VideoChatClient videoChatClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnConnect = findViewById(R.id.btnConnect);

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoChatClient = new VideoChatClient(MainActivity.this, "wss://yourserver.com");
                videoChatClient.connect();
            }
        });
    }
}
