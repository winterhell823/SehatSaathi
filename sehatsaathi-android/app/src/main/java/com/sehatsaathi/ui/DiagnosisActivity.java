package com.sehatsaathi.ui;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;
import com.sehatsaathi.data.local.DatabaseHelper;

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
            String patientName = getIntent().getStringExtra("PATIENT_NAME");
            if (patientName == null || patientName.isEmpty()) patientName = "Unknown Patient (Demo)";
            String uniqueId = "PT-" + String.format(Locale.getDefault(), "%04d", (int)(Math.random() * 10000));
            String dateStr = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_UNIQUE_ID, uniqueId);
            values.put(DatabaseHelper.COL_NAME, patientName);
            values.put(DatabaseHelper.COL_DIAGNOSIS, "Contact Dermatitis");
            values.put(DatabaseHelper.COL_CONFIDENCE, "92%");
            values.put(DatabaseHelper.COL_DATE, dateStr);
            
            long result = db.insert(DatabaseHelper.TABLE_PATIENTS, null, values);
            db.close();
            
            if(result != -1) {
                Toast.makeText(this, "Record saved and synced securely!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to save record locally.", Toast.LENGTH_SHORT).show();
            }
            
            // Navigate straight to History
            startActivity(new Intent(this, PatientHistoryActivity.class));
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save record.", Toast.LENGTH_SHORT).show();
        }
    }
}
