package com.sehatsaathi.ui.diagnostic;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.sehatsaathi.R;
public class DiagnosticMainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnostic_main);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new DiagnosticPagerAdapter(this));
    }
    public void nextPage() {
        if(viewPager.getCurrentItem() < 2) viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
    }
}