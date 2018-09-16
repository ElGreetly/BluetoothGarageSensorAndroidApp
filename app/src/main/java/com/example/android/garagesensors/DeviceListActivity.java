package com.example.android.garagesensors;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {
    ListView deviceList;
    Button getDevices;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> bluetoothDevices;
    public static final String deviceAddress = "device-address";
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        deviceList = (ListView) findViewById(R.id.list);
        getDevices = (Button) findViewById(R.id.devices);
        textView = (TextView) findViewById(R.id.text);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth device not available", Toast.LENGTH_SHORT).show();
        } else if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
        getDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setVisibility(View.VISIBLE);
                Pair();
            }
        });
    }
    void Pair() {
        bluetoothDevices = bluetoothAdapter.getBondedDevices();
        ArrayList list = new ArrayList();
        if(bluetoothDevices.size()>0){
            for(BluetoothDevice btBonded : bluetoothDevices){
                list.add(btBonded.getName() + "\n" + btBonded.getAddress());
            }
        } else {
            Toast.makeText(this, "No Paired Devices", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, list);
        deviceList.setAdapter(adapter);
        deviceList.setOnItemClickListener(clickListener);
    }
    AdapterView.OnItemClickListener clickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent intent = new Intent(DeviceListActivity.this, SensorActivity.class);
            intent.putExtra(deviceAddress, address);
            startActivity(intent);
        }
    };
}
