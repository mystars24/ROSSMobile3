package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    private Button loginbtn;
    private EditText loginemail, password;
    private TextView register, forgot;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        loginemail = findViewById(R.id.loginEmail);
        password = findViewById(R.id.loginPassword);
        loginbtn = findViewById(R.id.loginBtn);
        register = findViewById(R.id.registerPage);
        forgot = findViewById(R.id.forgotPage);

        authManager = new AuthManager(this);

        loginbtn.setOnClickListener(v -> {
            String email = loginemail.getText().toString().trim();
            String pass = password.getText().toString().trim();

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(Login.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                return;
            }

            authManager.loginUser(email, pass, new AuthManager.AuthCallback() {
                @Override
                public void onSuccess(String message) {
                    Toast.makeText(Login.this, message, Toast.LENGTH_SHORT).show();
                    // Check if the user has a device
                    authManager.checkUserDevice(email, new AuthManager.DeviceCheckCallback() {
                        @Override
                        public void onDeviceFound() {
                            Intent intent = new Intent(Login.this, UserMainDashboard.class);
                            startActivity(intent);
                            finish();
                        }
                        @Override
                        public void onDeviceNotFound() {
                            Intent intent = new Intent(Login.this, AddDevice.class);
                            startActivity(intent);
                            finish();
                        }
                        @Override
                        public void onError(String errorMessage) {
                            Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                @Override
                public void onFailure(String errorMessage) {
                    Toast.makeText(Login.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Register.class);
                startActivity(intent);
            }
        });
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ForgotPass.class);
                startActivity(intent);
            }
        });
    }
}
