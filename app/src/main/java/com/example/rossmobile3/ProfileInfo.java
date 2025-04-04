package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class ProfileInfo extends AppCompatActivity {

    private TextView emailInfo;
    private EditText fullnameInfo;
    private Button saveBtn, backbutton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        // Initialize AuthManager
        authManager = new AuthManager(this);

        // UI Elements
        emailInfo = findViewById(R.id.emailInfo);
        fullnameInfo = findViewById(R.id.fullnameInfo);
        saveBtn = findViewById(R.id.saveBtn);
        backbutton = findViewById(R.id.backbutton);

        // Load profile data
        loadUserProfile();

        // Save Button Click
        saveBtn.setOnClickListener(v -> saveUserInfo());

        // Back Button
        backbutton.setOnClickListener(v -> finish());
    }

    private void loadUserProfile() {
        authManager.getProfileInfo(new AuthManager.ProfileCallback() {
            @Override
            public void onSuccess(String email, String name) {
                emailInfo.setText(email);
                if (name != null) {
                    fullnameInfo.setText(name);
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileInfo.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo() {
        String fullname = fullnameInfo.getText().toString().trim();
        if (fullname.isEmpty()) {
            fullnameInfo.setError("Enter full name");
            return;
        }

        authManager.updateProfileInfo(fullname, new AuthManager.AuthCallback() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(ProfileInfo.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(ProfileInfo.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
