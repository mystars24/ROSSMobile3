package com.example.rossmobile3.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class UserDevices extends Fragment {

    private LinearLayout deviceContainer;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private DatabaseReference realtimeDb;
    private View view;
    private Handler handler = new Handler();
    private Runnable refreshRunnable, checkStatusRunnable;
    private long lastUpdateTime = 0;
    private static final long INACTIVITY_THRESHOLD = 10000; // 10 seconds
    private String lastDeviceStatus = "";
    private TextView deviceStatusText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_devices, container, false);

        deviceContainer = view.findViewById(R.id.deviceContainer);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference("bin"); // Root path for devices

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
        realtimeDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deviceStatus = snapshot.child("deviceStatus").getValue(String.class);
                    String fillLevel = snapshot.child("fillLevel").getValue() != null ?
                            snapshot.child("fillLevel").getValue().toString() : "N/A";

                    lastUpdateTime = System.currentTimeMillis(); // Update last received time
                    lastDeviceStatus = deviceStatus;
                    updateDeviceView(deviceStatus, fillLevel);
                    resetInactiveTimer();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserDevices", "Error fetching device status", error.toException());
            }
        });
    }

    private void updateDeviceView(String deviceStatus, String fillLevel) {
        if (deviceContainer.getChildCount() == 0) {
            View deviceView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, deviceContainer, false);
            deviceContainer.addView(deviceView);
            deviceStatusText = deviceView.findViewById(R.id.deviceStatus);
        } else {
            View deviceView = deviceContainer.getChildAt(0);
            deviceStatusText = deviceView.findViewById(R.id.deviceStatus);
        }

        TextView deviceIdText = deviceContainer.getChildAt(0).findViewById(R.id.deviceId);
        deviceIdText.setText("Device: Bin");
        deviceStatusText.setText("Status: " + deviceStatus);
    }

    private void sendInactiveStatusToDatabase() {
        realtimeDb.child("deviceStatus").setValue("Inactive")
                .addOnSuccessListener(aVoid -> Log.d("UserDevices", "Device status set to Inactive"))
                .addOnFailureListener(e -> Log.e("UserDevices", "Failed to update device status", e));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
        handler.removeCallbacks(checkStatusRunnable);
    }

    {
        checkStatusRunnable = new Runnable() {
            @Override
            public void run() {
                lastDeviceStatus = "Inactive";
                if (deviceStatusText != null) {
                    deviceStatusText.setText("Status: Inactive");
                }
                sendInactiveStatusToDatabase();
            }
        };
    }
}
