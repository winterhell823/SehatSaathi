package com.sehatsaathi.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

public class SettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
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
                    startActivity(new Intent(this, DiagnosisActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            });
        }
    }
}
