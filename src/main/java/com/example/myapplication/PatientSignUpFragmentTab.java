package com.example.myapplication;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PatientSignUpFragmentTab extends Fragment {
    private static final int IMAGE_PICK_CODE = 100;
    private static final int PERMISSION_CODE = 101;
    float v=0;
    private EditText editTextName, editTextPassword,editTextPhone;
    private ImageView imageViewPhoto;
    private Button btnSelectPhoto, btnRegister;
    private DatabaseHelper databaseHelper;
    private TextView btnlogin;
    private Bitmap selectedBitmap = null;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.sign_upfragment, container, false);


        FirebaseDatabase database=FirebaseDatabase.getInstance();
        DatabaseReference myref=database.getReference("msg");
        editTextName = root.findViewById(R.id.name);
        editTextPassword = root.findViewById(R.id.pass);
        imageViewPhoto = root.findViewById(R.id.imageViewPhoto);
        btnSelectPhoto = root.findViewById(R.id.selectPhoto);
        btnRegister = root.findViewById(R.id.sign_up);
        editTextPhone=root.findViewById(R.id.Mobile);
        btnlogin=root.findViewById(R.id.login);
        databaseHelper = new DatabaseHelper(getContext());

        editTextName.setTranslationX(800);
        editTextPhone.setTranslationX(800);
        editTextPassword.setTranslationX(800);
        btnRegister.setTranslationX(800);
        btnSelectPhoto.setTranslationX(800);
        editTextName.setAlpha(v);
        editTextPhone.setAlpha(v);
        editTextPassword.setAlpha(v);
        btnSelectPhoto.setAlpha(v);
        btnRegister.setAlpha(v);
        editTextName.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(300).start();
        editTextPhone.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(700).start();
        editTextPassword.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(900).start();
        btnRegister.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(1100).start();
        btnSelectPhoto.animate().translationX(0).alpha(1).setDuration(800).setStartDelay(1100).start();

        btnSelectPhoto.setOnClickListener(view -> {
            if (checkPermission()) {
                pickImageFromGallery();
            } else {
                requestPermission();
            }
        });
        btnRegister.setOnClickListener(view -> saveUserData());
        btnlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it=new Intent(getActivity(), LoginActivity.class);
                startActivity(it);
            }
        });


        return root;
    }
    private boolean checkPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.CAMERA},
                    PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
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
            Toast.makeText(getContext(), "Please enter username, password & mobile number!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedBitmap == null) {
            Toast.makeText(getContext(), "Please select a photo!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.insertUser(username, password, phone, selectedBitmap,
                () ->{
                    Toast.makeText(getContext(), "User Registered Successfully!", Toast.LENGTH_SHORT).show();
                    Intent it=new Intent(getContext(),AttendanceActivity.class);
                    startActivity(it);
                },
                () -> Toast.makeText(getContext(), "Username already exists or Registration Failed!", Toast.LENGTH_SHORT).show()
        );
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_CODE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);
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
                Toast.makeText(getContext(), "Permissions Denied! Enable them in settings.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
