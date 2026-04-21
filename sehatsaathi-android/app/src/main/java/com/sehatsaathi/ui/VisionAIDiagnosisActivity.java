package com.sehatsaathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

public class VisionAIDiagnosisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vision_ai);

        View proceedButton = findViewById(R.id.btnProceed);
        if (proceedButton != null) {
            proceedButton.setOnClickListener(v -> {
                // Navigate to Final Diagnosis Result screen
                Intent intent = new Intent(this, DiagnosisActivity.class);
                startActivity(intent);
            });
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_tools);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_patients) {
                    startActivity(new Intent(this, PatientHistoryActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tools) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, HealthCentreProfileActivity.class));
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
}
