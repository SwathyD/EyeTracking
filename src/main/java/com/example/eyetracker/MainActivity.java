package com.example.eyetracker;

import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startCapture(View v){
        Intent captureVideo = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if(captureVideo.resolveActivity(getPackageManager())!=null){
            startActivityForResult(captureVideo, 1);
        }
    }
}
