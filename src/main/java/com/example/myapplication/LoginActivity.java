
package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class LoginActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    private static final float ANIMATION_START_TRANSLATION = 300f;
    private static final int ANIMATION_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Initialize UI components
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.view_pager);

        // Set tab text colors
        tabLayout.setTabTextColors(
                ContextCompat.getColor(this, R.color.black),
                ContextCompat.getColor(this, R.color.cyan)
        );

        // Set up adapter for ViewPager
        LoginAdapter adapter = new LoginAdapter(getSupportFragmentManager(), this, 2);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager); // Automatically sets titles

        // Set tab gravity
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        // Floating Action Button Animations

    }
}
