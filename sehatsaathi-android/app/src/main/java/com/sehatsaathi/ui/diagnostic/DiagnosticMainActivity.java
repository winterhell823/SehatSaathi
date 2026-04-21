package com.sehatsaathi.ui.diagnostic;

import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.sehatsaathi.R;

public class DiagnosticMainActivity extends AppCompatActivity {
    
    private ViewPager2 viewPager;
    private String[] symptomAnswers;
    
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_main);
        
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new DiagnosticPagerAdapter(this));
        
        // Disable swipe (optional - remove if you want swipe navigation)
        viewPager.setUserInputEnabled(false);
        
        // Bottom nav handling
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav != null) {
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    finish();
                    return true;
                } else if (itemId == R.id.nav_patients) {
                    finish();
                    return true;
                } else if (itemId == R.id.nav_tools) {
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    finish();
                    return true;
                }
                return false;
            });
        }
    }
    
    public void nextPage() {
        if(viewPager.getCurrentItem() < 2) viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }
    
    public void setSymptomAnswers(String[] answers) {
        this.symptomAnswers = answers;
    }
    
    public String[] getSymptomAnswers() {
        return symptomAnswers;
    }
}