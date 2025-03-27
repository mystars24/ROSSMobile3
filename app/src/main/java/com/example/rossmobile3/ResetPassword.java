package com.example.rossmobile3;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ResetPassword extends AppCompatActivity {

    private EditText resetPassword, confirmResetPassword;
    private Button resetPasswordBtn;
    private AuthManager authManager;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_reset_password);

        resetPassword = findViewById(R.id.resetPassword);
        confirmResetPassword = findViewById(R.id.confirmresetPassword);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        authManager = new AuthManager(this);

        email = getIntent().getStringExtra("email");

        resetPasswordBtn.setOnClickListener(v -> {
            String newPassword = resetPassword.getText().toString().trim();
            String confirmPassword = confirmResetPassword.getText().toString().trim();

            if (newPassword.equals(confirmPassword) && !newPassword.isEmpty()) {
                authManager.updatePassword(email, newPassword, authCallback);
            } else {
                Toast.makeText(ResetPassword.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private final AuthManager.AuthCallback authCallback = new AuthManager.AuthCallback() {
        @Override
        public void onSuccess(String message) {
            Toast.makeText(ResetPassword.this, message, Toast.LENGTH_SHORT).show();
            finish(); // Close activity after successful password reset
        }

        @Override
        public void onFailure(String errorMessage) {
            Toast.makeText(ResetPassword.this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    };
}
