package com.sehatsaathi.ui.diagnostic;
import android.os.Bundle; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup;
import androidx.annotation.NonNull; import androidx.annotation.Nullable; import androidx.fragment.app.Fragment;
import com.sehatsaathi.R;
public class SymptomClarificationFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_symptom_clarification, container, false);
        view.findViewById(R.id.btnProceed).setOnClickListener(v -> {
            if(getActivity() instanceof DiagnosticMainActivity) ((DiagnosticMainActivity)getActivity()).nextPage();
        });
        return view;
    }
}