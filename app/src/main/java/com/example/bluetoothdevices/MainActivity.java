package com.example.bluetoothdevices;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Context context;
    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> devicesAdapter;
    private ArrayList<String> devicesList;
    private static final int PERMISSION_OK = 1;

    Button stop_button, restart_button;

    @SuppressLint("MissingPermission")
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {

            if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class);

                assert device != null;
                String deviceName = device.getName() != null
                        ? device.getName()
                        : "Dispositivo desconocido";
//
                String deviceAddress = device.getAddress();
                String deviceInfo =
                        "NOMBRE: " + deviceName + "\nMAC: " + deviceAddress + "\n" + "CLASE: " + device.getBluetoothClass() + "\n";

                Log.e("Dispositivos encontrados: ", deviceInfo);

                if (!devicesList.contains(deviceInfo)) {
                    devicesList.add(deviceInfo);
                    devicesAdapter.notifyDataSetChanged();
                }
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(intent.getAction())) {
                Toast.makeText(context, "Buscando dispositivos",
                        Toast.LENGTH_LONG).show();
            }

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
                searchBluetoothDevices();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter =
                this.getSystemService(BluetoothManager.class).getAdapter();

        ListView listView = findViewById(R.id.device_list);
        devicesList = new ArrayList<>();
        devicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                devicesList);
        listView.setAdapter(devicesAdapter);

        Button searchButton = findViewById(R.id.search_button);
        searchButton.setOnClickListener(v -> {
            devicesList.clear();
            searchBluetoothDevices();
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        registerReceiver(receiver, filter);
    }

    protected void onDestroy() {
        super.onDestroy();

        if (bluetoothAdapter != null) {
            if (ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.BLUETOOTH_SCAN)
                    == PackageManager.PERMISSION_GRANTED) {
                bluetoothAdapter.cancelDiscovery();
            }
        }
        unregisterReceiver(receiver);
    }

    @SuppressLint("MissingPermission")
    private void searchBluetoothDevices() {
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        } else {

            devicesAdapter.notifyDataSetChanged();
            if (checkPermissions()) {
                requestPermissions();
            } else {
                bluetoothAdapter.startDiscovery();
            }
        }
    }

    private boolean checkPermissions() {
        return (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.BLUETOOTH_SCAN)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED
        );
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{
                        android.Manifest.permission.BLUETOOTH_SCAN,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.BLUETOOTH_CONNECT,
                }, PERMISSION_OK);
    }
}