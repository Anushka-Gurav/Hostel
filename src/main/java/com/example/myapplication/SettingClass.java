package com.example.myapplication;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
public class SettingClass extends AppCompatActivity {
    private Switch nightModeSwitch; // Toggle button for night mode
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting);
        // Load user's theme preference
        SharedPreferences preferences = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = preferences.getBoolean("dark_mode", false);
        // Apply theme before setting content view
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // Initialize UI components
        nightModeSwitch = findViewById(R.id.themeSwitch);
        nightModeSwitch.setChecked(isDarkMode); // Set switch state based on stored preference
        // Handle switch toggle
        nightModeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleTheme(isChecked);
            }
        });
    }
    // Function to toggle theme and save user preference
    private void toggleTheme(boolean enableDarkMode) {
        SharedPreferences.Editor editor = getSharedPreferences("settings", MODE_PRIVATE).edit();
        editor.putBoolean("dark_mode", enableDarkMode);
        editor.apply();
        // Apply selected theme
        if (enableDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        // Restart activity to apply theme change
        recreate();
    }
}
