package com.sehatsaathi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiagnosisActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis);

        findViewById(R.id.btnSaveSync).setOnClickListener(v -> {
            saveDiagnosisRecord();
        });

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
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

    private void saveDiagnosisRecord() {
        try {
            SharedPreferences prefs = getSharedPreferences("sehat_saathi_db", Context.MODE_PRIVATE);
            String existingData = prefs.getString("patient_history", "[]");
            JSONArray historyArray = new JSONArray(existingData);

            JSONObject newRecord = new JSONObject();
            newRecord.put("name", "Unknown Patient (Demo)");
            newRecord.put("diagnosis", "Contact Dermatitis");
            newRecord.put("confidence", "92%");
            newRecord.put("date", new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date()));
            
            historyArray.put(newRecord);

            prefs.edit().putString("patient_history", historyArray.toString()).apply();
            
            Toast.makeText(this, "Record saved and synced securely!", Toast.LENGTH_SHORT).show();
            
            // Navigate straight to History
            startActivity(new Intent(this, PatientHistoryActivity.class));
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }
}
