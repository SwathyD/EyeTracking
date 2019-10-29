package com.personal.eyetracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.media.ImageReader;
import android.os.Bundle;
import android.view.Surface;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    public CameraManager mCamManager;
    public ImageReader mImageReader;
    public CameraDevice mCameraDevice;

    public int mImageWidth = 500;
    public int mImageHeight = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();
    }

    private void checkPermissions() {
        Permissions.check(this, Manifest.permission.CAMERA, "Chup Chap Allow Kar Be!", new PermissionHandler() {
            @Override
            public void onGranted() {
                setupCamera();
            }
        });
    }

    public void setupCamera(){
        mCamManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mImageReader = ImageReader.newInstance(mImageWidth, mImageHeight, ImageFormat.JPEG, 30);

        try {

            String cameraID = mCamManager.getCameraIdList()[0];
            mCamManager.openCamera(cameraID, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Toast.makeText(MainActivity.this, "Opened.", Toast.LENGTH_LONG).show();
                    mCameraDevice = camera;

                    createSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    camera.close();
                    mCameraDevice = null;
                }
            }, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createSession(){
        Surface imgReaderSurface = mImageReader.getSurface();

        mCameraDevice.createCaptureSession(Arrays.asList(imgReaderSurface), new CameraCaptureSession.StateCallback() {
            @Override
            public void onConfigured(@NonNull CameraCaptureSession session) {

            }

            @Override
            public void onConfigureFailed(@NonNull CameraCaptureSession session) {

            }
        }, null);
    }
}
