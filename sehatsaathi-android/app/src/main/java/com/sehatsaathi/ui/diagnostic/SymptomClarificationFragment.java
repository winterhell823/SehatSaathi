package com.sehatsaathi.ui.diagnostic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sehatsaathi.R;
import com.sehatsaathi.network.RagModels;
import com.sehatsaathi.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SymptomClarificationFragment extends Fragment {

    private TextView tvQuestion;
    private Button btnYes, btnNo, btnUnsure, btnProceed;
    private LinearLayout loadingLayout;
    private String selectedAnswer = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symptom_clarification, container, false);

        tvQuestion    = view.findViewById(R.id.tvCurrentQuestion);
        btnYes        = view.findViewById(R.id.btnQ1Yes);
        btnNo         = view.findViewById(R.id.btnQ1No);
        btnUnsure     = view.findViewById(R.id.btnQ1Unsure);
        btnProceed    = view.findViewById(R.id.btnProceed);
        loadingLayout = view.findViewById(R.id.loadingLayout);

        // Show the first question from the RAG initial response
        if (getActivity() instanceof DiagnosticMainActivity) {
            DiagnosticMainActivity activity = (DiagnosticMainActivity) getActivity();
            String q = activity.getFirstQuestion();
            if (tvQuestion != null && q != null) tvQuestion.setText(q);
        }

        // Answer buttons
        View.OnClickListener answerListener = v -> {
            selectedAnswer = null;
            if (v.getId() == R.id.btnQ1Yes)   selectedAnswer = "Yes";
            else if (v.getId() == R.id.btnQ1No)    selectedAnswer = "No";
            else if (v.getId() == R.id.btnQ1Unsure) selectedAnswer = "Unsure";

            // Highlight selected
            if (btnYes != null)   btnYes.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
            if (btnNo != null)    btnNo.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
            if (btnUnsure != null) btnUnsure.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
            v.setBackgroundResource(R.drawable.bg_diag_selection_active);
        };

        if (btnYes != null)    btnYes.setOnClickListener(answerListener);
        if (btnNo != null)     btnNo.setOnClickListener(answerListener);
        if (btnUnsure != null) btnUnsure.setOnClickListener(answerListener);

        if (btnProceed != null) {
            btnProceed.setOnClickListener(v -> {
                if (selectedAnswer == null) {
                    Toast.makeText(getContext(), "Please select an answer", Toast.LENGTH_SHORT).show();
                    return;
                }
                sendAnswerToRag(selectedAnswer);
            });
        }

        return view;
    }

    private void sendAnswerToRag(String answer) {
        if (!(getActivity() instanceof DiagnosticMainActivity)) return;
        DiagnosticMainActivity activity = (DiagnosticMainActivity) getActivity();

        // Show loading
        if (loadingLayout != null) loadingLayout.setVisibility(View.VISIBLE);
        if (btnProceed != null)    btnProceed.setEnabled(false);

        // Add user answer to history
        activity.addToHistory("user", answer);

        RagModels.ChatRequest request = new RagModels.ChatRequest(
                answer,
                activity.getConversationHistory(),
                activity.getPatientId(),
                activity.getSymptomText()
        );

        RetrofitClient.getService().chat(request).enqueue(new Callback<RagModels.ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<RagModels.ChatResponse> call,
                                   @NonNull Response<RagModels.ChatResponse> response) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                    if (btnProceed != null)    btnProceed.setEnabled(true);

                    if (response.isSuccessful() && response.body() != null) {
                        RagModels.ChatResponse ragResponse = response.body();
                        activity.addToHistory("assistant", ragResponse);
                        activity.setLastResponse(ragResponse);

                        if ("diagnosed".equals(ragResponse.status)) {
                            // Final diagnosis received — go to summary
                            activity.goToSummary(ragResponse);
                        } else {
                            // More follow-up needed — show next question
                            selectedAnswer = null;
                            if (btnYes != null)   btnYes.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
                            if (btnNo != null)    btnNo.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
                            if (btnUnsure != null) btnUnsure.setBackgroundResource(R.drawable.bg_diag_selection_inactive);

                            String nextQ = ragResponse.message != null ? ragResponse.message : "Any other symptoms?";
                            if (tvQuestion != null) tvQuestion.setText(nextQ);
                        }
                    } else {
                        Toast.makeText(getContext(), "Server error. Try again.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<RagModels.ChatResponse> call, @NonNull Throwable t) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    if (loadingLayout != null) loadingLayout.setVisibility(View.GONE);
                    if (btnProceed != null)    btnProceed.setEnabled(true);
                    Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }
}