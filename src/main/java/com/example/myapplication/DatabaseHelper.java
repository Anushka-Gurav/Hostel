package com.example.myapplication;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class DatabaseHelper {
    private final DatabaseReference usersRef;
    private final DatabaseReference attendanceRef;
    private final Context context;
    public DatabaseHelper(Context context) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        usersRef = database.getReference("users");
        attendanceRef = database.getReference("attendance");
        this.context = context;
    }
    public float calculatePercentage(List<String[]> attendanceData) {
        int present = 0;
        for (String[] row : attendanceData) {
            if (row[1].equalsIgnoreCase("P")) {
                present++;
            }
        }
        return (present * 100f) / attendanceData.size();
    }
    private Bitmap resizeBitmap(Bitmap bitmap, int maxWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float aspectRatio = (float) height / width;
        int newHeight = (int) (maxWidth * aspectRatio);
        return Bitmap.createScaledBitmap(bitmap, maxWidth, newHeight, true);
    }
    private String bitmapToBase64(Bitmap bitmap) {
        Bitmap resizedBitmap = resizeBitmap(bitmap, 800);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        byte[] byteArray = stream.toByteArray();
        if (byteArray.length > 3 * 1024 * 1024) {
            Log.e("FIREBASE", "Image too large: " + byteArray.length + " bytes");
            Toast.makeText(context, "Image too large! Please select an image under 3MB.", Toast.LENGTH_LONG).show();
            return null;
        }
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
    public Bitmap base64ToBitmap(String base64String) {
        byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }
    public void isUserExist(String username, OnUserExistListener listener) {
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
    public void insertUser(String username, String password, String phone, Bitmap photo, Runnable onSuccess, Runnable onFailure) {
        isUserExist(username, exists -> {
            if (exists) {
                Toast.makeText(context, "Username already exists!", Toast.LENGTH_SHORT).show();
                onFailure.run();
            } else {
                String encodedPhoto = bitmapToBase64(photo);
                if (encodedPhoto == null) {
                    onFailure.run();
                    return;
                }
                Map<String, Object> userData = new HashMap<>();
                userData.put("username", username);
                userData.put("password", password);
                userData.put("phone", phone);
                userData.put("photo", encodedPhoto);
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
    public void markAttendance(String name, String date, String status) {
        attendanceRef.child(date).child(name).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().exists()) {
                attendanceRef.child(date).child(name).setValue(status)
                        .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Attendance marked: " + name + " - " + status))
                        .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to mark attendance: " + e.getMessage()));
            } else {
                Log.d("FIREBASE", "Attendance already recorded for " + name);
            }
        });
    }
    public void markAllAbsent(String date) {
        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                    String userName = userSnapshot.getKey();
                    attendanceRef.child(date).child(userName).get().addOnCompleteListener(attendanceTask -> {
                        if (!attendanceTask.getResult().exists()) {
                            attendanceRef.child(date).child(userName).setValue("A")
                                    .addOnSuccessListener(aVoid -> Log.d("FIREBASE", "Marked absent: " + userName))
                                    .addOnFailureListener(e -> Log.e("FIREBASE", "Failed to mark absent: " + e.getMessage()));
                        }
                    });
                }
            }
        }).addOnFailureListener(e -> Log.e("FIREBASE", "Error fetching users: " + e.getMessage()));
    }
    public interface OnUserExistListener {
        void onResult(boolean exists);
    }
    public interface OnByteArrayLoadedListener {
        void onByteArrayLoaded(byte[] imageData);
    }
    public void getUserPhoto(String name, OnByteArrayLoadedListener listener) {
        usersRef.child(name).child("photo").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                String base64String = task.getResult().getValue(String.class);
                if (base64String != null && !base64String.isEmpty()) {
                    try {
                        byte[] userPhotoBytes = Base64.decode(base64String, Base64.DEFAULT);
                        if (userPhotoBytes.length > 0) {
                            Log.d("FIREBASE", "User photo fetched successfully for " + name);
                            listener.onByteArrayLoaded(userPhotoBytes);
                        } else {
                            Log.e("FIREBASE", "Decoded byte array is empty for user: " + name);
                            listener.onByteArrayLoaded(null);
                        }
                    } catch (Exception e) {
                        Log.e("FIREBASE", "Error decoding base64 image: " + e.getMessage());
                        listener.onByteArrayLoaded(null);
                    }
                } else {
                    Log.e("FIREBASE", "Photo not found or empty for user: " + name);
                    listener.onByteArrayLoaded(null);
                }
            } else {
                Log.e("FIREBASE", "Failed to fetch user photo: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
                listener.onByteArrayLoaded(null);
            }
        });
    }
    public interface OnBitmapLoadedListener {
        void onBitmapLoaded(Bitmap bitmap);
    }
    public interface OnResultListener<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }
    public void getMonthlyAttendance(String username, String monthPrefix, OnResultListener<Map<String, String>> listener) {
        attendanceRef.orderByKey().startAt(monthPrefix).endAt(monthPrefix + "\uf8ff").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        Map<String, String> monthlyData = new HashMap<>();
                        for (DataSnapshot dateSnapshot : task.getResult().getChildren()) {
                            String dateKey = dateSnapshot.getKey();
                            if (dateKey != null) {
                                DataSnapshot userSnap = dateSnapshot.child(username);
                                monthlyData.put(dateKey, userSnap.exists() ? userSnap.getValue(String.class) : "A");
                            }
                        }
                        listener.onSuccess(monthlyData);
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Attendance data not found"));
                    }
                });
    }
    public void getDailyAttendance(String username, String date, OnResultListener<String> listener) {
        attendanceRef.child(date).child(username).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            listener.onSuccess(task.getResult().getValue(String.class));
                        } else {
                            listener.onSuccess("A");  // Default to Absent if no entry
                        }
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Attendance not found"));
                    }
                });
    }
    public void getMonthlyAttendancePercentage(String username, String monthPrefix, OnResultListener<Float> listener) {
        attendanceRef.orderByKey().startAt(monthPrefix).endAt(monthPrefix + "\uf8ff").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        int totalDays = 0;
                        int presentDays = 0;

                        for (DataSnapshot dateSnapshot : task.getResult().getChildren()) {
                            totalDays++;
                            String status = dateSnapshot.child(username).getValue(String.class);
                            if ("P".equals(status)) {
                                presentDays++;
                            }
                        }

                        float percentage = (totalDays > 0) ? ((float) presentDays / totalDays) * 100 : 0f;
                        listener.onSuccess(percentage);
                    } else {
                        listener.onFailure(task.getException() != null ? task.getException() : new Exception("Attendance data not found"));
                    }
                });
    }
    public void getMonthlyAttendanceDataFromFirebase(String username, String month, OnResultListener<List<String[]>> listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("attendance").child(username);
        List<String[]> attendanceData = new ArrayList<>();

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    String status = dateSnapshot.getValue(String.class);

                    if (date != null && date.startsWith(month)) {
                        attendanceData.add(new String[]{date, status});
                    }
                }

                if (attendanceData.isEmpty()) {
                    listener.onFailure(new Exception("No data found"));
                } else {
                    listener.onSuccess(attendanceData);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.toException());
            }
        });
    }

    // Calculate monthly attendance percentage
    public void getMonthlyAttendancePercentageFromFirebase(String username, String month, OnResultListener<Float> listener) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("attendance").child(username);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalDays = 0;
                int presentDays = 0;

                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                    String date = dateSnapshot.getKey();
                    String status = dateSnapshot.getValue(String.class);

                    if (date != null && date.startsWith(month)) {
                        totalDays++;
                        if ("P".equalsIgnoreCase(status)) {
                            presentDays++;
                        }
                    }
                }

                if (totalDays == 0) {
                    listener.onFailure(new Exception("No attendance records for the month"));
                } else {
                    float percentage = (presentDays * 100.0f) / totalDays;
                    listener.onSuccess(percentage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure(error.toException());
            }
        });
    }

    public interface OnLoginResultListener {
        void onLoginResult(boolean success, String message);
    }

    public void checkUserCredentials(String username, String password, OnLoginResultListener listener) {
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
    public void updateUserPassword(String username, String newPassword, String confirmPassword, OnResultListener<String> listener) {
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
                listener.onFailure(new Exception("User not found"));
            }
        }).addOnFailureListener(e -> {
            listener.onFailure(new Exception("Failed to fetch user: " + e.getMessage()));
        });
    }


}