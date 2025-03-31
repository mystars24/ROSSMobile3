package com.example.rossmobile3.fragments;

import android.os.Bundle;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_devices, container, false);

        deviceContainer = view.findViewById(R.id.deviceContainer);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference("bin"); // Root path for devices

        loadUserDevices(); // Fetch devices when view is created

        return view;
    }

    private void loadUserDevices() {
        String userEmail = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        Log.d("UserDevices", "User Email: " + userEmail);

        if (userEmail == null) {
            Log.e("UserDevices", "No logged-in user.");
            return;
        }

        Log.d("UserDevices", "Fetching user devices for: " + userEmail);

        db.collection("users").whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Log.d("UserDevices", "User found: " + task.getResult().getDocuments().size());

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("device")) {
                                String deviceId = document.getString("device");
                                Log.d("UserDevices", "Device ID: " + deviceId);
                                if (deviceId != null) {
                                    fetchDeviceStatus(deviceId);
                                }
                            } else {
                                Log.e("UserDevices", "No device associated with this user.");
                            }
                        }
                    } else {
                        Log.e("UserDevices", "No user found in Firestore", task.getException());
                    }
                });
    }

    private void fetchDeviceStatus(String deviceId) {
        Log.d("UserDevices", "Fetching status for device: " + deviceId);

        realtimeDb.child(deviceId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deviceStatus = snapshot.child("deviceStatus").getValue(String.class);
                    String fillLevel = snapshot.child("fillLevel").getValue() != null ?
                            snapshot.child("fillLevel").getValue().toString() : "N/A";

                    Log.d("UserDevices", "Device " + deviceId + " status: " + deviceStatus);
                    Log.d("UserDevices", "Device " + deviceId + " fill level: " + fillLevel);

                    addDeviceView(deviceId, deviceStatus, fillLevel);
                } else {
                    Log.w("UserDevices", "Device " + deviceId + " not found in Realtime DB");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("UserDevices", "Error fetching device status", error.toException());
            }
        });
    }

    private void addDeviceView(String deviceId, String deviceStatus, String fillLevel) {
        View deviceView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, deviceContainer, false);

        TextView deviceIdText = deviceView.findViewById(R.id.deviceId);
        TextView deviceStatusText = deviceView.findViewById(R.id.deviceStatus);
//        TextView fillLevelText = deviceView.findViewById(R.id.fillLevel); // Make sure your layout has this TextView

        deviceIdText.setText("Device: " + deviceId);
        deviceStatusText.setText("Status: " + deviceStatus);
//        fillLevelText.setText("Fill Level: " + fillLevel);

        deviceContainer.addView(deviceView);

        Log.d("UserDevices", "Device view added: " + deviceId);
        Log.d("UserDevices", "Total Views: " + deviceContainer.getChildCount());
    }
}
