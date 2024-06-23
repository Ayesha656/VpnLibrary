package com.example.vpnlibrary;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.mylibrary.ImageFilterCallback;
import com.example.mylibrary.ImageFilterResultCallback;
import com.example.mylibrary.LocalVpnManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);

        LocalVpnManager.start(this, Arrays.asList(".jpg", ".png"), Arrays.asList("com.example.app"), new ImageFilterCallback() {
            @Override
            public void onImageUrlIntercepted(String imageUrl, ImageFilterResultCallback resultCallback) {
                validateImageUrl(imageUrl, resultCallback);
            }

            @Override
            public boolean onImageRequest(String imageUrl) {
                return true;
            }
        });
    }

    private void validateImageUrl(String imageUrl, ImageFilterResultCallback resultCallback) {
        String url = "https://your-backend-server.com/validate"; // Replace with your backend server URL

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean allowed = jsonResponse.getBoolean("allowed");
                            String newUrl = jsonResponse.optString("newUrl", imageUrl);
                            resultCallback.onResult(allowed, newUrl);
                        } catch (JSONException e) {
                            Log.e("MainActivity", "JSON parsing error", e);
                            resultCallback.onResult(false, null);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("MainActivity", "Error validating image URL", error);
                        resultCallback.onResult(false, null);
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("imageUrl", imageUrl);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalVpnManager.stop(this);
    }
}
