package com.example.virtual_eye_1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.os.Bundle;
import android.hardware.camera2.CameraCaptureSession;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private CameraCaptureSession cameraCaptureSession;
    private String cameraId;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private TextureView textureView;
    private Button startButton, stopButton;
    private CaptureRequest.Builder captureRequestBuilder;
    private SurfaceTexture surfaceTexture;
    private Surface captureSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        startButton = findViewById(R.id.start);
        stopButton = findViewById(R.id.stop);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        openCamera();
    }

    private CameraDevice.StateCallback myCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int errorCode) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };


    public void openCamera() {
        try {
            cameraId = cameraManager.getCameraIdList()[0];

            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);

            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            cameraManager.openCamera(cameraId, myCallBack, null);
        } catch(Exception e) {
            System.out.println("Error opening camera !!!");
        }
    }


    // opens a preview
    public void cameraPreview(View view) {
        surfaceTexture = textureView.getSurfaceTexture();
        captureSurface = new Surface(surfaceTexture);

        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(captureSurface);

            cameraDevice.createCaptureSession(Arrays.asList(captureSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    cameraCaptureSession = session;
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    // closes the preview
    public void closePreview(View view) {
        cameraCaptureSession.close();
    }
}