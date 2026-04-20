package com.sehatsaathi.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;
import com.sehatsaathi.data.local.DatabaseHelper;

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
        historyContainer.removeAllViews(); // Clear before inflation

        DatabaseHelper dbHelper = new DatabaseHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_PATIENTS, null, null, null, null, null, null);

        try {
            LayoutInflater inflater = LayoutInflater.from(this);
            
            if (cursor.moveToLast()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_NAME));
                    String pId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_UNIQUE_ID));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DATE));
                    String diagnosis = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_DIAGNOSIS));
                    String confidence = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_CONFIDENCE));
                
                    View patientRow = inflater.inflate(R.layout.item_patient_row, historyContainer, false);
                    
                    TextView tvPatientName = patientRow.findViewById(R.id.tvPatientName);
                    TextView tvPatientId = patientRow.findViewById(R.id.tvPatientId);
                    TextView tvDate = patientRow.findViewById(R.id.tvDate);
                    TextView tvDiagnosis = patientRow.findViewById(R.id.tvDiagnosis);

                    tvPatientName.setText(name != null ? name : "Unknown Patient");
                    if (pId != null && !pId.isEmpty()) {
                        tvPatientId.setVisibility(View.VISIBLE);
                        tvPatientId.setText("ID: " + pId);
                    } else {
                        tvPatientId.setVisibility(View.GONE);
                    }
                    tvDate.setText("Date: " + (date != null ? date : "Unknown Date"));
                    tvDiagnosis.setText((diagnosis != null ? diagnosis : "Unknown Diagnosis") + " (" + (confidence != null ? confidence : "92%") + ")");
                    
                    historyContainer.addView(patientRow);
                } while (cursor.moveToPrevious());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }
}
