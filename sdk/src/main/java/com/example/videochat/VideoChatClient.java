package com.example.videochat;

import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraVideoCapturer;
import org.webrtc.DataChannel;
import org.webrtc.DefaultVideoDecoderFactory;
import org.webrtc.DefaultVideoEncoderFactory;
import org.webrtc.EglBase;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;
import org.webrtc.SurfaceTextureHelper;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoSource;
import org.webrtc.VideoTrack;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class VideoChatClient {

    private static final String TAG = "VideoChatClient";

    private final Context context;
    private final String signalingServerUrl;

    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;

    private EglBase rootEglBase;
    private SurfaceViewRenderer localView, remoteView;

    private VideoTrack localVideoTrack;
    private AudioTrack localAudioTrack;
    private CameraVideoCapturer cameraVideoCapturer;

    private WebSocket webSocket;

    public VideoChatClient(Context context, String signalingServerUrl) {
        this.context = context;
        this.signalingServerUrl = signalingServerUrl;
    }

    public void init(SurfaceViewRenderer localView, SurfaceViewRenderer remoteView) {
        this.localView = localView;
        this.remoteView = remoteView;

        rootEglBase = EglBase.create();

        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions()
        );

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder()
                .setVideoEncoderFactory(new DefaultVideoEncoderFactory(rootEglBase.getEglBaseContext(), true, true))
                .setVideoDecoderFactory(new DefaultVideoDecoderFactory(rootEglBase.getEglBaseContext()))
                .setOptions(options)
                .createPeerConnectionFactory();

        localView.init(rootEglBase.getEglBaseContext(), null);
        remoteView.init(rootEglBase.getEglBaseContext(), null);
        localView.setZOrderMediaOverlay(true);

        startLocalMedia();
        createPeerConnection();
    }

    private void startLocalMedia() {
        cameraVideoCapturer = createCameraCapturer(new Camera2Enumerator(context));
        VideoSource videoSource = peerConnectionFactory.createVideoSource(cameraVideoCapturer.isScreencast());

        SurfaceTextureHelper surfaceTextureHelper =
                SurfaceTextureHelper.create("CaptureThread", rootEglBase.getEglBaseContext());

        cameraVideoCapturer.initialize(
                surfaceTextureHelper,
                context,
                videoSource.getCapturerObserver()
        );
        cameraVideoCapturer.startCapture(640, 480, 30);

        localVideoTrack = peerConnectionFactory.createVideoTrack("LOCAL_VIDEO", videoSource);
        localVideoTrack.addSink(localView);

        AudioSource audioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack("LOCAL_AUDIO", audioSource);
    }

    private CameraVideoCapturer createCameraCapturer(Camera2Enumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                CameraVideoCapturer capturer = enumerator.createCapturer(deviceName, null);
                if (capturer != null) return capturer;
            }
        }
        return null;
    }

    private void createPeerConnection() {
        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) { }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) { }

            @Override
            public void onIceConnectionReceivingChange(boolean b) { }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) { }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.d(TAG, "New ICE Candidate: " + iceCandidate.sdp);
                if (webSocket != null) {
                    webSocket.send("{\"type\":\"candidate\",\"sdpMid\":\"" + iceCandidate.sdpMid +
                            "\",\"sdpMLineIndex\":" + iceCandidate.sdpMLineIndex +
                            ",\"candidate\":\"" + iceCandidate.sdp + "\"}");
                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) { }

            @Override
            public void onAddStream(MediaStream mediaStream) {
                if (mediaStream.videoTracks.size() > 0) {
                    mediaStream.videoTracks.get(0).addSink(remoteView);
                }
            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) { }

            @Override
            public void onDataChannel(DataChannel dataChannel) { }

            @Override
            public void onRenegotiationNeeded() { }

            @Override
            public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) { }
        });

        MediaStream mediaStream = peerConnectionFactory.createLocalMediaStream("LOCAL_STREAM");
        mediaStream.addTrack(localAudioTrack);
        mediaStream.addTrack(localVideoTrack);
        peerConnection.addStream(mediaStream);
    }

    // ------------------- NEW CONNECT METHOD -------------------
    public void connect() {
        Log.d(TAG, "Connecting to signaling server: " + signalingServerUrl);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(signalingServerUrl).build();

        webSocket = client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                Log.d(TAG, "WebSocket connected");

                // Create SDP Offer
                MediaConstraints constraints = new MediaConstraints();
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
                constraints.mandatory.add(new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));

                peerConnection.createOffer(new SimpleSdpObserver() {
                    @Override
                    public void onCreateSuccess(SessionDescription sessionDescription) {
                        peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);

                        // Send SDP offer to server
                        webSocket.send("{\"type\":\"offer\",\"sdp\":\"" +
                                sessionDescription.description.replace("\n", "\\n") + "\"}");
                    }
                }, constraints);
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                Log.d(TAG, "Received message: " + text);
                try {
                    JSONObject json = new JSONObject(text);
                    String type = json.getString("type");

                    switch (type) {
                        case "answer":
                            // Remote SDP Answer
                            String sdp = json.getString("sdp");
                            SessionDescription answer = new SessionDescription(
                                    SessionDescription.Type.ANSWER,
                                    sdp
                            );
                            peerConnection.setRemoteDescription(new SimpleSdpObserver(), answer);
                            break;

                        case "candidate":
                            // ICE Candidate from remote
                            IceCandidate candidate = new IceCandidate(
                                    json.getString("sdpMid"),
                                    json.getInt("sdpMLineIndex"),
                                    json.getString("candidate")
                            );
                            peerConnection.addIceCandidate(candidate);
                            break;

                        default:
                            Log.w(TAG, "Unknown message type: " + type);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        });
    }

    public void disconnect() {
        if (peerConnection != null) {
            peerConnection.close();
        }
        if (cameraVideoCapturer != null) {
            try {
                cameraVideoCapturer.stopCapture();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cameraVideoCapturer.dispose();
        }
        if (localView != null) localView.release();
        if (remoteView != null) remoteView.release();
    }

    public void setAudioEnabled(boolean enabled) {
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(enabled);
        }
    }

    public void setVideoEnabled(boolean enabled) {
        if (localVideoTrack != null) {
            localVideoTrack.setEnabled(enabled);
        }
    }

    public void switchCamera() {
        if (cameraVideoCapturer != null) {
            cameraVideoCapturer.switchCamera(null);
        }
    }
}
