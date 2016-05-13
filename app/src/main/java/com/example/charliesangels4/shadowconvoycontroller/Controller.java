package com.example.charliesangels4.shadowconvoycontroller;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.UUID;

/* Note about OnTouchListener implementation:
    By setting the listener for EACH BUTTON, we are allowing multi-touch within each button.
    This is useful in detecting a button hold and release, but doesn't allow us to detect two buttons
    being pressed as two pointers. This is because both buttons register a primary pointer in themselves,
    while the view can see a primary and secondary pointer.
 */


public class Controller extends AppCompatActivity implements View.OnTouchListener {

    private static final String TAG = "Controller"; // For debugging with Log.d

    Button btnUp, btnDown, btnLeft, btnRight;
    String address = null;

    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;

    //Note: Hardcoded UUID is a well-known id for Bluetooth serial boards.
    //SPP UUID. Look for it
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        Intent newint = getIntent();
        address = newint.getStringExtra(DeviceList.EXTRA_ADDRESS); //receive the address of the bluetooth device

        // Call the Widgets
        btnUp = (Button)findViewById(R.id.buttonUp);
        btnUp.setOnTouchListener(this);
        btnDown = (Button)findViewById(R.id.buttonDown);
        btnDown.setOnTouchListener(this);
        btnLeft = (Button)findViewById(R.id.buttonLeft);
        btnLeft.setOnTouchListener(this);
        btnRight = (Button)findViewById(R.id.buttonRight);
        btnRight.setOnTouchListener(this);

        new ConnectBT().execute(); //Call the class to connect
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Disconnect();
    }

    // Transmits controller input through Bluetooth adapter
    public boolean onTouch(View v, MotionEvent event){
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            Log.d(TAG,event.toString());
            Log.d(TAG,Integer.toString(event.getActionMasked()));
            switch (v.getId()) {
                case R.id.buttonUp:
                    accel("F1"); // Binary value. TODO: Add resolution to acceleration speed
                    break;
                case R.id.buttonDown:
                    accel("B1"); // Binary value. TODO: Add resolution to acceleration speed
                    break;
                case R.id.buttonLeft:
                    turn("L1");
                    break;
                case R.id.buttonRight:
                    turn("R1");
                    break;
                default:
                    break;
            }
        } else if (event.getActionMasked() == MotionEvent.ACTION_UP){
            switch (v.getId()) {
                case R.id.buttonUp:
                    accel("F0"); // Binary value. TODO: Add resolution to acceleration speed
                    break;
                case R.id.buttonDown:
                    accel("B0"); // Binary value. TODO: Add resolution to acceleration speed
                    break;
                case R.id.buttonLeft:
                    turn("L0");
                    break;
                case R.id.buttonRight:
                    turn("R0");
                    break;
                default:
                    break;
            }
        }

        return true;
    }

    private void turn(String dir)
    {
        if (btSocket!=null)
        {
            try
            {
                String cmd = "T" + dir;
                btSocket.getOutputStream().write(cmd.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    private void accel(String speed)
    {
        if (btSocket!=null)
        {
            try
            {
                String cmd = "A" + speed;
                btSocket.getOutputStream().write(cmd.getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

//    private void brake()
//    {
//        if (btSocket!=null)
//        {
//            try
//            {
//                String cmd = "BRAKE";
//                btSocket.getOutputStream().write(cmd.getBytes());
//            }
//            catch (IOException e)
//            {
//                msg("Error");
//            }
//        }
//    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
//        finish(); //return to the first layout
    }

    // fast way to call Toast
    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s, Toast.LENGTH_LONG).show();
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(Controller.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice car_BT = myBluetooth.getRemoteDevice(address);//connects to the device's address (RC car in this case) and checks if it's available
                    //TODO: Use createRfcommSocket... instead, to create a secure connection (See BluetoothDevice documentation)
                    btSocket = car_BT.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connectio
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {
                msg("Connection Failed. Is it a SPP Bluetooth? Try again.");
                finish();
            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }
}
