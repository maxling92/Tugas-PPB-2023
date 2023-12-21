package com.example.fruitscanner2;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.fruitscanner2.ml.ModelUnquant;
import com.google.common.util.concurrent.ListenableFuture;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    TextView result, confidence;
    ImageView imageView;
    Button cameraButton;

    private PreviewView previewView;

    private ImageCapture imageCapture;

    private ProcessCameraProvider cameraProvider;



    int imageSize = 224;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        result = findViewById(R.id.result);
        confidence = findViewById(R.id.objectConfi);
        imageView = findViewById(R.id.imageView);
        cameraButton = findViewById(R.id.camera);

        previewView = findViewById(R.id.previewView);

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        // Periksa dan minta izin kamera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            try {
                setupCamera();
            } catch (Exception e) {
                // Tangani kesalahan inisialisasi kamera
                e.printStackTrace();
                Toast.makeText(this, "Terjadi kesalahan saat menginisialisasi kamera", Toast.LENGTH_SHORT).show();
            }
        }



        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("CameraButton", "Button clicked");
                // Lakukan pengecekan izin kamera sebelum memulai kamera
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        // Periksa status kamera sebelum mengambil gambar
                        if (cameraProvider != null) {
                            // Tetapkan file penyimpanan dan konfigurasi untuk penyimpanan gambar
                            File photoFile = getUniqueTempFile();
                            ImageCapture.OutputFileOptions outputFileOptions =
                                    new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                            Log.d("CameraButton", "Before taking picture");
                            // Mengambil gambar
                            imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(MainActivity.this),
                                    new ImageCapture.OnImageSavedCallback() {
                                        @Override
                                        public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                            Log.d("CameraButton", "Image saved");
                                            // File gambar tersimpan, tampilkan di ImageView
                                            Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                                            imageView.setImageBitmap(imageBitmap);

                                            // Proses klasifikasi gambar
                                            classifyImage(imageBitmap);
                                        }

                                        @Override
                                        public void onError(@NonNull ImageCaptureException error) {
                                            Log.e("CameraButton", "Error taking picture: " + error.getMessage());
                                            // Tangani kesalahan saat mengambil gambar
                                            error.printStackTrace();
                                            Toast.makeText(MainActivity.this, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Log.e("CameraButton", "Camera provider is null");
                            Toast.makeText(MainActivity.this, "Terjadi kesalahan: Kamera tidak tersedia", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("CameraButton", "Error: " + e.getMessage());
                        // Tangani kesalahan saat mengambil gambar
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.d("CameraButton", "Requesting camera permission");
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

    }



    // Metode ini akan dipanggil setelah izin diberikan atau tidak diberikan oleh pengguna
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Izin diberikan, lakukan setup kamera
            setupCamera() ;
        } else {
            // Izin ditolak, berikan pesan kepada pengguna atau ambil tindakan yang sesuai
            Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
        }
    }

    public void classifyImage(Bitmap image){
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for(int i = 0; i < imageSize; i++){
                for (int j = 0; j< imageSize; j++){
                    int val = intValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f /255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f /255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f /255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            int maxPos = 0;
            float maxConfidence = 0;
            for (int i = 0; i < confidences.length; i++){
                if(confidences[i] > maxConfidence){
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }
            String[] classes = getResources().getStringArray(R.array.fruit_classes);

            result.setText(classes[maxPos]);

            String s = "";
            for (int i = 0; i < classes.length; i++){
                s += String.format("%s; %.1f%%\n", classes[i], confidences[i] * 100);
            }

            confidence.setText(s);

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Terjadi kesalahan saat memuat model", Toast.LENGTH_SHORT).show();
        }

    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
                try {
                    Bitmap image = (Bitmap) data.getExtras().get("data");
                    int dimension = Math.min(image.getWidth(), image.getHeight());
                    image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);
                    imageView.setImageBitmap(image);

                    image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
                    classifyImage(image);
                } catch (Exception e) {
                    // Tangani kesalahan saat memproses gambar
                    e.printStackTrace();
                    Toast.makeText(this, "Terjadi kesalahan saat memproses gambar", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Handle the case where data is null or not an instance of Bitmap
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void setupCamera() {
        // Inisialisasi CameraX
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                imageCapture = new ImageCapture.Builder()
                        .setTargetResolution(new Size(imageSize, imageSize))
                        .build();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // Tangani kesalahan inisialisasi kamera
                e.printStackTrace();
                Toast.makeText(this, "Terjadi kesalahan saat menginisialisasi kamera", Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview(ProcessCameraProvider cameraProvider) {
        ImageView imageView = findViewById(R.id.imageView);


        // Konfigurasi CameraX
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();


        // Konfigurasi UseCase untuk menangkap gambar
        // ImageCapture imageCapture = new ImageCapture.Builder()
        //        .setTargetResolution(new Size(imageSize, imageSize))
        //        .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        // Hubungkan UseCase dengan kamera
        Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

        // Tambahkan OnClickListener untuk tombol camera

        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Tetapkan file penyimpanan dan konfigurasi untuk penyimpanan gambar
                    File photoFile = new File(getBatchDirectoryName(), "temp.jpg");
                    ImageCapture.OutputFileOptions outputFileOptions =
                            new ImageCapture.OutputFileOptions.Builder(photoFile).build();

                    // Mengambil gambar
                    imageCapture.takePicture(outputFileOptions, ContextCompat.getMainExecutor(MainActivity.this),
                            new ImageCapture.OnImageSavedCallback() {
                                @Override
                                public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                                    // File gambar tersimpan, tampilkan di ImageView
                                    Bitmap imageBitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                                    imageView.setImageBitmap(imageBitmap);

                                    // Proses klasifikasi gambar
                                    classifyImage(imageBitmap);
                                }

                                @Override
                                public void onError(@NonNull ImageCaptureException error) {
                                    // Tangani kesalahan saat mengambil gambar
                                    error.printStackTrace();
                                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show();
                                }
                            });
                } catch (Exception e) {
                    // Tangani kesalahan saat mengambil gambar
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Terjadi kesalahan saat mengambil gambar", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private File getBatchDirectoryName() {
        File appDir = new File(getExternalFilesDir(null), getResources().getString(R.string.app_name));
        if (!appDir.exists()) {
            if (!appDir.mkdirs()) {
                // Gagal membuat direktori, handle sesuai kebutuhan aplikasi Anda
                Log.e("DirectoryCreation", "Gagal membuat direktori");
            }
        }
        return new File(appDir, "temp.jpg");
    }
    private File getUniqueTempFile() {
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timestamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        Log.d("StorageDir", "Path: " + storageDir.getAbsolutePath());
        try {
            return File.createTempFile(imageFileName, ".jpg", storageDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
