package com.example.charliesangels4.shadowconvoycontroller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /* Switch to the DeviceList Activity */
    public void switchToDeviceList (View view){
        Intent intent = new Intent(this, DeviceList.class);
        startActivity(intent);
    }
}
