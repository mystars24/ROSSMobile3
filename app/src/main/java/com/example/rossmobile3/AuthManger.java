package com.example.rossmobile3;

import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthManger {
    private static final String TAG = "AuthManager";

    FirebaseAuth auth = FirebaseAuth.getInstance();

    public void registerUser(String email) {
        auth.createUserWithEmailAndPassword(email, "TemporaryPass123")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification()
                                    .addOnCompleteListener(verificationTask -> {
                                        if (verificationTask.isSuccessful()) {
                                            Toast.makeText(this, "OTP sent to email.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(this, "Failed to send OTP.", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else {
                        Toast.makeText(this, "Registration failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
