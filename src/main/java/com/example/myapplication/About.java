package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import com.example.myapplication.databinding.ActivityDashboardBinding;
public class About extends DrawerBaseActivity {
    ActivityDashboardBinding activityDashboardBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardBinding.getRoot());

        getLayoutInflater().inflate(R.layout.about_us, activityDashboardBinding.contentFrame, true);

        Button suggestionButton = findViewById(R.id.tutorial_button);

        // Link to the video (Replace with your actual video URL)
        String videoUrl = "https://www.youtube.com/watch?v=YOUR_VIDEO_LINK";

        suggestionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(intent);
        });
    }
}