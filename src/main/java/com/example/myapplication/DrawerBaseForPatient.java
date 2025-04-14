package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.io.File;

public class DrawerBaseForPatient extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout drawerLayout;

    public void setContentView(View view) {
        drawerLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_drawer_base, null);
        FrameLayout container = drawerLayout.findViewById(R.id.activityContainer);
        container.addView(view);
        super.setContentView(drawerLayout);
        Toolbar toolbar = drawerLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        NavigationView navigationView = drawerLayout.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.menu_drawer_open, R.string.menu_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_home) {
            openHome();
        }
//        else if (item.getItemId() == R.id.nav_chat) {
//            //openChatbot();
//        }
        else if(item.getItemId()==R.id.nav_logout)
        {
            openLogOut();
        }
        else if(item.getItemId()==R.id.nav_settings)
        {
            openSetting();
        }
        else if(item.getItemId()==R.id.nav_about)
        {
            openAbout();
        }
        else if(item.getItemId()==R.id.nav_share)
        {
            openShare();
        }
        drawerLayout.closeDrawers(); // Close the navigation drawer after selection
        return true;
    }

    private  static final int REQUEST_CODE = 100;

    private void openShare()
        {
            shareApkFile();
        }
    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.MANAGE_DEVICE_POLICY_RUNTIME_PERMISSIONS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.MANAGE_DEVICE_POLICY_RUNTIME_PERMISSIONS};
                requestPermissions(permissions, REQUEST_CODE);
            }
        }
    }

    // Handle the permission result
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void shareApkFile() {
        try {
            // Retrieve APK file path
            ApplicationInfo appInfo = getApplicationContext().getPackageManager()
                    .getApplicationInfo(getPackageName(), 0);
            File apkFile = new File(appInfo.sourceDir);

            if (!apkFile.exists()) {
                Toast.makeText(this, "APK file not found.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "APK file not found at: " + apkFile.getAbsolutePath());
                return;
            }

            Log.d(TAG, "APK File Path: " + apkFile.getAbsolutePath());

            // Generate URI using FileProvider
            Uri apkUri = FileProvider.getUriForFile(
                    this,
                    "com.example.share_apk.provider",  // Must match the authorities in AndroidManifest.xml
                    apkFile
            );

            Log.d(TAG, "Generated APK URI: " + apkUri.toString());

            // Create the share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.android.package-archive");
            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Jivan Jyot APK"));

        } catch (Exception e) {
            Log.e(TAG, "Error sharing APK: " + e.getMessage());
            Toast.makeText(this, "Error sharing APK: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


        private void openAbout()
        {
            Intent intent=new Intent(this,About.class);
            startActivity(intent);
        }
//    private void openChatbot() {
//        Intent intent = new Intent(this, ChatBot.class);
//        startActivity(intent);
//    }
    private void openSetting()
    {
        Intent intent=new Intent(this, SettingClass.class);
        startActivity(intent);
    }

    private void openHome() {
//        Intent intent = new Intent(this, PatientHome.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
//        finish(); // Prevent activity stacking issues
    }
    private void openLogOut(){
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish(); // Prevent activity stacking issues

    }
    protected void allocateActivity(String titleString) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(titleString);
        }
    }
}
