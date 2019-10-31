package com.personal.eyetracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class CompanionAppRelay extends AppCompatActivity {

    public CameraManager mCamManager;
    public CameraDevice mCameraDevice;
    private int currentCamera = 0;

    public ImageReader mImageReader;
    public TextureView mTextureView;
    public Surface surfaceTexture;
    public Surface imgReaderSurface;

    public int mImageWidth = 480;
    public int mImageHeight = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        Toast.makeText(CompanionAppRelay.this, Thread.currentThread().getName(), Toast.LENGTH_SHORT).show();

        Intent i = getIntent();

        new Thread(
                new NetworkOutput(
                        this,
                        i.getStringExtra("ip"),
                        i.getIntExtra("port", 0)
                )
        ).start();

        mTextureView = findViewById(R.id.textureView);

        ImageView cameraFlipper = findViewById(R.id.imageView);
        cameraFlipper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCameraDevice.close();

                CompanionAppRelay.this.currentCamera = (CompanionAppRelay.this.currentCamera + 1)%2;

                setupCamera();
            }
        });

        checkPermissions();
    }

    private void checkPermissions() {
        Permissions.check(this, Manifest.permission.CAMERA, "Click on Allow!", new PermissionHandler() {
            @Override
            public void onGranted() {
                initialization();

                setupCamera();
            }
        });
    }

    private void initialization() {
        mCamManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        mImageReader = ImageReader.newInstance(mImageWidth, mImageHeight, ImageFormat.YUV_420_888, 30);
        imgReaderSurface = mImageReader.getSurface();

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image img = reader.acquireNextImage();

                ByteBuffer buff = img.getPlanes()[0].getBuffer();
                byte[] buffBytes = new byte[buff.capacity()];

                buff.get(buffBytes);
                GlobalContext.buffer.add(buffBytes);

                img.close();

                Toast.makeText(CompanionAppRelay.this, buffBytes.length, Toast.LENGTH_LONG).show();

                synchronized (GlobalContext.buffer){
                    GlobalContext.buffer.notify();
                }
            }
        }, null);

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                surfaceTexture = new Surface(surface);

                startSession();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) { }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) { }
        });

    }

    public void setupCamera(){

        try {
            String cameraID = mCamManager.getCameraIdList()[currentCamera];
            mCamManager.openCamera(cameraID, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    mCameraDevice = camera;

                    startSession();
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

    public void startSession(){
        if(mCameraDevice == null || surfaceTexture == null){
            return;
        }

        try {
            final CaptureRequest.Builder captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
            captureRequest.addTarget(surfaceTexture);
            captureRequest.addTarget(imgReaderSurface);

            mCameraDevice.createCaptureSession(Arrays.asList(surfaceTexture, imgReaderSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {

                        session.setRepeatingRequest(captureRequest.build(), null, null);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(CompanionAppRelay.this, "Session could not be configured!", Toast.LENGTH_LONG).show();
                }
            }, null);



        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}

class GlobalContext{
    public static ArrayList<byte[]> buffer = new ArrayList<>();
}

class NetworkOutput implements Runnable {
    String ip;
    int port;
    CompanionAppRelay c;

    public NetworkOutput(CompanionAppRelay c, String ip, int port) {
        this.c = c;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket soc = new Socket(ip, port);
            ObjectOutputStream oos = new ObjectOutputStream(soc.getOutputStream());

            while(true){

                if(GlobalContext.buffer.size() == 0 ){
                    synchronized (GlobalContext.buffer){
                        GlobalContext.buffer.wait();
                    }
                }

                while(GlobalContext.buffer.size() > 0){
                    GlobalContext.buffer.remove(0);
                }
            }


//            oos.close();
//            soc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}