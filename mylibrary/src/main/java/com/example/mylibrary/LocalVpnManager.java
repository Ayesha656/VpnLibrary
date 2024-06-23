package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class LocalVpnManager {
    public static void start(Context context, List<String> imageExtensions, List<String> packageNames, ImageFilterCallback callback) {
        Intent intent = new Intent(context, LocalVpnService.class);
        intent.putStringArrayListExtra("imageExtensions", new ArrayList<>(imageExtensions));
        intent.putStringArrayListExtra("packageNames", new ArrayList<>(packageNames));
        LocalVpnService.setImageFilterCallback(callback);
        context.startService(intent);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, LocalVpnService.class));
    }
}
