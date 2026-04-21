package com.sehatsaathi.ui.diagnostic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.sehatsaathi.R;

public class SymptomClarificationFragment extends Fragment {
    
    private String[] answers = new String[5]; // Store answers for 5 questions
    
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symptom_clarification, container, false);
        
        // Question 1 - Fever duration
        setupQuestionButton(view, R.id.btnQ1Yes, R.id.btnQ1No, R.id.btnQ1Unsure, 0);
        
        // Question 2 - Skin rashes
        setupQuestionButton(view, R.id.btnQ2Yes, R.id.btnQ2No, R.id.btnQ2Unsure, 1);
        
        // Question 3 - Breathing difficulty (Yes/No only)
        setupYesNoButton(view, R.id.btnQ3Yes, R.id.btnQ3No, 2);
        
        // Question 4 - Severe headaches (Yes/No only)
        setupYesNoButton(view, R.id.btnQ4Yes, R.id.btnQ4No, 3);
        
        // Question 5 - Loss of appetite
        setupQuestionButton(view, R.id.btnQ5Yes, R.id.btnQ5No, R.id.btnQ5Unsure, 4);
        
        view.findViewById(R.id.btnProceed).setOnClickListener(v -> {
            // Pass answers to activity before proceeding
            if(getActivity() instanceof DiagnosticMainActivity) {
                ((DiagnosticMainActivity)getActivity()).setSymptomAnswers(answers);
                ((DiagnosticMainActivity)getActivity()).nextPage();
            }
        });
        
        return view;
    }
    
    private void setupQuestionButton(View view, int yesId, int noId, int unsureId, int questionIndex) {
        Button yesBtn = view.findViewById(yesId);
        Button noBtn = view.findViewById(noId);
        Button unsureBtn = view.findViewById(unsureId);
        
        View.OnClickListener listener = v -> {
            // Reset all buttons in this group
            resetButton(yesBtn);
            resetButton(noBtn);
            resetButton(unsureBtn);
            
            // Highlight selected button
            Button selected = (Button) v;
            selected.setBackgroundResource(R.drawable.bg_diag_selection_active);
            selected.setTextColor(getResources().getColor(android.R.color.white, null));
            
            // Store answer
            answers[questionIndex] = selected.getText().toString();
        };
        
        yesBtn.setOnClickListener(listener);
        noBtn.setOnClickListener(listener);
        unsureBtn.setOnClickListener(listener);
    }
    
    private void setupYesNoButton(View view, int yesId, int noId, int questionIndex) {
        Button yesBtn = view.findViewById(yesId);
        Button noBtn = view.findViewById(noId);
        
        View.OnClickListener listener = v -> {
            resetButton(yesBtn);
            resetButton(noBtn);
            
            Button selected = (Button) v;
            selected.setBackgroundResource(R.drawable.bg_diag_selection_active);
            selected.setTextColor(getResources().getColor(android.R.color.white, null));
            
            answers[questionIndex] = selected.getText().toString();
        };
        
        yesBtn.setOnClickListener(listener);
        noBtn.setOnClickListener(listener);
    }
    
    private void resetButton(Button btn) {
        btn.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
        btn.setTextColor(getResources().getColor(R.color.diag_text_gray, null));
    }
}