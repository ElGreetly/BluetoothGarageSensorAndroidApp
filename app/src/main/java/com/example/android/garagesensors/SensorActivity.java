package com.example.android.garagesensors;

import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class SensorActivity extends AppCompatActivity {
    String address;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    ProgressDialog progressDialog;
    boolean btConnected = false;
    UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler mHandler;
    int[] imageId = new int[6];
    KeyguardManager.KeyguardLock kl;
    PowerManager.WakeLock wl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "INFO");
        wl.acquire();

        KeyguardManager km = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
        kl = km.newKeyguardLock("name");
        kl.disableKeyguard();
        address = getIntent().getExtras().getString(DeviceListActivity.deviceAddress);
        imageId = new int[] {R.id.left_car1, R.id.left_car2, R.id.left_car3, R.id.right_car1, R.id.right_car2, R.id.right_car3};
        new Connect().execute();
        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1){
                    byte[] writeBuf = (byte[]) msg.obj;
                    int begin = (int)msg.arg1;
                    int end = (int)msg.arg2;
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    ImageView imageView;
                    int t = Integer.parseInt(writeMessage);
                    if(t == 11){
                        imageView = (ImageView) findViewById(R.id.left_car1);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (t == 10){
                        imageView = (ImageView) findViewById(R.id.left_car1);
                        imageView.setVisibility(View.INVISIBLE);
                    } else if (t == 21){
                        imageView = (ImageView) findViewById(R.id.left_car2);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (t == 20){
                        imageView = (ImageView) findViewById(R.id.left_car2);
                        imageView.setVisibility(View.INVISIBLE);
                    } else if (t == 31){
                        imageView = (ImageView) findViewById(R.id.left_car3);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (t == 30){
                        imageView = (ImageView) findViewById(R.id.left_car3);
                        imageView.setVisibility(View.INVISIBLE);
                    } else if (t == 41){
                        imageView = (ImageView) findViewById(R.id.right_car1);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (t == 40){
                        imageView = (ImageView) findViewById(R.id.right_car1);
                        imageView.setVisibility(View.INVISIBLE);
                    } else if (t == 51){
                        imageView = (ImageView) findViewById(R.id.right_car2);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (t == 50){
                        imageView = (ImageView) findViewById(R.id.right_car2);
                        imageView.setVisibility(View.INVISIBLE);
                    } else if (t == 61){
                        imageView = (ImageView) findViewById(R.id.right_car3);
                        imageView.setVisibility(View.VISIBLE);
                    } else if (t == 60){
                        imageView = (ImageView) findViewById(R.id.right_car3);
                        imageView.setVisibility(View.INVISIBLE);
                    }
                }
             }
        };
    }
    private class Connect extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SensorActivity.this, "Connecting....", "Please Wait!");
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                if (socket == null || !btConnected) {
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
                    socket = device.createRfcommSocketToServiceRecord(mUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    socket.connect();
                    btConnected = true;
                    Connected connected = new Connected(socket);
                    connected.start();
                }
            } catch (IOException e) {
                btConnected = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void Result){
            super.onPostExecute(Result);
            if(!btConnected){
                Toast.makeText(getApplicationContext(), "Connection Failed!", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }
    private class Connected extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public Connected(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;
            while (true) {
                try {
                    bytes += mmInStream.read(buffer, bytes, buffer.length - bytes); for(int i = begin; i < bytes; i++) {
                        if(buffer[i] == "#".getBytes()[0]) {
                            mHandler.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if(i == bytes - 1) {
                                bytes = 0;
                                begin = 0; }
                        } }
                } catch (IOException e) {
                    break;
                } }
        }
    }
}
