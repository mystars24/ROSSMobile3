package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ForgotOTP extends AppCompatActivity {

    private EditText forgotOTPcode;
    private Button resetPasswordBtn;
    private String email;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_otp);

        forgotOTPcode = findViewById(R.id.forgotOTPcode);
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn);
        db = FirebaseFirestore.getInstance();

        email = getIntent().getStringExtra("email");

        resetPasswordBtn.setOnClickListener(v -> {
            String enteredOTP = forgotOTPcode.getText().toString().trim();

            if (!enteredOTP.isEmpty()) {
                verifyOTP(enteredOTP);
            } else {
                Toast.makeText(ForgotOTP.this, "Enter OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOTP(String enteredOTP) {
        db.collection("otp_codes").document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String correctOTP = documentSnapshot.getString("otp");
                        if (enteredOTP.equals(correctOTP)) {
                            Toast.makeText(ForgotOTP.this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(ForgotOTP.this, ResetPassword.class);
                            intent.putExtra("email", email);
                            startActivity(intent);
                        } else {
                            Toast.makeText(ForgotOTP.this, "Invalid OTP", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ForgotOTP.this, "No OTP found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(ForgotOTP.this, "Error verifying OTP", Toast.LENGTH_SHORT).show());
    }
}
