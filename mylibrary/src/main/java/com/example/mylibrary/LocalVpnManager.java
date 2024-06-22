package com.example.mylibrary;

import android.content.Context;
import android.content.Intent;

import java.util.List;

public class LocalVpnManager {
    private static LocalVpnService vpnService;

    public static void start(Context context, List<String> imageExtensions, List<String> packageNames, ImageFilterCallback callback) {
        Intent intent = new Intent(context, LocalVpnService.class);
        context.startService(intent);
        vpnService = new LocalVpnService();
        vpnService.setImageFilterCallback(callback);
        vpnService.setImageExtensions(imageExtensions);
        vpnService.setPackageNames(packageNames);
    }

    public static void stop(Context context) {
        if (vpnService != null) {
            context.stopService(new Intent(context, LocalVpnService.class));
        }
    }
}