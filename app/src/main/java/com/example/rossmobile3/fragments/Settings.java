package com.example.rossmobile3.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.rossmobile3.AuthManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.rossmobile3.R;

public class Settings extends Fragment {

    AuthManager authManager;
    FirebaseAuth mAuth;

    DatabaseReference deviceStatusRef;
    Handler handler = new Handler(Looper.getMainLooper());
    Runnable checkStatusRunnable;

    Button logoutBtn;
    View view;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(requireActivity());
        mAuth = FirebaseAuth.getInstance();

        view = inflater.inflate(R.layout.fragment_settings, container, false);

        logoutBtn = view.findViewById(R.id.logoutBtn);

        logoutBtn.setOnClickListener(view -> authManager.logoutUser());

        return view;

    }
}