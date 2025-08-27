package com.example.videochat;

import android.util.Log;

import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

public class SimpleSdpObserver implements SdpObserver {

    private static final String TAG = "SimpleSdpObserver";

    @Override
    public void onCreateSuccess(SessionDescription sessionDescription) {
        Log.d(TAG, "onCreateSuccess: " + sessionDescription.description);
    }

    @Override
    public void onSetSuccess() {
        Log.d(TAG, "onSetSuccess");
    }

    @Override
    public void onCreateFailure(String error) {
        Log.e(TAG, "onCreateFailure: " + error);
    }

    @Override
    public void onSetFailure(String error) {
        Log.e(TAG, "onSetFailure: " + error);
    }
}
