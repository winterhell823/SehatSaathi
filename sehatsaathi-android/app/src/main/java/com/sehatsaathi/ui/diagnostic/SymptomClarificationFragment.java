package com.sehatsaathi.ui.diagnostic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.sehatsaathi.R;

public class SymptomClarificationFragment extends Fragment {
    
    private String[] answers = new String[5];
    
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symptom_clarification, container, false);
        
        Toast.makeText(getContext(), "Symptom Clarification loaded", Toast.LENGTH_SHORT).show();
        
        // Question 1
        setupQuestionButton(view, R.id.btnQ1Yes, R.id.btnQ1No, R.id.btnQ1Unsure, 0);
        // Question 2
        setupQuestionButton(view, R.id.btnQ2Yes, R.id.btnQ2No, R.id.btnQ2Unsure, 1);
        // Question 3 - Yes/No only
        setupYesNoButton(view, R.id.btnQ3Yes, R.id.btnQ3No, 2);
        // Question 4 - Yes/No only
        setupYesNoButton(view, R.id.btnQ4Yes, R.id.btnQ4No, 3);
        // Question 5
        setupQuestionButton(view, R.id.btnQ5Yes, R.id.btnQ5No, R.id.btnQ5Unsure, 4);
        
        view.findViewById(R.id.btnProceed).setOnClickListener(v -> {
            StringBuilder sb = new StringBuilder("Answers: ");
            for (int i = 0; i < answers.length; i++) {
                sb.append(answers[i] != null ? answers[i] : "null").append(", ");
            }
            Toast.makeText(getContext(), sb.toString(), Toast.LENGTH_LONG).show();
            
            if(getActivity() instanceof DiagnosticMainActivity) {
                ((DiagnosticMainActivity)getActivity()).setSymptomAnswers(answers);
                ((DiagnosticMainActivity)getActivity()).nextPage();
            }
        });
        
        return view;
    }
    
    private void setupQuestionButton(View view, int yesId, int noId, int unsureId, int index) {
        Button yesBtn = view.findViewById(yesId);
        Button noBtn = view.findViewById(noId);
        Button unsureBtn = view.findViewById(unsureId);
        
        View.OnClickListener listener = v -> {
            Toast.makeText(getContext(), "Clicked: " + ((Button)v).getText(), Toast.LENGTH_SHORT).show();
            
            resetButton(yesBtn);
            resetButton(noBtn);
            resetButton(unsureBtn);
            
            Button selected = (Button) v;
            selected.setBackgroundResource(R.drawable.bg_diag_selection_active);
            selected.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            
            answers[index] = selected.getText().toString();
        };
        
        yesBtn.setOnClickListener(listener);
        noBtn.setOnClickListener(listener);
        unsureBtn.setOnClickListener(listener);
    }
    
    private void setupYesNoButton(View view, int yesId, int noId, int index) {
        Button yesBtn = view.findViewById(yesId);
        Button noBtn = view.findViewById(noId);
        
        View.OnClickListener listener = v -> {
            Toast.makeText(getContext(), "Clicked: " + ((Button)v).getText(), Toast.LENGTH_SHORT).show();
            
            resetButton(yesBtn);
            resetButton(noBtn);
            
            Button selected = (Button) v;
            selected.setBackgroundResource(R.drawable.bg_diag_selection_active);
            selected.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white));
            
            answers[index] = selected.getText().toString();
        };
        
        yesBtn.setOnClickListener(listener);
        noBtn.setOnClickListener(listener);
    }
    
    private void resetButton(Button btn) {
        btn.setBackgroundResource(R.drawable.bg_diag_selection_inactive);
        btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.diag_text_gray));
    }
}