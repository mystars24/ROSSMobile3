package com.example.rossmobile3.fragments;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.example.rossmobile3.R;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class DeviceControl extends Fragment {

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
    private View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_device_control, container, false);
        initializeComponents();
        return view;
    }

    private void initializeComponents() {
        btnUp = view.findViewById(R.id.btnUp);
        btnDown = view.findViewById(R.id.btnDown);
        btnLeft = view.findViewById(R.id.btnLeft);
        btnRight = view.findViewById(R.id.btnRight);
        manualautoBtn = view.findViewById(R.id.manualautoBtn);
        onoffBtn = view.findViewById(R.id.onoffBtn);
        textStatus = view.findViewById(R.id.textStatus);
        connectdiscBtn = view.findViewById(R.id.connectdiscBtn);
        listViewDevices = view.findViewById(R.id.listViewDevices);
        deviceListContainer = view.findViewById(R.id.deviceListContainer);
//        Button backBtn = view.findViewById(R.id.backbtn);

//        backBtn.setOnClickListener(v -> requireActivity().onBackPressed());

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceListAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1);
        listViewDevices.setAdapter(deviceListAdapter);

        updateStatus("Not Connected");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 1);
        }

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

        manualautoBtn.setOnClickListener(v -> {
            isAutoMode = !isAutoMode;
            sendCommand(isAutoMode ? "A" : "M");
            setControlButtonsEnabled(!isAutoMode);
            updateStatus(isAutoMode ? "Auto Mode Enabled" : "Manual Mode Enabled");
            manualautoBtn.setText(isAutoMode ? "Switch to Manual" : "Switch to Auto");
        });

        onoffBtn.setOnClickListener(v -> {
            isSweeperOn = !isSweeperOn;
            sendCommand(isSweeperOn ? "O" : "F");
            onoffBtn.setText(isSweeperOn ? "Sweeper On" : "Sweeper Off");
        });

        listViewDevices.setOnItemClickListener((parent, view, position, id) -> {
            String selectedDevice = (String) parent.getItemAtPosition(position);
            String deviceAddress = selectedDevice.split("\n")[1];
            BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
            connectToDevice(device);
            deviceListContainer.setVisibility(View.GONE);
        });

        ImageView btnCloseDeviceList = view.findViewById(R.id.btnCloseDeviceList);
        btnCloseDeviceList.setOnClickListener(v -> deviceListContainer.setVisibility(View.GONE));
    }

    private void updateStatus(String status) {
        textStatus.setText(status);
    }

    private View.OnTouchListener createTouchListener(final String command, final ImageView button, final int normalImage, final int pressedImage) {
        return (v, event) -> {
            if (isAutoMode) {
                Toast.makeText(getContext(), "Auto Mode is enabled. Switch to Manual Mode to use this button.", Toast.LENGTH_SHORT).show();
                return false;
            }
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    button.setImageResource(pressedImage);
                    sendCommand(command);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    button.setImageResource(normalImage);
                    sendCommand("S");
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
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Toast.makeText(getContext(), "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            return;
        }

        deviceListAdapter.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.isEmpty()) {
            Toast.makeText(getContext(), "No paired Bluetooth devices found", Toast.LENGTH_SHORT).show();
            return;
        }
        for (BluetoothDevice device : pairedDevices) {
            deviceListAdapter.add(device.getName() + "\n" + device.getAddress());
        }
        deviceListContainer.setVisibility(View.VISIBLE);
    }

    private void connectToDevice(BluetoothDevice device) {
        try {
            bluetoothSocket = device.createRfcommSocketToServiceRecord(HC05_UUID);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            updateStatus("Connected to " + device.getName());
        } catch (IOException e) {
            updateStatus("Connection Failed");
        }
    }

    private void sendCommand(String command) {
        if (bluetoothSocket == null || outputStream == null) return;
        try {
            outputStream.write(command.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            disconnectBluetooth();
        }
    }

    private void disconnectBluetooth() {
        try {
            if (outputStream != null) outputStream.close();
            if (bluetoothSocket != null) bluetoothSocket.close();
            updateStatus("Not Connected");
        } catch (IOException ignored) {}
    }
}
