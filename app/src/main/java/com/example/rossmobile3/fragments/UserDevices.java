package com.example.rossmobile3.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.rossmobile3.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Map;
import java.util.HashMap;

public class UserDevices extends Fragment {

    private LinearLayout deviceContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private DatabaseReference realtimeDb;
    private View view;
    private Handler handler = new Handler();
    private Runnable refreshRunnable, checkStatusRunnable;
    private long lastUpdateTime = 0;
    private String currentDeviceId;
    private static final long INACTIVITY_THRESHOLD = 10000; // 10 seconds
    private String lastDeviceStatus = "";
    private TextView deviceStatusText;
    private Map<String, Long> deviceLastUpdateTimes = new HashMap<>(); // Track multiple devices
    private Map<String, View> deviceViews = new HashMap<>(); // Track device views

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_devices, container, false);

        deviceContainer = view.findViewById(R.id.deviceContainer);
        deviceStatusText = view.findViewById(R.id.deviceStatus);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference("device"); // Updated root path

        loadUserDevices(); // Initial fetch
        startAutoRefresh();

        return view;
    }

    private void startAutoRefresh() {
        refreshRunnable = new Runnable() {
            @Override
            public void run() {
                loadUserDevices();
                handler.postDelayed(this, 2000); // Refresh every 2 seconds
            }
        };
        handler.post(refreshRunnable);
    }

    private void resetInactiveTimer() {
        handler.removeCallbacks(checkStatusRunnable);
        handler.postDelayed(checkStatusRunnable, INACTIVITY_THRESHOLD);
    }

    private void loadUserDevices() {
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (userEmail == null) {
            Log.e("UserDevices", "No logged-in user.");
            return;
        }

        db.collection("users").whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("device")) {
                                String deviceId = document.getString("device");
                                if (deviceId != null) {
                                    fetchDeviceStatus(deviceId);
                                }
                            }
                        }
                    }
                });
    }

    private void fetchDeviceStatus(String deviceId) {
        DatabaseReference deviceRef = realtimeDb.child(deviceId);

        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deviceStatus = snapshot.child("deviceStatus").getValue(String.class);
                    deviceLastUpdateTimes.put(deviceId, System.currentTimeMillis());
                    updateDeviceView(deviceId, deviceStatus);
                    resetInactiveTimer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserDevices", "Error fetching device status", error.toException());
            }
        });
    }
    {
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                for (Map.Entry<String, Long> entry : deviceLastUpdateTimes.entrySet()) {
                    String deviceId = entry.getKey();
                    long lastUpdate = entry.getValue();

                    if (currentTime - lastUpdate > INACTIVITY_THRESHOLD) {
                        markDeviceAsInactive(deviceId);
                    }
                }
                handler.postDelayed(this, 1000); // Check every second
            }
        };
    }
    private void markDeviceAsInactive(String deviceId) {
        // Update Firebase
        realtimeDb.child(deviceId).child("deviceStatus").setValue("Inactive")
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserDevices", deviceId + " marked inactive in Firebase");
                    updateDeviceView(deviceId, "Inactive");
                })
                .addOnFailureListener(e -> Log.e("UserDevices", "Failed to mark device inactive", e));
    }

    private void updateDeviceView(String deviceId, String deviceStatus) {
        View deviceView = deviceViews.get(deviceId);

        if (deviceView == null) {
            // Create new view if doesn't exist
            deviceView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, deviceContainer, false);
            TextView deviceIdText = deviceView.findViewById(R.id.deviceId);
            TextView statusText = deviceView.findViewById(R.id.deviceStatus);

            deviceIdText.setText("Device: " + deviceId);
            statusText.setText("Status: " + deviceStatus);

            deviceContainer.addView(deviceView);
            deviceViews.put(deviceId, deviceView);
        } else {
            // Update existing view
            TextView statusText = deviceView.findViewById(R.id.deviceStatus);
            statusText.setText("Status: " + deviceStatus);

            // Optional: Change color for inactive status
            if ("Inactive".equals(deviceStatus)) {
                statusText.setTextColor(Color.RED);
            } else {
                statusText.setTextColor(Color.BLACK); // Or your default color
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
        handler.removeCallbacks(checkStatusRunnable);
        deviceLastUpdateTimes.clear();
        deviceViews.clear();
    }

    {
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                lastDeviceStatus = "Inactive";
                if (deviceStatusText != null) {
                    deviceStatusText.setText("Status: Inactive");
                }
            }
        };
    }
}