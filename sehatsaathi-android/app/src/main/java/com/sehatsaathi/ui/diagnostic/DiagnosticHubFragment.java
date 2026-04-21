package com.sehatsaathi.ui.diagnostic;
import android.os.Bundle; import android.view.LayoutInflater; import android.view.View; import android.view.ViewGroup;
import androidx.annotation.NonNull; import androidx.annotation.Nullable; import androidx.fragment.app.Fragment;
import com.sehatsaathi.R;
public class DiagnosticHubFragment extends Fragment {
    @Nullable @Override public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diagnostic_hub, container, false);
        view.findViewById(R.id.btnStart).setOnClickListener(v -> {
            if(getActivity() instanceof DiagnosticMainActivity) ((DiagnosticMainActivity)getActivity()).nextPage();
        });
        return view;
    }
}