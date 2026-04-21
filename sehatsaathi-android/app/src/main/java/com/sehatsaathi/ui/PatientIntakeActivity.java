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

        btnMale = findViewById(R.id.btnMale);
        btnFemale = findViewById(R.id.btnFemale);
        btnOther = findViewById(R.id.btnOther);

        btnMale.setOnClickListener(v -> selectGender("Male"));
        btnFemale.setOnClickListener(v -> selectGender("Female"));
        btnOther.setOnClickListener(v -> selectGender("Other"));

        View backBtn = findViewById(R.id.btnBack);
        if (backBtn != null) backBtn.setOnClickListener(v -> finish());

        findViewById(R.id.btnNext).setOnClickListener(v -> {
            String name = ((EditText) findViewById(R.id.etFullName)).getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Please enter patient name", Toast.LENGTH_SHORT).show();
                return;
            }

            String age = "";
            EditText etAge = findViewById(R.id.etAge);
            if (etAge != null) age = etAge.getText().toString().trim();

            String symptoms = "";
            EditText etSymptoms = findViewById(R.id.etSymptoms);
            if (etSymptoms != null) symptoms = etSymptoms.getText().toString().trim();

            // Navigate to Symptom Clarification (DiagnosticMainActivity with ViewPager)
            Intent intent = new Intent(this, DiagnosticMainActivity.class);
            intent.putExtra("PATIENT_NAME", name);
            intent.putExtra("PATIENT_AGE", age);
            intent.putExtra("PATIENT_GENDER", selectedGender);
            intent.putExtra("PATIENT_SYMPTOMS", symptoms);
            startActivity(intent);
        });

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

    private void selectGender(String gender) {
        selectedGender = gender;
        btnMale.setBackgroundResource(gender.equals("Male") ? R.drawable.bg_gender_selected : R.drawable.bg_gender_unselected);
        btnMale.setTextColor(gender.equals("Male") ? 0xFFFFFFFF : 0xFF5D4037);
        btnFemale.setBackgroundResource(gender.equals("Female") ? R.drawable.bg_gender_selected : R.drawable.bg_gender_unselected);
        btnFemale.setTextColor(gender.equals("Female") ? 0xFFFFFFFF : 0xFF5D4037);
        btnOther.setBackgroundResource(gender.equals("Other") ? R.drawable.bg_gender_selected : R.drawable.bg_gender_unselected);
        btnOther.setTextColor(gender.equals("Other") ? 0xFFFFFFFF : 0xFF5D4037);
    }
}
