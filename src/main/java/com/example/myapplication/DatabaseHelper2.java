package com.example.myapplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper2 {
    private final DatabaseReference usersRef;
    private final Context context;
    public DatabaseHelper2(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("admin");
        this.context = context;
    }
    public void isUserExist(String username, DatabaseHelper.OnUserExistListener listener) {
        usersRef.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                listener.onResult(true);
            } else {
                listener.onResult(false);
            }
        }).addOnFailureListener(e -> {
            Log.e("FIREBASE", "Error checking user existence: " + e.getMessage());
            listener.onResult(false);
        });
    }
    public void insertUser(String username, String password, String phone, Runnable onSuccess, Runnable onFailure) {
        isUserExist(username, exists -> {
            if (exists) {
                Toast.makeText(context, "Username already exists!", Toast.LENGTH_SHORT).show();
                onFailure.run();
            } else {


                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("password", password);
                userData.put("phone", phone);
                usersRef.child(username).setValue(userData)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FIREBASE", "User registered: " + username);
                            Toast.makeText(context, "User registered successfully!", Toast.LENGTH_SHORT).show();
                            onSuccess.run();
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FIREBASE", "Failed to register user: " + e.getMessage());
                            Toast.makeText(context, "Failed to register user!", Toast.LENGTH_SHORT).show();
                            onFailure.run();
                        });
            }
        });
    }
    public interface OnLoginResultListener {
        void onLoginResult(boolean success, String message);
    }

    public void checkUserCredentials(String username, String password, DatabaseHelper.OnLoginResultListener listener) {
        usersRef.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                String storedPassword = task.getResult().child("password").getValue(String.class);
                if (storedPassword != null && storedPassword.equals(password)) {
                    listener.onLoginResult(true, "Login successful");
                } else {
                    listener.onLoginResult(false, "Incorrect password");
                }
            } else {
                listener.onLoginResult(false, "User not found");
            }
        }).addOnFailureListener(e -> {
            listener.onLoginResult(false, "Login failed: " + e.getMessage());
        });
    }
    public void updateUserPassword(String username, String newPassword, String confirmPassword, DatabaseHelper.OnResultListener<String> listener) {
        if (!newPassword.equals(confirmPassword)) {
            listener.onFailure(new Exception("Passwords do not match"));
            return;
        }

        usersRef.child(username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                usersRef.child(username).child("password").setValue(newPassword)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("FIREBASE", "Password updated successfully for " + username);
                            listener.onSuccess("Password updated successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FIREBASE", "Failed to update password: " + e.getMessage());
                            listener.onFailure(e);
                        });
            } else {
                listener.onFailure(new Exception("Admin not found"));
            }
        }).addOnFailureListener(e -> {
            listener.onFailure(new Exception("Failed to fetch user: " + e.getMessage()));
        });
    }

}
