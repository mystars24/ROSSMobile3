package com.example.rossmobile3;

import android.content.Context;
import android.widget.Toast;

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
    private static final String SENDER_EMAIL = "rosstars2425@gmail.com";  // Your email
    private static final String SENDER_PASSWORD = "sfwh wvlr evnj jjii";  // App password

    public AuthManager(Context context) {
        this.auth = FirebaseAuth.getInstance();
        this.db = FirebaseFirestore.getInstance();
        this.context = context;
    }

    // ✅ Register User with OTP
    public void registerUser(String email, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, "TemporaryPass123")
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String otp = generateOTP();
                        saveOTPToFirebase(email, otp);  // Save OTP to Firebase
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

    public void sendForgotPasswordOTP(String email, AuthCallback callback) {
        String otp = generateOTP();
        saveOTPToFirebase(email, otp);
        sendOTPEmail(email, otp, callback);
    }

    public interface AuthCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }
}
