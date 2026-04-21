package com.sehatsaathi.ui.diagnostic;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.Gson;
import com.sehatsaathi.R;
import com.sehatsaathi.network.RagModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagnosticMainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;

    // ── RAG session state shared across all fragments ──────────────────────────
    private String patientId = "anonymous";
    private String symptomText = "";
    private String firstQuestion = "";
    private String currentQuestion = "";
    private final List<Object> conversationHistory = new ArrayList<>();
    private RagModels.ChatResponse lastResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_main);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new DiagnosticPagerAdapter(this));
        viewPager.setUserInputEnabled(false); // navigation is button-driven
    }

    // ── Navigation ───────────────────────────────────────────────────────────

    public void nextPage() {
        if (viewPager.getCurrentItem() < 2)
            viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }

    /**
     * Called by SymptomClarificationFragment when the final RAG response arrives.
     * Passes all result data to DiagnosisActivity for display and saving.
     */
    public void goToSummary(RagModels.ChatResponse diagnosedResponse) {
        Intent intent = new Intent(this, com.sehatsaathi.ui.DiagnosisActivity.class);
        intent.putExtra("PATIENT_NAME",  getIntent().getStringExtra("PATIENT_NAME"));
        intent.putExtra("PATIENT_AGE",   getIntent().getStringExtra("PATIENT_AGE"));
        intent.putExtra("PATIENT_GENDER", getIntent().getStringExtra("PATIENT_GENDER"));

        if (diagnosedResponse != null && diagnosedResponse.condition != null) {
            intent.putExtra("DIAGNOSIS_NAME",       diagnosedResponse.condition.name);
            intent.putExtra("DIAGNOSIS_CONFIDENCE", diagnosedResponse.condition.confidencePercentage);
            intent.putExtra("DIAGNOSIS_REASONING",  diagnosedResponse.condition.reasoning);
            intent.putExtra("DIAGNOSIS_TREATMENT",  diagnosedResponse.recommendedTreatment);
            intent.putExtra("DIAGNOSIS_MESSAGE",    diagnosedResponse.message);
            intent.putExtra("REFERRAL_NEEDED",      diagnosedResponse.referralNeeded);
            intent.putExtra("REFERRAL_REASON",      diagnosedResponse.referralReason);

            // Serialise lists to JSON strings
            Gson gson = new Gson();
            intent.putExtra("RED_FLAGS_JSON",  gson.toJson(diagnosedResponse.redFlags));
            intent.putExtra("NEXT_STEPS_JSON", gson.toJson(diagnosedResponse.nextSteps));
            intent.putExtra("PRECAUTIONS_JSON",gson.toJson(diagnosedResponse.precautions));
            intent.putExtra("MEDICATIONS_JSON",gson.toJson(diagnosedResponse.medications));
        }

        startActivity(intent);
    }

    // ── Session state setters/getters ─────────────────────────────────────────

    public void setPatientId(String id)        { this.patientId = id; }
    public String getPatientId()               { return patientId; }

    public void setSymptomText(String text)    { this.symptomText = text; }
    public String getSymptomText()             { return symptomText; }

    public void setFirstQuestion(String q)     { this.firstQuestion = q; }
    public String getFirstQuestion()           { return firstQuestion; }

    public void setCurrentQuestion(String q)   { this.currentQuestion = q; }
    public String getCurrentQuestion()         { return currentQuestion; }

    public void setLastResponse(RagModels.ChatResponse r) { this.lastResponse = r; }
    public RagModels.ChatResponse getLastResponse()       { return lastResponse; }

    public List<Object> getConversationHistory() { return conversationHistory; }

    public void addToHistory(String role, String content) {
        Map<String, Object> turn = new HashMap<>();
        turn.put("role", role);
        turn.put("content", content);
        conversationHistory.add(turn);
    }

    /** Stores assistant response as JSON string in history (required by Groq format) */
    public void addToHistory(String role, RagModels.ChatResponse response) {
        Map<String, Object> turn = new HashMap<>();
        turn.put("role", role);
        turn.put("content", new Gson().toJson(response));
        conversationHistory.add(turn);
    }
}