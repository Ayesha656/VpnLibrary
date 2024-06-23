package com.example.mylibrary;

import android.content.Intent;
import android.net.VpnService;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;

public class LocalVpnService extends VpnService {
    private static final String TAG = "LocalVpnService";
    private ParcelFileDescriptor vpnInterface;
    private static ImageFilterCallback imageFilterCallback;
    private List<String> imageExtensions;
    private List<String> packageNames;

    public static void setImageFilterCallback(ImageFilterCallback callback) {
        imageFilterCallback = callback;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        imageExtensions = intent.getStringArrayListExtra("imageExtensions");
        packageNames = intent.getStringArrayListExtra("packageNames");

        try {
            vpnInterface = establishVpn();
        } catch (IOException e) {
            Log.e(TAG, "Failed to establish VPN", e);
            stopSelf();
            return START_NOT_STICKY;
        }

        new Thread(this::interceptAndFilterTraffic).start();

        return START_STICKY;
    }

    private void interceptAndFilterTraffic() {
        try {
            DatagramChannel tunnel = DatagramChannel.open();
            tunnel.connect(new InetSocketAddress("127.0.0.1", vpnInterface.getFd()));
            ByteBuffer buffer = ByteBuffer.allocate(32767);

            while (true) {
                buffer.clear();
                SocketAddress from = tunnel.receive(buffer);
                buffer.flip();
                if (from == null) {
                    Thread.sleep(10);
                    continue;
                }

                // Process HTTP request
                String data = new String(buffer.array()).trim();
                if (data.startsWith("GET") && data.contains("Host: ")) {
                    String[] lines = data.split("\r\n");
                    String host = null;
                    for (String line : lines) {
                        if (line.startsWith("Host: ")) {
                            host = line.substring(6).trim();
                            break;
                        }
                    }

                    if (host != null) {
                        // Check if this request is from an allowed package
                        String packageName = getPackageName(from);
                        if (packageNames.isEmpty() || packageNames.contains(packageName)) {
                            // Check if this request is for an image
                            String imageUrl = getImageUrlFromRequest(data);
                            if (imageUrl != null && isImageUrlAllowed(imageUrl)) {
                                // Allow the request
                                tunnel.write(ByteBuffer.wrap(data.getBytes()));
                            } else {
                                // Block the request or modify URL
                                String blockedResponse = "HTTP/1.1 403 Forbidden\r\n\r\n";
                                tunnel.write(ByteBuffer.wrap(blockedResponse.getBytes()));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in interceptAndFilterTraffic", e);
        }
    }

    private String getPackageName(SocketAddress from) {
        // Implement logic to get package name based on source address or port if needed
        // For simplicity, returning a dummy package name
        return "com.example.app";
    }

    private String getImageUrlFromRequest(String request) {
        // Implement logic to extract image URL from HTTP request
        // For simplicity, assume it extracts from a typical GET request
        String[] lines = request.split("\r\n");
        for (String line : lines) {
            if (line.startsWith("GET ")) {
                String[] parts = line.split(" ");
                if (parts.length > 1) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }

    private boolean isImageUrlAllowed(String imageUrl) {
        // Implement logic to check if the image URL is allowed
        // For simplicity, allow all image URLs for demonstration
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (vpnInterface != null) {
                vpnInterface.close();
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to close VPN interface", e);
        }
    }

    private ParcelFileDescriptor establishVpn() throws IOException {
        Builder builder = new Builder();
        builder.addAddress("10.0.0.2", 24);
        builder.addRoute("0.0.0.0", 0);
        builder.setSession("LocalVpnService");
        return builder.establish();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
