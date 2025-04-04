package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AddDevice extends AppCompatActivity {

    private EditText addDeviceID;
    private Button addBtn, logoutBtn;
    private ImageView settingsBtn;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        addDeviceID = findViewById(R.id.addDeviceID);
        addBtn = findViewById(R.id.addBtn);
        authManager = new AuthManager(this);
        settingsBtn = findViewById(R.id.settingsBtn);

        addBtn.setOnClickListener(v -> {
            String deviceID = addDeviceID.getText().toString().trim();

            if (deviceID.isEmpty()) {
                Toast.makeText(AddDevice.this, "Please enter a device ID", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.addDevice(deviceID, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(AddDevice.this, message, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(AddDevice.this, UserMainDashboard.class));
                    finish();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(AddDevice.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });

        settingsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(AddDevice.this, ProfileInfo.class);
            startActivity(intent);
        });

        logoutBtn = findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(view -> authManager.logoutUser());

    }
}
