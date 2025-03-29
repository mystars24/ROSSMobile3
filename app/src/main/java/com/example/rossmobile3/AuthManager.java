package com.example.rossmobile3;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.MessagingException;

public class AuthManager {
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private Context context;
    private FirebaseAuth mAuth;
    private static final String SENDER_EMAIL = "rosstars2425@gmail.com";  // Your email
    private static final String SENDER_PASSWORD = "sfwh wvlr evnj jjii";  // App password

    public AuthManager(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
        this.mAuth = FirebaseAuth.getInstance();
    }

    // ✅ Register User with OTP
    public void registerUser(String email, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, "TemporaryPass123")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String userId = user.getUid();
                            saveUserToFirestore(userId, email); // Save email to Firestore
                        }

                        String otp = generateOTP();
                        saveOTPToFirebase(email, otp); // Save OTP to Firestore
                        sendOTPEmail(email, otp, callback);
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            callback.onFailure("Email is already registered. Please use another email.");
                        } else {
                            callback.onFailure("Registration failed. " + task.getException().getMessage());
                        }
                    }
                });
    }

    private void saveUserToFirestore(String userId, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);

        db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "User registered successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    // ✅ Login User
    public void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            callback.onSuccess("Login successful.");
                        } else {
                            callback.onFailure("User not found.");
                        }
                    } else {
                        callback.onFailure("Login failed: " + task.getException().getMessage());
                    }
                });
    }

    private void saveOTPToFirebase(String email, String otp) {
        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", otp);
        otpData.put("timestamp", System.currentTimeMillis());

        db.collection("otp_codes").document(email)
                .set(otpData)
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});
    }

    private String generateOTP() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(999999)); // Generate 6-digit OTP
    }

    public void sendOTPEmail(String recipientEmail, String otp, AuthCallback callback) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                MailSender sender = new MailSender(SENDER_EMAIL, SENDER_PASSWORD);
                String subject = "Your OTP Code";
                String message = "Your OTP is: " + otp + "\nIt will expire in 5 minutes.";

                sender.sendEmail(recipientEmail, subject, message);
                callback.onSuccess("OTP sent successfully."); // No need to pass OTP here
            } catch (MessagingException e) {
                callback.onFailure("Failed to send OTP.");
            }
        });
    }

    public void checkEmailVerification(AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            user.reload().addOnCompleteListener(task -> {
                if (user.isEmailVerified()) {
                    callback.onSuccess("Email verified successfully.");
                } else {
                    callback.onFailure("Email not verified. Check your inbox.");
                }
            });
        } else {
            callback.onFailure("No user logged in.");
        }
    }

    public void updatePassword(String email, String newPassword, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, "TemporaryPass123")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.updatePassword(newPassword)
                                    .addOnCompleteListener(passwordTask -> {
                                        if (passwordTask.isSuccessful()) {
                                            updatePasswordStatusInFirestore(user.getUid(), email); // Store email in Firestore
                                            callback.onSuccess("Password updated successfully.");
                                        } else {
                                            callback.onFailure("Failed to update password.");
                                        }
                                    });
                        }
                    } else {
                        callback.onFailure("Authentication failed.");
                    }
                });
    }

    private void updatePasswordStatusInFirestore(String userId, String email) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", email);
        userData.put("passwordSet", true);

        db.collection("users").document(userId)
                .set(userData) // This ensures the email is stored along with passwordSet status
                .addOnSuccessListener(aVoid -> Toast.makeText(context, "Password setup completed!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(context, "Failed to update Firestore: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public void sendForgotPasswordOTP(String email, AuthCallback callback) {
        String otp = generateOTP();
        saveOTPToFirebase(email, otp);
        sendOTPEmail(email, otp, callback);
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    public void logoutUser() {
        if (mAuth != null) {
            mAuth.signOut();
        }
        Toast.makeText(context, "Logout Successfully", Toast.LENGTH_SHORT).show();

        // Ensure context is an activity before starting an Intent
        if (context instanceof AppCompatActivity) {
            Intent intent = new Intent(context, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        } else {
            Log.e("AuthManager", "Context is not an instance of AppCompatActivity");
        }
    }
}
