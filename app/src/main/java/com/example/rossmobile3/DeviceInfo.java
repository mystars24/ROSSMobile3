package com.example.rossmobile3;

import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DeviceInfo extends AppCompatActivity {

    private TextView deviceNameText, deviceStatusText, statusText;
    private DatabaseReference realtimeDb;
    private Handler handler = new Handler();
    private Runnable checkStatusRunnable;
    private static final long INACTIVITY_THRESHOLD = 10000; // 10 seconds

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        deviceNameText = findViewById(R.id.deviceName);
        deviceStatusText = findViewById(R.id.deviceStatus);
        statusText = findViewById(R.id.trashLevel);

        String deviceId = getIntent().getStringExtra("deviceId");
        if (deviceId != null) {
            deviceNameText.setText("Device: " + deviceId);
            fetchDeviceData(deviceId);
        }
    }

    private void fetchDeviceData(String deviceId) {
        realtimeDb = FirebaseDatabase.getInstance().getReference("device").child(deviceId);

        ValueEventListener deviceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deviceStatus = snapshot.child("deviceStatus").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);

                    deviceStatusText.setText(deviceStatus != null ? deviceStatus : "Unknown");
                    statusText.setText(status != null ? String.valueOf(status) : "N/A");
                    resetInactiveTimer(deviceId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                deviceStatusText.setText("Error loading data");
                statusText.setText("Error loading data");
            }
        };

        realtimeDb.addValueEventListener(deviceListener);
    }

    private void resetInactiveTimer(String deviceId) {
        handler.removeCallbacks(checkStatusRunnable);
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                deviceStatusText.setText("Inactive");
                realtimeDb.child("deviceStatus").setValue("Inactive");
            }
        };
        handler.postDelayed(checkStatusRunnable, INACTIVITY_THRESHOLD);
    }
}
