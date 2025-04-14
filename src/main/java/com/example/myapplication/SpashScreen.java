package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.window.SplashScreen;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class SpashScreen extends AppCompatActivity {
    FloatingActionButton fab;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spash_screen);

        // Delay for 10 seconds (10000ms) and then navigate to MainActivity
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SpashScreen.this, LoginActivity.class);
            startActivity(intent);
            finish(); // Close SplashActivity so it doesn't remain in back stack
        }, 6000); // Change to 20000 for 20 seconds delay
        fab=findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(SpashScreen.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}