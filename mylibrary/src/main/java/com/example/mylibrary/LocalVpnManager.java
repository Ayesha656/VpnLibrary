package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;

import java.util.ArrayList;
import java.util.List;

public class LocalVpnManager {
    private static final int MSG_SET_CALLBACK = 1;

    public static void start(Context context, List<String> imageExtensions, List<String> packageNames, ImageFilterCallback callback) {
        Intent intent = new Intent(context, LocalVpnService.class);
        intent.putStringArrayListExtra("imageExtensions", new ArrayList<>(imageExtensions));
        intent.putStringArrayListExtra("packageNames", new ArrayList<>(packageNames));

        // Create a Messenger to handle the callback
        Messenger messenger = new Messenger(new IncomingHandler(callback));
        intent.putExtra("callbackMessenger", messenger);

        context.startService(intent);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, LocalVpnService.class));
    }

    // Handler to process messages from the service
    private static class IncomingHandler extends Handler {
        private final ImageFilterCallback callback;

        IncomingHandler(ImageFilterCallback callback) {
            this.callback = callback;
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SET_CALLBACK:
                    // Process the callback
                    String imageUrl = msg.getData().getString("imageUrl");
                    boolean isAllowed = callback.onImageRequest(imageUrl);
                    // Send the response back to the service
                    Message response = Message.obtain(null, MSG_SET_CALLBACK, isAllowed ? 1 : 0, 0);
                    try {
                        msg.replyTo.send(response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
