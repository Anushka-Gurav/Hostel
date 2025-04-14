package com.example.myapplication;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private static final int IMAGE_PICK_CODE = 100;
    private static final int PERMISSION_CODE = 101;

    private EditText editTextName, editTextPassword,editTextPhone;
    private ImageView imageViewPhoto;
    private Button btnSelectPhoto, btnRegister;
    private DatabaseHelper databaseHelper;

    private Bitmap selectedBitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myref=database.getReference("msg");
        myref.setValue("hello world");
        editTextName = findViewById(R.id.editTextName);
        editTextPassword = findViewById(R.id.editTextPassword);
        imageViewPhoto = findViewById(R.id.imageViewPhoto);
        btnSelectPhoto = findViewById(R.id.btnSelectPhoto);
        btnRegister = findViewById(R.id.btnRegister);
        editTextPhone=findViewById(R.id.editTextPhone);
        databaseHelper = new DatabaseHelper(MainActivity.this);
     //   firebaseDatabase = FirebaseDatabase.getInstance();
      //  scheduleDailyReminder(this);

        btnSelectPhoto.setOnClickListener(view -> {
            if (checkPermission()) {
                pickImageFromGallery();
            } else {
                requestPermission();
            }
        });
        btnRegister.setOnClickListener(view -> saveUserData());
    }
    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.CAMERA},
                    PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                    PERMISSION_CODE);
        }
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    private byte[] convertBitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }
    private void saveUserData() {
        String username = editTextName.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String phone = editTextPhone.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please enter username, password & mobile number!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBitmap == null) {
            Toast.makeText(this, "Please select a photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.insertUser(username, password, phone, selectedBitmap,
                () ->{
            Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show();
            Intent it=new Intent(MainActivity.this,AttendanceActivity.class);
            startActivity(it);
                },
                () -> Toast.makeText(this, "Username already exists or Registration Failed!", Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imageViewPhoto.setImageBitmap(selectedBitmap);
            } catch (IOException e) {
                Log.e("ImageError", "Failed to load image", e);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {
            boolean allGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Log.e("PERMISSION_ERROR", "Permission Denied: " + permissions[i]);
                    allGranted = false;
                }
            }

            if (allGranted) {
                pickImageFromGallery();
            } else {
                Toast.makeText(this, "Permissions Denied! Enable them in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }
//    public void scheduleDailyReminder(Context context) {
//        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//
//        // Reminder Notification at 7:00 PM
//        Intent reminderIntent = new Intent(context, RemainderReceiver.class);
//        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(
//                context, 0, reminderIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Calendar reminderTime = Calendar.getInstance();
//        reminderTime.set(Calendar.HOUR_OF_DAY, 19);  // 7:00 PM
//        reminderTime.set(Calendar.MINUTE, 0);
//        reminderTime.set(Calendar.SECOND, 0);
//
//        if (alarmManager != null) {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                    reminderTime.getTimeInMillis(),
//                    AlarmManager.INTERVAL_DAY,
//                    reminderPendingIntent);
//        }
//
//        // Auto-mark Absent at 9:30 PM
//        Intent absentIntent = new Intent(context, MarkAbsentReceiver.class);
//        PendingIntent absentPendingIntent = PendingIntent.getBroadcast(
//                context, 1, absentIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//
//        Calendar absentTime = Calendar.getInstance();
//        absentTime.set(Calendar.HOUR_OF_DAY, 21);  // 9:30 PM
//        absentTime.set(Calendar.MINUTE, 40);
//        absentTime.set(Calendar.SECOND, 0);
//
//        if (alarmManager != null) {
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
//                    absentTime.getTimeInMillis(),
//                    AlarmManager.INTERVAL_DAY,
//                    absentPendingIntent);
//        }
//    }

}
