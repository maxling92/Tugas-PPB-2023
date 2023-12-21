package com.example.fruitscanner2;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

public class CameraActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Panggil startCamera() di sini jika Anda ingin mulai kamera saat aktivitas dibuat
        startCamera();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                if (cameraProvider == null) {
                    Log.e(TAG, "Error getting camera provider.");
                    return;
                }

                // Bind the preview use case
                Preview preview = new Preview.Builder().build();

                // Set up the camera selector to choose the back camera by default
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Attach the preview use case to the previewView
                PreviewView previewView = findViewById(R.id.previewView);
                if (previewView != null && previewView.getSurfaceProvider() != null) {
                    // Update the preview use case when the previewView is ready
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                } else {
                    Log.e(TAG, "PreviewView or SurfaceProvider is null. Unable to set up camera preview.");
                    return;
                }

                // Unbind any use cases before binding them
                cameraProvider.unbindAll();

                // Bind camera use casespp
                Camera camera = cameraProvider.bindToLifecycle(
                        this, cameraSelector, preview);

            } catch (Exception e) {
                Log.e(TAG, "Error starting camera: " + e.getMessage());
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
}
