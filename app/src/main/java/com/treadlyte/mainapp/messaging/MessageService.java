package com.treadlyte.mainapp.messaging;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.ParseUser;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.WritableMessage;

public class MessageService extends Service implements SinchClientListener {

    private static final String APP_KEY = "ce3db807-bf79-40f0-b78c-f817845d978c";
    private static final String APP_SECRET = "Ob8yvoMn3EeFO+/MNeFVtw==";
    private static final String ENVIRONMENT = "sandbox.sinch.com";
    private final MessageServiceInterface serviceInterface = new MessageServiceInterface();
    private SinchClient sinchClient = null;
    private MessageClient messageClient = null;
    private String currentUserId;

    private Intent broadcastIntent = new Intent("com.treadlyte.mainapp.messaging.ListUsersActivity");
    private LocalBroadcastManager broadcaster;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //get the current user id from Parse
        currentUserId = ParseUser.getCurrentUser().getObjectId();
        if (currentUserId != null && !isSinchClientStarted()) {
            startSinchClient(currentUserId);
        }

        //onStartCommand
        broadcaster = LocalBroadcastManager.getInstance(this);

        return super.onStartCommand(intent, flags, startId);
    }
    public void startSinchClient(String username) {
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(username)
                .applicationKey(APP_KEY)
                .applicationSecret(APP_SECRET)
                .environmentHost(ENVIRONMENT)
                .build();
        //this client listener requires that you define
        //a few methods below
        sinchClient.addSinchClientListener(this);
        //messaging is "turned-on", but calling is not
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.checkManifest();
        sinchClient.start();
    }
    private boolean isSinchClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }
    //The next 5 methods are for the sinch client listener
    @Override
    public void onClientFailed(SinchClient client, SinchError error) {
        broadcastIntent.putExtra("success", false);
        broadcaster.sendBroadcast(broadcastIntent);
        sinchClient = null;
    }
    @Override
    public void onClientStarted(SinchClient client) {
        //onClientStarted
        broadcastIntent.putExtra("success", true);
        broadcaster.sendBroadcast(broadcastIntent);

        client.startListeningOnActiveConnection();
        messageClient = client.getMessageClient();
    }
    @Override
    public void onClientStopped(SinchClient client) {
        sinchClient = null;
    }
    @Override
    public void onRegistrationCredentialsRequired(SinchClient client, ClientRegistration clientRegistration) {}
    @Override
    public void onLogMessage(int level, String area, String message) {}
    @Override
    public IBinder onBind(Intent intent) {
        return serviceInterface;
    }
    public void sendMessage(String recipientUserId, String textBody) {
        if (messageClient != null) {
            WritableMessage message = new WritableMessage(recipientUserId, textBody);
            messageClient.send(message);
        }
    }
    public void addMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.addMessageClientListener(listener);
        }
    }
    public void removeMessageClientListener(MessageClientListener listener) {
        if (messageClient != null) {
            messageClient.removeMessageClientListener(listener);
        }
    }
    @Override
    public void onDestroy() {
        sinchClient.stopListeningOnActiveConnection();
        sinchClient.terminate();
    }
    //public interface for ListUsersActivity
    public class MessageServiceInterface extends Binder {
        public void sendMessage(String recipientUserId, String textBody) {
            MessageService.this.sendMessage(recipientUserId, textBody);
        }
        public void addMessageClientListener(MessageClientListener listener) {
            MessageService.this.addMessageClientListener(listener);
        }
        public void removeMessageClientListener(MessageClientListener listener) {
            MessageService.this.removeMessageClientListener(listener);
        }
        public boolean isSinchClientStarted() {
            return MessageService.this.isSinchClientStarted();
        }
    }
}