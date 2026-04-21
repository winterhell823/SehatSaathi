package com.sehatsaathi.ui;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;
import com.sehatsaathi.ui.diagnostic.DiagnosticMainActivity;

public class PatientIntakeActivity extends AppCompatActivity {

    private String selectedGender = "Male";
    private TextView btnMale, btnFemale, btnOther;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_intake);

        View startButton = findViewById(R.id.btnStart);
        if (startButton != null) {
            startButton.setOnClickListener(v -> {
                // Launch DiagnosticMainActivity with ViewPager fragments
                startActivity(new Intent(this, com.sehatsaathi.ui.diagnostic.DiagnosticMainActivity.class));
            });
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
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
