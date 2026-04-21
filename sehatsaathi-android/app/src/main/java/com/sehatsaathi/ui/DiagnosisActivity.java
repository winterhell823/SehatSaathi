package com.sehatsaathi.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sehatsaathi.R;
import com.sehatsaathi.data.local.DatabaseHelper;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DiagnosisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis);

        Intent intent = getIntent();

        // ── Read RAG results from intent ───────────────────────────────────────
        String patientName  = intent.getStringExtra("PATIENT_NAME");
        String diagnosisName = intent.getStringExtra("DIAGNOSIS_NAME");
        int confidence       = intent.getIntExtra("DIAGNOSIS_CONFIDENCE", 0);
        String reasoning     = intent.getStringExtra("DIAGNOSIS_REASONING");
        String treatment     = intent.getStringExtra("DIAGNOSIS_TREATMENT");
        String aiMessage     = intent.getStringExtra("DIAGNOSIS_MESSAGE");
        boolean referral     = intent.getBooleanExtra("REFERRAL_NEEDED", false);
        String referralReason= intent.getStringExtra("REFERRAL_REASON");

        if (patientName == null || patientName.isEmpty()) patientName = "Unknown Patient";
        if (diagnosisName == null || diagnosisName.isEmpty()) diagnosisName = "Awaiting Analysis";

        // ── Bind to views ──────────────────────────────────────────────────────
        bindText(R.id.tvConditionName,     diagnosisName);
        bindText(R.id.tvConfidenceScore,   confidence + "% Confidence Score");
        bindText(R.id.tvReasoning,         reasoning != null ? reasoning : aiMessage);
        bindText(R.id.tvTreatment,         treatment);
        bindText(R.id.tvPatientHeader,     "Patient: " + patientName);

        // Confidence progress bar
        View progressBar = findViewById(R.id.confidenceBar);
        if (progressBar instanceof android.widget.ProgressBar) {
            ((android.widget.ProgressBar) progressBar).setProgress(confidence);
        }

        // Referral card
        View referralCard = findViewById(R.id.referralCard);
        if (referralCard != null) referralCard.setVisibility(referral ? View.VISIBLE : View.GONE);
        bindText(R.id.tvReferralReason, referralReason);

        // Red flags list
        String redFlagsJson = intent.getStringExtra("RED_FLAGS_JSON");
        bindList(R.id.containerRedFlags, redFlagsJson, "• ");

        // Next steps list
        String nextStepsJson = intent.getStringExtra("NEXT_STEPS_JSON");
        bindList(R.id.containerNextSteps, nextStepsJson, "→ ");

        // ── Save to SQLite history ─────────────────────────────────────────────
        saveDiagnosisRecord(patientName, diagnosisName, confidence + "%");

        // ── Bottom navigation ──────────────────────────────────────────────────
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_tools);
            bottomNav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_home) {
                    startActivity(new Intent(this, HomeActivity.class)); finish(); return true;
                } else if (id == R.id.nav_patients) {
                    startActivity(new Intent(this, PatientHistoryActivity.class)); finish(); return true;
                } else if (id == R.id.nav_tools) {
                    return true;
                } else if (id == R.id.nav_profile) {
                    startActivity(new Intent(this, HealthCentreProfileActivity.class)); finish(); return true;
                }
                return false;
            });
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private void bindText(int viewId, String text) {
        View v = findViewById(viewId);
        if (v instanceof TextView && text != null) ((TextView) v).setText(text);
    }

    private void bindList(int containerId, String json, String prefix) {
        if (json == null) return;
        LinearLayout container = findViewById(containerId);
        if (container == null) return;
        container.removeAllViews();
        try {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> items = new Gson().fromJson(json, listType);
            if (items == null) return;
            for (String item : items) {
                TextView tv = new TextView(this);
                tv.setText(prefix + item);
                tv.setTextSize(13f);
                tv.setPadding(0, 8, 0, 8);
                tv.setTextColor(0xFF4E463D);
                container.addView(tv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDiagnosisRecord(String name, String diagnosis, String confidence) {
        try {
            String uniqueId = "PAT" + System.currentTimeMillis();
            String dateStr  = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(new Date());

            DatabaseHelper dbHelper = new DatabaseHelper(this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues values = new ContentValues();
            values.put(DatabaseHelper.COL_UNIQUE_ID,  uniqueId);
            values.put(DatabaseHelper.COL_NAME,       name);
            values.put(DatabaseHelper.COL_DIAGNOSIS,  diagnosis);
            values.put(DatabaseHelper.COL_CONFIDENCE, confidence);
            values.put(DatabaseHelper.COL_DATE,       dateStr);

            long result = db.insert(DatabaseHelper.TABLE_PATIENTS, null, values);
            db.close();
            if (result != -1) Toast.makeText(this, "Record saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
