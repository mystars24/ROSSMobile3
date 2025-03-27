package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterPassword extends AppCompatActivity {

    private EditText password, confirmPassword;
    private Button register;
    private FirebaseAuth auth;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_password);

        password = findViewById(R.id.registerPassword);
        confirmPassword = findViewById(R.id.confirmPassword);
        register = findViewById(R.id.registerButton);
        auth = FirebaseAuth.getInstance();

        // Get email from intent
        email = getIntent().getStringExtra("email");

        register.setOnClickListener(v -> setNewPassword());
    }

    private void setNewPassword() {
        String newPassword = password.getText().toString().trim();
        String confirmNewPassword = confirmPassword.getText().toString().trim();

        if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
            Toast.makeText(this, "Please enter both password fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(confirmNewPassword)) {
            Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(RegisterPassword.this, "User Created Successfully.", Toast.LENGTH_SHORT).show();

                    // Redirect to Login screen
                    Intent intent = new Intent(RegisterPassword.this, Login.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RegisterPassword.this, "Failed to update password.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not found. Please sign in again.", Toast.LENGTH_SHORT).show();
        }
    }
}
