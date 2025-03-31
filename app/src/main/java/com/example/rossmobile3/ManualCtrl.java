package com.example.rossmobile3;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class ManualCtrl extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket bluetoothSocket;
    private OutputStream outputStream;
    private final String HC05_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FB";
    private final UUID HC05_UUID = UUID.fromString(HC05_UUID_STRING);
    private ListView listViewDevices;
    private ArrayAdapter<String> deviceListAdapter;
    private LinearLayout deviceListContainer;
    private ImageView btnUp, btnDown, btnRight, btnLeft;
    private boolean isSweeperOn = false;
    private boolean isAutoMode = false;

    private Button manualautoBtn, connectdiscBtn, onoffBtn;
    private TextView textStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_ctrl);

        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        manualautoBtn = findViewById(R.id.manualautoBtn);
        onoffBtn = findViewById(R.id.onoffBtn);
        textStatus = findViewById(R.id.textStatus);
        connectdiscBtn = findViewById(R.id.connectdiscBtn);
        listViewDevices = findViewById(R.id.listViewDevices);
        deviceListContainer = findViewById(R.id.deviceListContainer);
        Button backBtn = findViewById(R.id.backbtn);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ManualCtrl.this, UserMainDashboard.class);
                startActivity(intent);
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listViewDevices.setAdapter(deviceListAdapter);

        updateStatus("Not Connected");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }

//        btnConnect.setOnClickListener(v -> showAvailableDevices());
//        btnDisconnect.setOnClickListener(v -> disconnectBluetooth());

        connectdiscBtn.setOnClickListener(v -> {
            if (bluetoothSocket != null && bluetoothSocket.isConnected()) {
                disconnectBluetooth();
                connectdiscBtn.setText("Connect to Device");
            } else {
                showAvailableDevices();
                connectdiscBtn.setText("Disconnect");
            }
        });


        btnUp.setOnTouchListener(createTouchListener("U", btnUp, R.drawable.arrow_up, R.drawable.arrow_press_up));
        btnDown.setOnTouchListener(createTouchListener("D", btnDown, R.drawable.arrow_down, R.drawable.arrow_press_down));
        btnLeft.setOnTouchListener(createTouchListener("L", btnLeft, R.drawable.arrow_left, R.drawable.arrow_press_left));
        btnRight.setOnTouchListener(createTouchListener("R", btnRight, R.drawable.arrow_right, R.drawable.arrow_press_right));


//        autoBtn.setOnClickListener(v -> {
//            if (!isAutoMode) {
//                sendCommand("A");
//                setControlButtonsEnabled(false);
//                isAutoMode = true;
//                updateStatus("Auto Mode Enabled");
//            }
//        });
//
//        manualBtn.setOnClickListener(v -> {
//            if (isAutoMode) {
//                sendCommand("M");
//                setControlButtonsEnabled(true);
//                isAutoMode = false;
//                updateStatus("Manual Mode Enabled");
//            }
//        });

        manualautoBtn.setOnClickListener(v -> {
            if (!isAutoMode) {
                sendCommand("A");
                setControlButtonsEnabled(false);
                isAutoMode = true;
                updateStatus("Auto Mode Enabled");
                manualautoBtn.setText("Switch to Manual");
            } else {
                sendCommand("M");
                setControlButtonsEnabled(true);
                isAutoMode = false;
                updateStatus("Manual Mode Enabled");
                manualautoBtn.setText("Switch to Auto");
            }
        });

        onoffBtn.setOnClickListener(v -> {
//            Toast toast;
            if (isSweeperOn) {
                sendCommand("F");
                isSweeperOn = false;
                onoffBtn.setText("Sweeper Off"); // Change button text
//                toast = Toast.makeText(v.getContext(), "Sweeper Turned Off", Toast.LENGTH_SHORT);
            } else {
                sendCommand("O");
                isSweeperOn = true;
                onoffBtn.setText("Sweeper On"); // Change button text
//                toast = Toast.makeText(v.getContext(), "Sweeper Turned On", Toast.LENGTH_SHORT);
            }

//            toast.show();

            // Dismiss the toast after 1 second
//            new android.os.Handler().postDelayed(toast::cancel, 1000);
        });



        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDevice = (String) parent.getItemAtPosition(position);
            String deviceAddress = selectedDevice.split("\n")[1];
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
            deviceListContainer.setVisibility(View.GONE);
        });

        ImageView btnCloseDeviceList = findViewById(R.id.btnCloseDeviceList);
        btnCloseDeviceList.setOnClickListener(v -> {
            deviceListContainer.setVisibility(View.GONE);
        });
    }

    private void updateStatus(String status) {
        textStatus.setText(status);
    }

    private View.OnTouchListener createTouchListener(final String command, final ImageView button, final int normalImage, final int pressedImage) {
        return (v, event) -> {
            if (isAutoMode) {
                Toast.makeText(this, "Auto Mode is enabled. Switch to Manual Mode to use this button.", Toast.LENGTH_SHORT).show();
                return false;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setImageResource(pressedImage); // Change to pressed image
                    sendCommand(command);
//                    Toast.makeText(this, "Sent command: " + command, Toast.LENGTH_SHORT).show();
                    return true;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    button.setImageResource(normalImage); // Revert to normal image
                    sendCommand("S");
//                    Toast.makeText(this, "Sent command: S (Stop)", Toast.LENGTH_SHORT).show();
                    return true;
            }
            return false;
        };
    }



    private void setControlButtonsEnabled(boolean enabled) {
        int visibility = enabled ? View.VISIBLE : View.GONE;
        btnUp.setVisibility(visibility);
        btnDown.setVisibility(visibility);
        btnLeft.setVisibility(visibility);
        btnRight.setVisibility(visibility);
    }

    private void showAvailableDevices() {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        if (deviceListContainer == null) {
            Toast.makeText(this, "UI Error: deviceListContainer is not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceListAdapter.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(this, "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
            return;
        }

        for (BluetoothDevice device : pairedDevices) {
            deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
        }

        runOnUiThread(() -> deviceListContainer.setVisibility(View.VISIBLE));
    }


    private void connectToDevice(BluetoothDevice device) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(HC05_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            updateStatus("Connected to " + device.getName());
            connectdiscBtn.setText("Disconnect"); // Update button text
        } catch (IOException e) {
            updateStatus("Connection Failed");
            e.printStackTrace(); // Add this to check what went wrong
        }
    }


    private void sendCommand(String command) {
        if (bluetoothSocket == null || outputStream == null || !bluetoothSocket.isConnected()) {
            Toast.makeText(this, "Not connected to Bluetooth device", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            updateStatus("Failed to send command");
            disconnectBluetooth(); // Reset connection if something goes wrong
        }
    }


    private void disconnectBluetooth() {
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
            updateStatus("Not Connected");
        } catch (IOException e) {
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBluetooth();
    }
}