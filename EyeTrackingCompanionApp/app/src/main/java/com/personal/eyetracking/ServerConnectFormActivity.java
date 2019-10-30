package com.personal.eyetracking;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

public class ServerConnectFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_connect);

        final Button connect_button = findViewById(R.id.button);
        final EditText server_ip = findViewById(R.id.editText);
        final EditText port = findViewById(R.id.editText2);

        Permissions.check(this, Manifest.permission.INTERNET, "Click on Allow!", new PermissionHandler() {
            @Override
            public void onGranted() {

                connect_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(ServerConnectFormActivity.this, CompanionAppRelay.class);
                        i.putExtra("port", Integer.parseInt(port.getText().toString()));
                        i.putExtra("ip", server_ip.getText().toString());

                        startActivity(i);
                    }
                });
            }
        });

    }
}
