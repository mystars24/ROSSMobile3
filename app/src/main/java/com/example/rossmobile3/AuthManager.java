package com.example.rossmobile3;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import android.app.ProgressDialog;

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
        if (email.isEmpty() || password.isEmpty()) {
            callback.onFailure("Email and password cannot be empty.");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            callback.onFailure("Invalid email format.");
            return;
        }

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Logging in...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressDialog.dismiss(); // Hide loading dialog

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

    public void checkUserDevice(String email, DeviceCheckCallback callback) {
        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        boolean hasDevice = false;

                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            if (document.contains("device")) { // Check if device exists
                                hasDevice = true;
                                break;
                            }
                        }

                        if (hasDevice) {
                            callback.onDeviceFound();
                        } else {
                            callback.onDeviceNotFound();
                        }
                    } else {
                        callback.onDeviceNotFound();
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onError("Failed to check device: " + e.getMessage());
                });
    }

    public interface DeviceCheckCallback {
        void onDeviceFound();
        void onDeviceNotFound();
        void onError(String errorMessage);
    }

    //    Add Device Method
    public void addDevice(String deviceID, AuthCallback callback) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            callback.onFailure("User not authenticated.");
            return;
        }

        String userEmail = user.getEmail();
        DatabaseReference deviceRef = FirebaseDatabase.getInstance().getReference().child("device").child(deviceID);
        Log.d("Firebase", "Checking device path: " + deviceRef.toString());

        deviceRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    DatabaseReference userRef = deviceRef.child("user");

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot userSnapshot) {
                            String currentUser = userSnapshot.getValue(String.class);

                            if (currentUser == null || currentUser.isEmpty()) {
                                // Update Realtime Database
                                userRef.setValue(userEmail)
                                        .addOnSuccessListener(aVoid -> {
                                            // Update Firestore
                                            Map<String, Object> deviceData = new HashMap<>();
                                            deviceData.put("email", userEmail);
                                            deviceData.put("device", deviceID); // Store device ID

                                            db.collection("users").document(user.getUid())
                                                    .set(deviceData)
                                                    .addOnSuccessListener(aVoid1 -> callback.onSuccess("Device added successfully!"))
                                                    .addOnFailureListener(e -> callback.onFailure("Failed to update Firestore: " + e.getMessage()));
                                        })
                                        .addOnFailureListener(e -> callback.onFailure("Failed to update Realtime Database: " + e.getMessage()));
                            } else {
                                callback.onFailure("Device is already registered to another user.");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError error) {
                            callback.onFailure("Error: " + error.getMessage());
                        }
                    });

                } else {
                    callback.onFailure("Device ID does not exist in the system.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                callback.onFailure("Error: " + error.getMessage());
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

    //    OTP Generator
    private String generateOTP() {
        Random random = new Random();
        return String.valueOf(100000 + random.nextInt(900000)); // Generates exactly 6 digits
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

    // ✅ Retrieve user profile info
    public void getProfileInfo(ProfileCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("User not logged in.");
            return;
        }

        String userId = user.getUid();
        String email = user.getEmail();

        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        callback.onSuccess(email, name);
                    } else {
                        callback.onFailure("User profile not found.");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure("Error fetching profile: " + e.getMessage()));
    }

    // ✅ Update user profile info (only name)
    public void updateProfileInfo(String newName, AuthCallback callback) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            callback.onFailure("User not logged in.");
            return;
        }

        String userId = user.getUid();
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("name", newName);

        db.collection("users").document(userId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Profile updated successfully."))
                .addOnFailureListener(e -> callback.onFailure("Failed to update profile: " + e.getMessage()));
    }

    // ✅ Callback interface for profile operations
//    public interface ProfileCallback {
//        void onSuccess(String email, String name);
//        void onFailure(String errorMessage);
//    }


    public interface ProfileCallback {
        void onSuccess(String email, String name);
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