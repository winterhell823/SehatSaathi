package com.sehatsaathi.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class PatientHistoryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_history);

        loadPatientHistory();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_patients);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class));
                    finish();
                    return true;
                } else if (itemId == R.id.nav_patients) {
                    return true;
                } else if (itemId == R.id.nav_tools) {
                    startActivity(new Intent(this, PatientIntakeActivity.class));
                    finish();
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

    private void loadPatientHistory() {
        LinearLayout historyContainer = findViewById(R.id.historyContainer);
        SharedPreferences prefs = getSharedPreferences("sehat_saathi_db", Context.MODE_PRIVATE);
        String existingData = prefs.getString("patient_history", "[]");

        try {
            JSONArray historyArray = new JSONArray(existingData);
            LayoutInflater inflater = LayoutInflater.from(this);
            
            // Show empty state if needed OR inflate existing items. We'll populate from newest (last) to oldest
            for (int i = historyArray.length() - 1; i >= 0; i--) {
                JSONObject record = historyArray.getJSONObject(i);
                
                View patientRow = inflater.inflate(R.layout.item_patient_row, historyContainer, false);
                
                TextView tvPatientName = patientRow.findViewById(R.id.tvPatientName);
                TextView tvPatientId = patientRow.findViewById(R.id.tvPatientId);
                TextView tvDate = patientRow.findViewById(R.id.tvDate);
                TextView tvDiagnosis = patientRow.findViewById(R.id.tvDiagnosis);

                tvPatientName.setText(record.optString("name", "Unknown Patient"));
                String pId = record.optString("id", "");
                if (!pId.isEmpty()) {
                    tvPatientId.setVisibility(View.VISIBLE);
                    tvPatientId.setText("ID: " + pId);
                } else {
                    tvPatientId.setVisibility(View.GONE);
                }
                tvDate.setText("Date: " + record.optString("date", "Unknown Date"));
                tvDiagnosis.setText(record.optString("diagnosis", "Unknown Diagnosis") + " (" + record.optString("confidence", "92%") + ")");
                
                historyContainer.addView(patientRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
