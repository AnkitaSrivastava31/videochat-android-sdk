package com.example.videochat;

import android.content.Context;
import android.util.Log;

public class VideoChatClient {

    private final Context context;
    private final String serverUrl;

    public VideoChatClient(Context context, String serverUrl) {
        this.context = context;
        this.serverUrl = serverUrl;
    }

    public void connect() {
        Log.d("VideoChatClient", "Connecting to server: " + serverUrl);

    }

    public void disconnect() {
        Log.d("VideoChatClient", "Disconnected from server: " + serverUrl);
    }
}
