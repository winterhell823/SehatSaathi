package com.sehatsaathi.ui.diagnostic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sehatsaathi.R;
import com.sehatsaathi.network.RagModels;
import com.sehatsaathi.network.RetrofitClient;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiagnosticHubFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostic_hub, container, false);

        // Pre-fill patient name from intake form
        String patientName = "";
        String patientSymptoms = "";
        if (getActivity() != null) {
            patientName = getActivity().getIntent().getStringExtra("PATIENT_NAME");
            patientSymptoms = getActivity().getIntent().getStringExtra("PATIENT_SYMPTOMS");
        }

        // Show patient name in the hub card
        TextView tvPatientName = view.findViewById(R.id.tvPatientName);
        if (tvPatientName != null && patientName != null && !patientName.isEmpty()) {
            tvPatientName.setText(patientName);
        }

        // Pre-fill clinical observations with symptoms from intake
        EditText etObservations = view.findViewById(R.id.etObservations);
        if (etObservations != null && patientSymptoms != null && !patientSymptoms.isEmpty()) {
            etObservations.setText(patientSymptoms);
        }

        final String finalPatientName = patientName != null ? patientName : "patient";
        final String finalPatientSymptoms = patientSymptoms != null ? patientSymptoms : "";

        Button btnStart = view.findViewById(R.id.btnStart);
        if (btnStart != null) {
            btnStart.setOnClickListener(v -> {
                // Get symptoms from observations field (may be edited by doctor)
                String symptoms = finalPatientSymptoms;
                if (etObservations != null && !etObservations.getText().toString().trim().isEmpty()) {
                    symptoms = etObservations.getText().toString().trim();
                }

                if (symptoms.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter symptoms first", Toast.LENGTH_SHORT).show();
                    return;
                }

                btnStart.setEnabled(false);
                btnStart.setText("Analysing with RAG...");

                final String symptomText = symptoms;
                final String patientId = "PAT_" + System.currentTimeMillis();

                // Send symptoms to backend RAG
                RagModels.ChatRequest request = new RagModels.ChatRequest(
                        symptomText,
                        new ArrayList<>(),   // empty history on first call
                        patientId,
                        symptomText
                );

                RetrofitClient.getService().chat(request).enqueue(new Callback<RagModels.ChatResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<RagModels.ChatResponse> call,
                                           @NonNull Response<RagModels.ChatResponse> response) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            btnStart.setEnabled(true);
                            btnStart.setText("Start SehatSaathi Analysis  ⚡");

                            if (response.isSuccessful() && response.body() != null) {
                                RagModels.ChatResponse ragResponse = response.body();

                                // Store session data and advance to symptom clarification
                                if (getActivity() instanceof DiagnosticMainActivity) {
                                    DiagnosticMainActivity activity = (DiagnosticMainActivity) getActivity();
                                    activity.setPatientId(patientId);
                                    activity.setSymptomText(symptomText);
                                    activity.addToHistory("user", symptomText);
                                    activity.addToHistory("assistant", ragResponse);
                                    activity.setFirstQuestion(ragResponse.message != null
                                            ? ragResponse.message + "\n\n" + (ragResponse.followUpQuestion != null
                                                ? ragResponse.followUpQuestion : "")
                                            : "Please describe how you feel?");
                                    activity.nextPage();
                                }
                            } else {
                                Toast.makeText(getContext(),
                                        "RAG API error. Check server connection.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onFailure(@NonNull Call<RagModels.ChatResponse> call, @NonNull Throwable t) {
                        if (getActivity() == null) return;
                        getActivity().runOnUiThread(() -> {
                            btnStart.setEnabled(true);
                            btnStart.setText("Start SehatSaathi Analysis  ⚡");
                            Toast.makeText(getContext(),
                                    "Cannot reach server: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        });
                    }
                });
            });
        }

        return view;
    }
}