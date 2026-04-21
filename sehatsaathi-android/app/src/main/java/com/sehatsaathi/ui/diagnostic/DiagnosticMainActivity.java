package com.sehatsaathi.ui.diagnostic;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.sehatsaathi.R;

public class DiagnosticMainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_main);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new DiagnosticPagerAdapter(this));
        // Disable swiping - navigation is button-driven
        viewPager.setUserInputEnabled(false);
    }

    /**
     * Called from DiagnosticHubFragment to move to symptom clarification
     */
    public void nextPage() {
        if (viewPager.getCurrentItem() < 2) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
        }
    }

    /**
     * Called from SymptomClarificationFragment to go to final summary
     */
    public void goToSummary() {
        // Pass patient info to DiagnosisActivity to save history
        String patientName = getIntent().getStringExtra("PATIENT_NAME");
        String patientAge = getIntent().getStringExtra("PATIENT_AGE");
        String patientGender = getIntent().getStringExtra("PATIENT_GENDER");
        String patientSymptoms = getIntent().getStringExtra("PATIENT_SYMPTOMS");

        Intent intent = new Intent(this, com.sehatsaathi.ui.DiagnosisActivity.class);
        intent.putExtra("PATIENT_NAME", patientName);
        intent.putExtra("PATIENT_AGE", patientAge);
        intent.putExtra("PATIENT_GENDER", patientGender);
        intent.putExtra("PATIENT_SYMPTOMS", patientSymptoms);
        startActivity(intent);
    }
}