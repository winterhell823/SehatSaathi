package com.sehatsaathi.ui.diagnostic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sehatsaathi.R;

public class SymptomClarificationFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symptom_clarification, container, false);

        // Wire up answer buttons to highlight selection
        setupQuestionButtons(view,
                R.id.btnQ1Yes, R.id.btnQ1No, R.id.btnQ1Unsure);
        setupQuestionButtons(view,
                R.id.btnQ2Yes, R.id.btnQ2No, R.id.btnQ2Unsure);
        setupQuestionButtons(view,
                R.id.btnQ3Yes, R.id.btnQ3No, -1);
        setupQuestionButtons(view,
                R.id.btnQ4Yes, R.id.btnQ4No, -1);
        setupQuestionButtons(view,
                R.id.btnQ5Yes, R.id.btnQ5No, R.id.btnQ5Unsure);

        // Proceed to analysis → go to DiagnosisActivity (saves history)
        View btnProceed = view.findViewById(R.id.btnProceed);
        if (btnProceed != null) {
            btnProceed.setOnClickListener(v -> {
                if (getActivity() instanceof DiagnosticMainActivity) {
                    ((DiagnosticMainActivity) getActivity()).goToSummary();
                }
            });
        }

        return view;
    }

    private void setupQuestionButtons(View root, int yesId, int noId, int unsureId) {
        View btnYes = root.findViewById(yesId);
        View btnNo = root.findViewById(noId);
        View btnUnsure = unsureId != -1 ? root.findViewById(unsureId) : null;

        View.OnClickListener listener = v -> {
            // Reset all to inactive
            if (btnYes != null) btnYes.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
            if (btnNo != null) btnNo.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
            if (btnUnsure != null) btnUnsure.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
            // Highlight selected
            v.setBackgroundResource(R.drawable.bg_diag_selection_active);
        };

        if (btnYes != null) btnYes.setOnClickListener(listener);
        if (btnNo != null) btnNo.setOnClickListener(listener);
        if (btnUnsure != null) btnUnsure.setOnClickListener(listener);
    }
}