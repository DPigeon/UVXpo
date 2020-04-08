package com.example.utilisateur.uvexposureapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVER_BT = 1;

    Button toggleButton;
    ToggleButton scanButton;
    TextView pairedTextView;

    BluetoothAdapter bluetoothAdapter;
    protected ArrayAdapter adapter;
    protected ArrayAdapter adapter2;
    protected ListView deviceListView;
    protected ListView pairedDevicesListView;
    protected List<String> devicesString;
    protected List<String> pairedDevicesString;
    protected List<BluetoothDevice> availableDevices;
    protected List<BluetoothDevice> pairedDevices;
    protected SharedPreferencesHelper sharedPreferencesHelper;
    int counter = 0; // for scanned devices

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        sharedPreferencesHelper = new SharedPreferencesHelper(BluetoothActivity.this);
        devicesString = new ArrayList<String>();
        pairedDevicesString = new ArrayList<String>();
        availableDevices = new ArrayList<BluetoothDevice>();
        pairedDevices = new ArrayList<BluetoothDevice>();

        setupUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        updatePairedDevices();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    protected void setupUI() {
        toggleButton = findViewById(R.id.toggleButton);
        scanButton = findViewById(R.id.scanButton);
        pairedTextView = findViewById(R.id.pairedTextView);
        deviceListView = findViewById(R.id.scanListView);
        pairedDevicesListView = findViewById(R.id.pairedDeviceListView);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // Initialize bluetooth
        bluetoothSetStatus();
        bluetoothButtons();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, devicesString);
        adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, pairedDevicesString);
        deviceListView.setAdapter(adapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) { // As soon as we click an item in the course view list
                BluetoothDevice device = availableDevices.get(position);
                try {
                    if (pairDevice(device)) { // Pairing the device
                        sharedPreferencesHelper.saveBluetoothConnection(device.getAddress());
                        updatePairedDevices();
                        adapter2.notifyDataSetChanged();
                        displayToast("Bluetooth device paired!");
                    }
                    else
                        displayToast("Bluetooth device was not paired!");
                } catch (Exception exception) {
                    displayToast("Error pairing device!");
                }
            }
        });
        scanButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                if (isChecked) {
                    adapter.clear();
                    registerReceiver(broadReceiver, filter);
                    bluetoothAdapter.startDiscovery();
                } else {
                    unregisterReceiver(broadReceiver);
                    bluetoothAdapter.cancelDiscovery();
                }
            }
        });
        pairedDevicesListView.setAdapter(adapter2);
        pairedDevicesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) { // As soon as we click an item in the course view list
                BluetoothDevice device = pairedDevices.get(position);
                try {
                    unpairDevice(device);
                    pairedDevices.remove(position);
                    adapter2.notifyDataSetChanged();
                } catch (Exception exception) {
                    displayToast("Error unpairing device!");
                }
            }
        });
    }

    protected void updatePairedDevices() {
        Set<BluetoothDevice> devices = bluetoothAdapter.getBondedDevices(); // Get paired devices
        for(BluetoothDevice device : devices) {
            pairedDevices.add(device);
            pairedDevicesString.add("    " + device.getName()); // We add all devices already paired with the app
        }
    }

    protected void bluetoothSetStatus() { // Sets the status of the bluetooth connection
        if (bluetoothAdapter == null) {
            Log.i("BT", "Bluetooth not supported on Virtual Devices! Use a real device.");
            finish(); // Allowing to skip the exception
        } else
            displayToast("Bluetooth is available!");

            if (bluetoothAdapter.isEnabled())
                displayToast("Bluetooth is on!");
            else
                displayToast("Bluetooth is off!");
    }

    protected void bluetoothButtons() {
        // Toggle button
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!bluetoothAdapter.isEnabled()) {
                    displayToast("Turning on Bluetooth...");
                    // Start a new bluetooth activity
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(intent, REQUEST_ENABLE_BT);
                    toggleButton.setText("On");
                    pairedTextView.setText("Paired Devices"); // Clearing the paired devices list
                } else {
                    displayToast("Turning Bluetooth off...");
                    bluetoothAdapter.disable();
                    toggleButton.setText("Off");
                    pairedTextView.setText("No Paired Devices"); // Clearing the paired devices list
                }
            }
        });
    }

    public boolean pairDevice(BluetoothDevice device) throws Exception {
        Class class1 = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = class1.getMethod("createBond");
        Boolean returnValue = (Boolean) createBondMethod.invoke(device);
        return returnValue.booleanValue();
    }

    public void unpairDevice(BluetoothDevice device) throws Exception {
        try {
            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
            displayToast("Device " + device.getName() + " has been unpaired!");
        } catch (Exception e) {
            displayToast("Could not unpair device!");
        }
    }

    private BluetoothAdapter.LeScanCallback mScanCb = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            String deviceName = device.getName(); // Bug: always get null for most of devices...
            if (deviceName != null) {
                devicesString.add("    " + deviceName + "\n    " + device.getAddress());
                availableDevices.add(device);
            }
            adapter.notifyDataSetChanged();
            counter = counter + 1;
        }
    };

    private final BroadcastReceiver broadReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE); // We find every device broadcasted
                if(device.getBondState() != BluetoothDevice.BOND_BONDED)
                    bluetoothAdapter.startLeScan(mScanCb);
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // Used to show an action after sent to a new activity
        switch(resultCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK)
                    displayToast("Bluetooth is on!");
                else
                    displayToast("Could not turn Bluetooth on!");
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void displayToast(String message) { // Used to display toasts
        Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG);
        toast.show();
    }

}
