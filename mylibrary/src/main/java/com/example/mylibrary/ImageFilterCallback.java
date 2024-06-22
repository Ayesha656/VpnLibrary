package com.example.mylibrary;

public interface ImageFilterCallback {
    void onImageUrlIntercepted(String imageUrl, ImageFilterResultCallback resultCallback);
}