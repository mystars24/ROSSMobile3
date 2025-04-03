package com.example.rossmobile3.fragments;

import android.content.Intent;
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

import com.example.rossmobile3.DeviceInfo;
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
    private Runnable refreshRunnable;
    private Map<String, View> deviceViews = new HashMap<>(); // Track device views

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_devices, container, false);

        deviceContainer = view.findViewById(R.id.deviceContainer);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        realtimeDb = FirebaseDatabase.getInstance().getReference("device");

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
                                    updateDeviceView(deviceId);
                                }
                            }
                        }
                    }
                });
    }

    private void updateDeviceView(String deviceId) {
        View deviceView = deviceViews.get(deviceId);

        if (deviceView == null) {
            deviceView = LayoutInflater.from(getContext()).inflate(R.layout.device_item, deviceContainer, false);
            TextView deviceIdText = deviceView.findViewById(R.id.deviceId);

            deviceIdText.setText("Device: " + deviceId);

            deviceView.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), DeviceInfo.class);
                intent.putExtra("deviceId", deviceId);
                startActivity(intent);
            });

            deviceContainer.addView(deviceView);
            deviceViews.put(deviceId, deviceView);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacks(refreshRunnable);
        deviceViews.clear();
    }
}
