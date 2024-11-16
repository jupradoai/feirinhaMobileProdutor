package com.aula.twittercourse;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class DashboardActivity extends MenuActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        // Find the ViewPager2 and TabLayout
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        // Set up the ViewPager2 with the FragmentStateAdapter
        FragmentStateAdapter adapter = new ChartPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Link the TabLayout with the ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Produtos e Preço");
                    break;
                case 1:
                    tab.setText("Progresso");
                    break;
                case 2:
                    tab.setText("Radar Chart");
                    break;
            }
        }).attach();
    }

    private static class ChartPagerAdapter extends FragmentStateAdapter {

        public ChartPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new BarChartFragment();
                case 1:
                    return new PieChartFragment();
                case 2:
                    return new RadarChartFragment();
                default:
                    throw new IllegalStateException("Unexpected position: " + position);
            }
        }

        @Override
        public int getItemCount() {
            return 3; // Number of pages
        }
    }
}