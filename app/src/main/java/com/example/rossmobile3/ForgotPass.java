package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class ForgotPass extends AppCompatActivity {

    private EditText forgotEmail;
    private Button verifyEmail;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_pass);

        forgotEmail = findViewById(R.id.forgotEmail);
        verifyEmail = findViewById(R.id.forgotBtn);

        authManager = new AuthManager(this);

        verifyEmail.setOnClickListener(v -> {
            String email = forgotEmail.getText().toString().trim();
            if (!email.isEmpty()) {
                authManager.sendForgotPasswordOTP(email, new AuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(String message) {
                        Toast.makeText(ForgotPass.this, message, Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ForgotPass.this, ForgotOTP.class);
                        intent.putExtra("email", email);
                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Toast.makeText(ForgotPass.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(ForgotPass.this, "Enter your registered email", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
