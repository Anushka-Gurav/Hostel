package com.example.myapplication;
import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import java.io.File;

public class Share extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        shareApkFile();
    }
    private static final int REQUEST_CODE = 100;

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.MANAGE_DEVICE_POLICY_RUNTIME_PERMISSIONS) == PackageManager.PERMISSION_DENIED) {
                String[] permissions = {Manifest.permission.MANAGE_DEVICE_POLICY_RUNTIME_PERMISSIONS};
                requestPermissions(permissions, REQUEST_CODE);
            }
        }
    }

    // Handle the permission result
    @Override
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
                    "com.example.myapplication.provider",  // Must match the authorities in AndroidManifest.xml
                    apkFile
            );

            Log.d(TAG, "Generated APK URI: " + apkUri.toString());

            // Create the share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.android.package-archive");
            shareIntent.putExtra(Intent.EXTRA_STREAM, apkUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Hostel APK"));

        } catch (Exception e) {
            Log.e(TAG, "Error sharing APK: " + e.getMessage());
            Toast.makeText(this, "Error sharing APK: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

}
