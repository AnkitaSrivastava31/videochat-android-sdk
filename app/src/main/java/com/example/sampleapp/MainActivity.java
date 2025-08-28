package com.example.sampleapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.videochat.VideoChatClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.webrtc.SurfaceViewRenderer;

public class MainActivity extends AppCompatActivity {

    private VideoChatClient videoChatClient;
    private static final int PICK_FILE_REQUEST = 101;
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
        btnSwitchCamera.setOnClickListener(v -> videoChatClient.switchCamera());

        // Share Document
        btnShareDoc.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*"); // or "application/pdf" for PDFs only
            startActivityForResult(Intent.createChooser(intent, "Select Document"), PICK_FILE_REQUEST);
        });

        // End Call
        btnEndCall.setOnClickListener(v -> {
            videoChatClient.disconnect();
            finish();
        });

        // Start Call Automatically
        videoChatClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri fileUri = data.getData();
            if (fileUri != null) {
                uploadDocument(fileUri);
            }
        }
    }

    //Upload document to Firebase Storage
    private void uploadDocument(Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference()
                .child("shared_docs/" + System.currentTimeMillis());

        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            Toast.makeText(this, "Document Shared!", Toast.LENGTH_SHORT).show();

                            // Send download URL to peer via your signaling (WebSocket)
                            videoChatClient.sendMessage(
                                    "{\"type\":\"doc\",\"url\":\"" + uri.toString() + "\"}"
                            );
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
