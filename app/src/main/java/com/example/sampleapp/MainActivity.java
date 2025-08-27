package com.example.sampleapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.videochat.VideoChatClient;

import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {

    private VideoChatClient videoChatClient;
    private SurfaceViewRenderer localView, remoteView;
    private Button btnMute, btnCamera, btnSwitchCamera, btnShareDoc, btnEndCall;

    private boolean isMuted = false;
    private boolean isCameraOn = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Components
        localView = findViewById(R.id.localView);
        remoteView = findViewById(R.id.remoteView);
        btnMute = findViewById(R.id.btnMute);
        btnCamera = findViewById(R.id.btnCamera);
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnShareDoc = findViewById(R.id.btnShareDoc);
        btnEndCall = findViewById(R.id.btnEndCall);

        // Init VideoChatClient
        videoChatClient = new VideoChatClient(this, "wss://yourserver.com");
        videoChatClient.init(localView, remoteView);

        // Mute / Unmute
        btnMute.setOnClickListener(v -> {
            isMuted = !isMuted;
            videoChatClient.setAudioEnabled(!isMuted);
            btnMute.setText(isMuted ? "Unmute" : "Mute");
        });

        // Toggle Camera
        btnCamera.setOnClickListener(v -> {
            isCameraOn = !isCameraOn;
            videoChatClient.setVideoEnabled(isCameraOn);
            btnCamera.setText(isCameraOn ? "Camera Off" : "Camera On");
        });

        // Switch Camera
        btnSwitchCamera.setOnClickListener(v -> {
            videoChatClient.switchCamera();
        });

        // Share Document (Placeholder)
        btnShareDoc.setOnClickListener(v -> {
            Toast.makeText(this, "Document Sharing feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        // End Call
        btnEndCall.setOnClickListener(v -> {
            videoChatClient.disconnect();
            finish();
        });

        // Start Call Automatically
        videoChatClient.connect();
    }
}
