package com.example.rossmobile3.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.rossmobile3.AuthManager;
import com.example.rossmobile3.ProfileInfo;
import com.example.rossmobile3.R;
import com.google.firebase.auth.FirebaseAuth;

public class Settings extends Fragment {

    AuthManager authManager;
    FirebaseAuth mAuth;
    Handler handler = new Handler(Looper.getMainLooper());
    Button logoutBtn;

    TextView profileBtn;
    View view;

    @SuppressLint("WrongViewCast")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(requireActivity());
        mAuth = FirebaseAuth.getInstance();
        view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Profile button -> ProfileInfo Activity
        profileBtn = view.findViewById(R.id.profileBtn);
        profileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ProfileInfo.class);
                startActivity(intent);
            }
        });

        // Logout button
        logoutBtn = view.findViewById(R.id.logoutBtn);
        logoutBtn.setOnClickListener(view -> authManager.logoutUser());

        return view;
    }
}
