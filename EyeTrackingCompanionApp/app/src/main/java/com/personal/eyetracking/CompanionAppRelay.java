package com.personal.eyetracking;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompanionAppRelay extends AppCompatActivity {

    public CameraManager mCamManager;
    public CameraDevice mCameraDevice;
    private int currentCamera = 0;

    public ImageReader mImageReader;
    public TextureView mTextureView;
    public Surface surfaceTexture;
    public Surface imgReaderSurface;

    public Handler mBackgroundHandler;

    public int mImageWidth = 640;
    public int mImageHeight = 480;

    private int mFrameDelayms = 0;

    private long last_sent_time = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        Intent i = getIntent();

        HandlerThread mBackgroundHandlerThread = new HandlerThread("CompanionApp");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());

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
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

        Permissions.check(this, permissions , "Click on Allow!",null, new PermissionHandler() {
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

        Spinner spinner = findViewById(R.id.spinner);
        final List<String> fps_list = Arrays.asList(getResources().getStringArray(R.array.fps_list));

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                int rate = Integer.parseInt(fps_list.get(position));

                mFrameDelayms = 1000/rate;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                final Image img = reader.acquireNextImage();

                mBackgroundHandler.post(new Runnable() {
                    @Override
                    public void run() {

                        if(System.currentTimeMillis() - last_sent_time >= mFrameDelayms && GlobalContext.buffer.size() < 10){
                            GlobalContext.buffer.add(img);

                            last_sent_time = System.currentTimeMillis();

                            synchronized (GlobalContext.buffer){
                                GlobalContext.buffer.notify();
                            }
                        }else{
                            img.close();
                        }

                    }
                });
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
            final CaptureRequest.Builder captureRequest = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
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
    public static ArrayList<Image> buffer = new ArrayList<>();
}

class NetworkOutput implements Runnable {
    String ip;
    int port;
    CompanionAppRelay c;

    public boolean isStopped = false;

    public NetworkOutput(CompanionAppRelay c, String ip, int port) {
        this.c = c;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Socket soc = new Socket(ip, port);
            OutputStream os = soc.getOutputStream();
            DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(os));

            while(!isStopped){

                if(GlobalContext.buffer.size() == 0 ){
                    synchronized (GlobalContext.buffer){
                        GlobalContext.buffer.wait();
                    }
                }

                while(GlobalContext.buffer.size() > 0){
                    Image img = GlobalContext.buffer.remove(0);
                    byte[] image = ImageUtil.imageToByteArray(img);

                    dos.writeInt(image.length);
                    for(byte b: image){
                        dos.writeByte(b);
                    }

                    img.close();
                }
            }

            dos.close();
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setStopped(boolean stopped) {
        isStopped = stopped;
    }
}

final class ImageUtil {

    public static byte[] imageToByteArray(Image image) {
        byte[] data = null;
        if (image.getFormat() == ImageFormat.JPEG) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer buffer = planes[0].getBuffer();
            data = new byte[buffer.capacity()];
            buffer.get(data);
            return data;
        } else if (image.getFormat() == ImageFormat.YUV_420_888) {
            data = NV21toJPEG(
                    YUV_420_888toNV21(image),
                    image.getWidth(), image.getHeight());
        }
        return data;
    }

    private static byte[] YUV_420_888toNV21(Image image) {
        byte[] nv21;
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        nv21 = new byte[ySize + uSize + vSize];

        //U and V are swapped
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        return nv21;
    }

    private static byte[] NV21toJPEG(byte[] nv21, int width, int height) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        YuvImage yuv = new YuvImage(nv21, ImageFormat.NV21, width, height, null);
        yuv.compressToJpeg(new Rect(0, 0, width, height), 100, out);
        return out.toByteArray();
    }
}