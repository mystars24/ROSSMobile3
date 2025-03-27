package com.example.rossmobile3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegisterOTP extends AppCompatActivity {

    private EditText OTP;
    private Button OTPBtn;
    private String email; // Store the email for lookup
    private FirebaseFirestore db; // Firestore instance

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_otp);

        OTP = findViewById(R.id.newOTP);
        OTPBtn = findViewById(R.id.regOTPBtn);
        db = FirebaseFirestore.getInstance();

        // Get email from Intent
        email = getIntent().getStringExtra("email");

        OTPBtn.setOnClickListener(v -> verifyOTP());
    }

    private void verifyOTP() {
        String enteredOTP = OTP.getText().toString().trim();

        if (enteredOTP.isEmpty()) {
            Toast.makeText(this, "Please enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch correct OTP from Firestore
        DocumentReference otpRef = db.collection("otp_codes").document(email);
        otpRef.get().addOnSuccessListener(document -> {
            if (document.exists() && document.contains("otp")) {
                String correctOTP = document.getString("otp");

                if (enteredOTP.equals(correctOTP)) {
                    Toast.makeText(this, "OTP Verified. Set your password.", Toast.LENGTH_SHORT).show();

                    // Delete OTP after verification (Security best practice)
                    otpRef.delete();

                    // Navigate to password setup
                    Intent intent = new Intent(this, RegisterPassword.class);
                    intent.putExtra("email", email); // Pass email to next activity
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(this, "Invalid OTP. Try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "OTP expired or not found.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Error verifying OTP", Toast.LENGTH_SHORT).show());
    }
}
