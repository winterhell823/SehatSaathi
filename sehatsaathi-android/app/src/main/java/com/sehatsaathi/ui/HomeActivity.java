package com.sehatsaathi.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        View startDiagnosisButton = findViewById(R.id.btnStartDiagnosis);
        if (startDiagnosisButton != null) {
            startDiagnosisButton.setOnClickListener(v -> startActivity(new Intent(this, PatientIntakeActivity.class)));
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    return true;
                } else if (itemId == R.id.nav_patients) {
                    startActivity(new Intent(this, PatientHistoryActivity.class));
                    return true;
                } else if (itemId == R.id.nav_tools) {
                    startActivity(new Intent(this, PatientIntakeActivity.class));
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    startActivity(new Intent(this, HealthCentreProfileActivity.class));
                    return true;
                }
                return false;
            });
        }
    }
}
