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
        String userEmail = auth.getCurrentUser().getEmail();

        Log.d("UserDevices", "Fetching user devices for: " + userEmail);

        db.collection("users").whereEqualTo("email", userEmail)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Log.d("UserDevices", "User found: " + document.getId());

                            document.getReference().collection("devices").get()
                                    .addOnCompleteListener(deviceTask -> {
                                        if (deviceTask.isSuccessful() && deviceTask.getResult() != null) {
                                            for (QueryDocumentSnapshot deviceDoc : deviceTask.getResult()) {
                                                String deviceId = deviceDoc.getString("device");
                                                Log.d("UserDevices", "Device found: " + deviceId);

                                                if (deviceId != null) {
                                                    fetchDeviceStatus(deviceId);
                                                }
                                            }
                                        } else {
                                            Log.e("UserDevices", "Error fetching user devices", deviceTask.getException());
                                        }
                                    });
                        }
                    } else {
                        Log.e("UserDevices", "User not found", task.getException());
                    }
                });
    }


    private void fetchDeviceStatus(String deviceId) {
        Log.d("UserDevices", "Fetching status for device: " + deviceId);

        realtimeDb.child(deviceId).child("deviceStatus").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String deviceStatus = snapshot.getValue(String.class);
                    Log.d("UserDevices", "Device " + deviceId + " status: " + deviceStatus);
                    addDeviceView(deviceId, deviceStatus);
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


    private void addDeviceView(String deviceId, String deviceStatus) {
        View deviceView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, deviceContainer, false);

        TextView deviceIdText = deviceView.findViewById(R.id.deviceId);
        TextView deviceStatusText = deviceView.findViewById(R.id.deviceStatus);

        deviceIdText.setText("Device: " + deviceId);
        deviceStatusText.setText("Status: " + deviceStatus);

        deviceContainer.addView(deviceView); // Dynamically add device to list
    }
}
