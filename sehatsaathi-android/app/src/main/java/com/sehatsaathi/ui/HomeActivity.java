package com.sehatsaathi.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        findViewById(R.id.btnStartDiagnosis).setOnClickListener(v -> {
            startActivity(new Intent(this, DiagnosisActivity.class));
        });

        findViewById(R.id.tvViewAllPatients).setOnClickListener(v -> {
            startActivity(new Intent(this, PatientHistoryActivity.class));
        });

        findViewById(R.id.cardPatient1).setOnClickListener(v -> {
            startActivity(new Intent(this, PatientHistoryActivity.class));
        });

        findViewById(R.id.cardPatient2).setOnClickListener(v -> {
            startActivity(new Intent(this, PatientHistoryActivity.class));
        });

        findViewById(R.id.cardPatient3).setOnClickListener(v -> {
            startActivity(new Intent(this, PatientHistoryActivity.class));
        });

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_patients) {
                startActivity(new Intent(this, PatientHistoryActivity.class));
                return true;
            } else if (itemId == R.id.nav_tools) {
                startActivity(new Intent(this, DiagnosisActivity.class));
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            }
            return false;
        });
    }
}