package com.sehatsaathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.sehatsaathi.R;

public class LoginActivity extends AppCompatActivity {

    private static final String CORRECT_ID = "admin";
    private static final String CORRECT_PASS = "123456";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText etHealthId = findViewById(R.id.etHealthId);
        EditText etPassword = findViewById(R.id.etPassword);
        
        findViewById(R.id.btnLogin).setOnClickListener(v -> {
            String enteredId = etHealthId.getText().toString().trim();
            String enteredPass = etPassword.getText().toString().trim();
            
            if (CORRECT_ID.equals(enteredId) && CORRECT_PASS.equals(enteredPass)) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Invalid credentials. Unauthorized access restricted.", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btnFingerprint).setOnClickListener(v -> {
            // For hackathon/demo fallback logic
            if (etHealthId.getText().toString().trim().equals(CORRECT_ID)) {
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Please enter valid ID first, or use biometrics (if enrolled).", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
