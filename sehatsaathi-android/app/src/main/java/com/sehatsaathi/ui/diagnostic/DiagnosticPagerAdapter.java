package com.sehatsaathi.ui.diagnostic;
import androidx.annotation.NonNull; import androidx.fragment.app.Fragment; import androidx.fragment.app.FragmentActivity; import androidx.viewpager2.adapter.FragmentStateAdapter;
public class DiagnosticPagerAdapter extends FragmentStateAdapter {
    public DiagnosticPagerAdapter(@NonNull FragmentActivity fa) { super(fa); }
    @NonNull @Override public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new DiagnosticHubFragment();
            case 1: return new SymptomClarificationFragment();
            case 2: return new DiagnosticSummaryFragment();
            default: return new DiagnosticHubFragment();
        }
    }
    @Override public int getItemCount() { return 3; }
}