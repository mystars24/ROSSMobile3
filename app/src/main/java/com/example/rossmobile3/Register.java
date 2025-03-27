package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Register extends AppCompatActivity {

    private TextView login;
    private EditText emailInput;
    private Button signup;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        emailInput = findViewById(R.id.newEmail);
        signup = findViewById(R.id.signupBtn);
        login = findViewById(R.id.loginText);
        authManager = new AuthManager(this);

        signup.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Register.this, "Enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.registerUser(email, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String otp) {  // ✅ Corrected! Now it matches AuthCallback
                    Intent intent = new Intent(Register.this, RegisterOTP.class);
                    intent.putExtra("email", email);
                    intent.putExtra("otp", otp); // Pass OTP if needed
                    startActivity(intent);
                }

                @Override
                public void onFailure(String error) {
                    Toast.makeText(Register.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        });

        login.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
        });
    }
}
