//
//package com.example.myapplication;
//
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.location.Location;
//import android.os.AsyncTask;
//import android.os.Bundle;
//import android.provider.MediaStore;
//import android.util.Log;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.ImageView;
//import android.widget.Toast;
//
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.biometric.BiometricManager;
//import androidx.biometric.BiometricPrompt;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//
//import com.google.android.gms.location.FusedLocationProviderClient;
//import com.google.android.gms.location.LocationServices;
//
//import org.opencv.android.OpenCVLoader;
//import org.opencv.android.Utils;
//import org.opencv.core.*;
//import org.opencv.features2d.DescriptorMatcher;
//import org.opencv.features2d.ORB;
//import org.opencv.features2d.FlannBasedMatcher;
//import org.opencv.imgproc.Imgproc;
//import android.Manifest;
//import java.io.ByteArrayInputStream;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Calendar;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//import java.util.concurrent.Executor;
//
//public class AttendanceActivity extends AppCompatActivity {
//
//    private static final int IMAGE_CAPTURE_CODE = 102;
//    private final double HOSTEL_LAT = 18.486421; // Replace with your actual latitude
//    private final double HOSTEL_LNG = 73.81693; // Replace with your actual longitude
//    private FusedLocationProviderClient fusedLocationClient;
//    private EditText editTextName;
//    private ImageView imageViewCaptured;
//    private Button btnCapture, btnCheckAttendance;
//    private DatabaseHelper databaseHelper;
//    private Bitmap capturedBitmap;
//
//    static {
//        if (!OpenCVLoader.initDebug()) {
//            throw new RuntimeException("OpenCV initialization failed!");
//        }
//    }
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.attendance_layout);
//
//        editTextName = findViewById(R.id.editTextName);
//        imageViewCaptured = findViewById(R.id.imageViewCaptured);
//        btnCapture = findViewById(R.id.btnCapture);
//        btnCheckAttendance = findViewById(R.id.btnMarkAttendance);
//
//        databaseHelper = new DatabaseHelper(this);
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//
//        btnCapture.setOnClickListener(view -> captureImage());
//        btnCheckAttendance.setOnClickListener(view -> markAttendance());
//    }
//
//    private void captureImage() {
//        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, IMAGE_CAPTURE_CODE);
//        } else {
//            Toast.makeText(this, "Camera not available!", Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK && data != null) {
//            Bundle extras = data.getExtras();
//            capturedBitmap = (Bitmap) extras.get("data");
//            imageViewCaptured.setImageBitmap(capturedBitmap);
//        }
//    }
//    private void markAttendance() {
//        String username = editTextName.getText().toString().trim();
//        if (!isWithinAllowedTimeWindow()) {
//            Toast.makeText(this, "Attendance window is closed. Please try again between 8 PM and 9 PM.", Toast.LENGTH_LONG).show();
//            return;
//        }
//
//        if (username.isEmpty()) {
//            Toast.makeText(this, "Enter username!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (capturedBitmap == null) {
//            Toast.makeText(this, "Capture an image first!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        // 🔒 Check location permission first
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
//            return;
//        }
//
//        // 🔍 Get current location and check distance
//        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
//            if (location != null) {
//                float[] results = new float[1];
//                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
//                        HOSTEL_LAT, HOSTEL_LNG, results);
//                float distanceInMeters = results[0];
//
//                if (distanceInMeters > 100) {
//                    Toast.makeText(this, "You are not in hostel", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//
//                // 🟢 User is inside hostel — proceed with original attendance logic
//                databaseHelper.getUserPhoto(username, new DatabaseHelper.OnByteArrayLoadedListener() {
//                    @Override
//                    public void onByteArrayLoaded(byte[] storedImageData) {
//                        if (storedImageData == null) {
//                            runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "User not found!", Toast.LENGTH_SHORT).show());
//                            return;
//                        }
//
//                        Bitmap storedBitmap = BitmapFactory.decodeByteArray(storedImageData, 0, storedImageData.length);
//
//                        if (storedBitmap == null) {
//                            runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Error decoding stored image!", Toast.LENGTH_SHORT).show());
//                            return;
//                        }
//
//                        new CompareFacesTask(username, storedBitmap, capturedBitmap).execute();
//                    }
//                });
//
//            } else {
//                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private class CompareFacesTask extends AsyncTask<Void, Void, Boolean> {
//        private String username;
//        private Bitmap storedBitmap;
//        private Bitmap capturedBitmap;
//
//        CompareFacesTask(String username, Bitmap storedBitmap, Bitmap capturedBitmap) {
//            this.username = username;
//            this.storedBitmap = storedBitmap;
//            this.capturedBitmap = capturedBitmap;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... voids) {
//            return compareImagesORB(storedBitmap, capturedBitmap);
//        }
//
//        @Override
//        protected void onPostExecute(Boolean isMatch) {
//            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
//
//            if (isMatch) {
//                databaseHelper.markAttendance(username, currentDate, "P");
//                Toast.makeText(AttendanceActivity.this, "Attendance Marked: P", Toast.LENGTH_SHORT).show();
//            } else {
//                // Face failed → trigger fingerprint fallback
//                BiometricManager biometricManager = BiometricManager.from(AttendanceActivity.this);
//                if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
//                    promptFingerprintFallback(username, currentDate);
//                } else {
//                    // No fingerprint available → mark Absent
//                    databaseHelper.markAttendance(username, currentDate, "A");
//                    Toast.makeText(AttendanceActivity.this, "Face & Fingerprint failed. Marked: A", Toast.LENGTH_SHORT).show();
//                }
//
//            }
//
//        }
//
//    }
//
//    private Mat preprocessImage(Bitmap bitmap) {
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
//        Imgproc.resize(mat, mat, new Size(150, 150)); // Increase size for better accuracy
//        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY); // Convert to grayscale
//        return mat;
//    }
//
//    private boolean compareImagesORB(Bitmap storedBitmap, Bitmap capturedBitmap) {
//        Mat img1 = preprocessImage(storedBitmap);
//        Mat img2 = preprocessImage(capturedBitmap);
//
//        ORB orb = ORB.create(500);  // Detect 500 keypoints
//        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
//        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
//        Mat descriptors1 = new Mat();
//        Mat descriptors2 = new Mat();
//
//        orb.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
//        orb.detectAndCompute(img2, new Mat(), keypoints2, descriptors2);
//
//        if (descriptors1.empty() || descriptors2.empty()) {
//            return false;
//        }
//
//                if (descriptors1.empty() || descriptors2.empty()) {
//            Log.e("FaceMatch", "Descriptors are empty! Image may not have enough keypoints.");
//            return false;
//        }
//
//        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
//        List<MatOfDMatch> knnMatches = new ArrayList<>();
//        matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2);
//
//        // Lowe's Ratio Test to filter good matches
//        float ratioThreshold = 0.8f; // Looser threshold for real-world conditions
//        int goodMatches = 0;
//        for (MatOfDMatch matOfDMatch : knnMatches) {
//            DMatch[] matches = matOfDMatch.toArray();
//            if (matches.length >= 2 && matches[0].distance < ratioThreshold * matches[1].distance) {
//                goodMatches++;
//            }
//        }
//
//        Log.d("FaceMatch", "Good Matches: " + goodMatches);
//
//        // 🔹 Step 2: Histogram Comparison for Backup Matching
//        double similarityScore = compareHistograms(descriptors1, descriptors2);
//        Log.d("FaceMatch", "Histogram Similarity Score: " + similarityScore);
//
//        // ✅ Improved Condition: ORB Matching + Histogram Similarity
//        return goodMatches > 25 || similarityScore > 0.75;
//    }
//        private double compareHistograms(Mat img1, Mat img2) {
//        Mat hist1 = new Mat();
//        Mat hist2 = new Mat();
//        // Compute histograms
//        Imgproc.calcHist(Collections.singletonList(img1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
//        Imgproc.calcHist(Collections.singletonList(img2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));
//        // Normalize histograms
//        Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
//        Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);
//        return Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
//    }
//    private void promptFingerprintFallback(String username, String currentDate) {
//        Executor executor = ContextCompat.getMainExecutor(this);
//        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
//            @Override
//            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
//                super.onAuthenticationSucceeded(result);
//                Toast.makeText(AttendanceActivity.this, "Fingerprint Verified!", Toast.LENGTH_SHORT).show();
//                databaseHelper.markAttendance(username, currentDate, "P");
//            }
//
//            @Override
//            public void onAuthenticationFailed() {
//                super.onAuthenticationFailed();
//                Toast.makeText(AttendanceActivity.this, "Fingerprint not recognized!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onAuthenticationError(int errorCode, CharSequence errString) {
//                super.onAuthenticationError(errorCode, errString);
//                Toast.makeText(AttendanceActivity.this, "Biometric error: " + errString, Toast.LENGTH_SHORT).show();
//                databaseHelper.markAttendance(username, currentDate, "A");
//            }
//        });
//
//        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
//                .setTitle("Fingerprint Authentication")
//                .setSubtitle("Face not recognized. Use fingerprint to verify identity.")
//                .setNegativeButtonText("Cancel")
//                .build();
//
//        biometricPrompt.authenticate(promptInfo);
//    }
//    private boolean isWithinAllowedTimeWindow() {
//        Calendar now = Calendar.getInstance();
//        int hour = now.get(Calendar.HOUR_OF_DAY);
//        int minute = now.get(Calendar.MINUTE);
//
//        // Check if time is between 8:00 PM and 9:00 PM
//        return (hour == 14) || (hour == 21 && minute == 0);  // 20 = 8 PM, allow till 9:00 PM sharp
//    }
//}
package com.example.myapplication;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityDashboardBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.*;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.ORB;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import java.util.concurrent.Executor;
public class AttendanceActivity extends DrawerBaseActivity {
    private static final int IMAGE_CAPTURE_CODE = 102;
    private final double HOSTEL_LAT = 18.486421;
    private final double HOSTEL_LNG = 73.81693;
    private FusedLocationProviderClient fusedLocationClient;
    private EditText editTextName;
    private ImageView imageViewCaptured;
    private Button btnCapture, btnCheckAttendance,btnViewAttendance;
    private TextView textViewAttendanceStatus;
    private DatabaseHelper databaseHelper;
    private Bitmap capturedBitmap;
    private Button btnDownloadPDF,btnDownloadAttendance;
    ActivityDashboardBinding activityDashboardBinding;
    static {
        if (!OpenCVLoader.initDebug()) {
            throw new RuntimeException("OpenCV initialization failed!");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityDashboardBinding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(activityDashboardBinding.getRoot());
        getLayoutInflater().inflate(R.layout.attendance_layout, activityDashboardBinding.contentFrame, true);
        editTextName = findViewById(R.id.editTextName);
        imageViewCaptured = findViewById(R.id.imageViewCaptured);
        btnCapture = findViewById(R.id.btnCapture);
        btnCheckAttendance = findViewById(R.id.btnMarkAttendance);
        textViewAttendanceStatus = findViewById(R.id.textViewAttendanceStatus);
        btnViewAttendance=findViewById(R.id.btnViewAttendance);
        databaseHelper = new DatabaseHelper(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        btnCapture.setOnClickListener(view -> captureImage());
        btnCheckAttendance.setOnClickListener(view -> markAttendance());
//        btnViewAttendance.setOnClickListener(view -> updateAttendanceStatus());
        btnViewAttendance.setOnClickListener(view -> {
            String username = editTextName.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(this, "Enter username to view attendance!", Toast.LENGTH_SHORT).show();
            } else {
                updateAttendanceStatus(username);
            }
        });
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
        btnDownloadPDF=findViewById(R.id.btnDownloadAttendance);
        btnDownloadPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAttendanceAsPDF();
            }
        });
        btnDownloadAttendance = findViewById(R.id.btnDownloadAttendance); // Add this line
    //    btnDownloadAttendance.setOnClickListener(view -> downloadAttendanceAsPdf());
    }
    private void downloadAttendanceAsPDF() {
        String username = editTextName.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Enter username first!", Toast.LENGTH_SHORT).show();
            return;
        }
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());
        databaseHelper.getMonthlyAttendanceDataFromFirebase(username, month, new DatabaseHelper.OnResultListener<List<String[]>>() {
            @Override
            public void onSuccess(List<String[]> data) {
                databaseHelper.getMonthlyAttendancePercentageFromFirebase(username, month, new DatabaseHelper.OnResultListener<Float>() {
                    @Override
                    public void onSuccess(Float percentage) {
                        createPDF(username, data, percentage);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(AttendanceActivity.this, "Failed to get percentage", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(AttendanceActivity.this, "Failed to get attendance data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void createPDF(String username, List<String[]> data, float percentage) {
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 600, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        paint.setTextSize(12f);
        int y = 25;
        canvas.drawText("Attendance Report for: " + username, 10, y, paint);
        y += 20;
        for (String[] record : data) {
            canvas.drawText("Date: " + record[0] + " | Status: " + record[1], 10, y, paint);
            y += 20;
        }
        y += 20;
        canvas.drawText("Attendance %: " + String.format(Locale.getDefault(), "%.2f", percentage), 10, y, paint);
        pdfDocument.finishPage(page);
        File dir = new File(Environment.getExternalStorageDirectory(), "AttendanceReports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = username + "_attendance.pdf";
        File file = new File(dir, fileName);
        try {
            pdfDocument.writeTo(new FileOutputStream(file));
            Toast.makeText(this, "PDF saved to " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving PDF", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, IMAGE_CAPTURE_CODE);
        } else {
            Toast.makeText(this, "Camera not available!", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == IMAGE_CAPTURE_CODE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            capturedBitmap = (Bitmap) extras.get("data");
            imageViewCaptured.setImageBitmap(capturedBitmap);
        }
    }
    private void markAttendance() {
        String username = editTextName.getText().toString().trim();
        if (username.isEmpty()) {
            Toast.makeText(this, "Enter username!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (capturedBitmap == null) {
            Toast.makeText(this, "Capture an image first!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                float[] results = new float[1];
                Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                        HOSTEL_LAT, HOSTEL_LNG, results);
                float distanceInMeters = results[0];
                if (distanceInMeters > 100) {
                    Toast.makeText(this, "You are not in hostel", Toast.LENGTH_SHORT).show();
                    return;
                }
                databaseHelper.getUserPhoto(username, new DatabaseHelper.OnByteArrayLoadedListener() {
                    @Override
                    public void onByteArrayLoaded(byte[] storedImageData) {
                        if (storedImageData == null) {
                            runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "User not found!", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        Bitmap storedBitmap = BitmapFactory.decodeByteArray(storedImageData, 0, storedImageData.length);
                        if (storedBitmap == null) {
                            runOnUiThread(() -> Toast.makeText(AttendanceActivity.this, "Error decoding stored image!", Toast.LENGTH_SHORT).show());
                            return;
                        }

                        new CompareFacesTask(username, storedBitmap, capturedBitmap).execute();
                    }
                });

            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class CompareFacesTask extends AsyncTask<Void, Void, Boolean> {
        private String username;
        private Bitmap storedBitmap, capturedBitmap;

        CompareFacesTask(String username, Bitmap storedBitmap, Bitmap capturedBitmap) {
            this.username = username;
            this.storedBitmap = storedBitmap;
            this.capturedBitmap = capturedBitmap;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            return compareImagesORB(storedBitmap, capturedBitmap);
        }

        @Override
        protected void onPostExecute(Boolean isMatch) {
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            if (isMatch) {
                databaseHelper.markAttendance(username, currentDate, "P");
                Toast.makeText(AttendanceActivity.this, "Attendance Marked: P", Toast.LENGTH_SHORT).show();
                // Removed: updateAttendanceStatus(username);
            } else {
                BiometricManager biometricManager = BiometricManager.from(AttendanceActivity.this);
                if (biometricManager.canAuthenticate() == BiometricManager.BIOMETRIC_SUCCESS) {
                    promptFingerprintFallback(username, currentDate);
                } else {
                    databaseHelper.markAttendance(username, currentDate, "A");
                    Toast.makeText(AttendanceActivity.this, "Face & Fingerprint failed. Marked: A", Toast.LENGTH_SHORT).show();
                    // Removed: updateAttendanceStatus(username);
                }
            }
        }

    }

    private void updateAttendanceStatus(String username) {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String month = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(new Date());

        databaseHelper.getDailyAttendance(username, today, new DatabaseHelper.OnResultListener<String>() {
            @Override
            public void onSuccess(String dailyStatus) {
                databaseHelper.getMonthlyAttendancePercentage(username, month, new DatabaseHelper.OnResultListener<Float>() {
                    @Override
                    public void onSuccess(Float monthlyPercent) {
                        String display = "Today's Attendance: " + dailyStatus + "\nMonthly Attendance: " + String.format(Locale.getDefault(), "%.2f", monthlyPercent) + "%";
                        textViewAttendanceStatus.setText(display);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        textViewAttendanceStatus.setText("Today's Attendance: " + dailyStatus + "\nMonthly Attendance: Error");
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                textViewAttendanceStatus.setText("Attendance data not available");
                e.printStackTrace();
            }
        });
    }


    private Mat preprocessImage(Bitmap bitmap) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        Imgproc.resize(mat, mat, new Size(150, 150));
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGBA2GRAY);
        return mat;
    }

    private boolean compareImagesORB(Bitmap storedBitmap, Bitmap capturedBitmap) {
        Mat img1 = preprocessImage(storedBitmap);
        Mat img2 = preprocessImage(capturedBitmap);

        ORB orb = ORB.create(500);
        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

        orb.detectAndCompute(img1, new Mat(), keypoints1, descriptors1);
        orb.detectAndCompute(img2, new Mat(), keypoints2, descriptors2);

        if (descriptors1.empty() || descriptors2.empty()) {
            Log.e("FaceMatch", "Descriptors are empty! Image may not have enough keypoints.");
            return false;
        }

        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        List<MatOfDMatch> knnMatches = new ArrayList<>();
        matcher.knnMatch(descriptors1, descriptors2, knnMatches, 2);

        float ratioThreshold = 0.8f;
        int goodMatches = 0;
        for (MatOfDMatch matOfDMatch : knnMatches) {
            DMatch[] matches = matOfDMatch.toArray();
            if (matches.length >= 2 && matches[0].distance < ratioThreshold * matches[1].distance) {
                goodMatches++;
            }
        }

        double similarityScore = compareHistograms(descriptors1, descriptors2);
        return goodMatches > 25 || similarityScore > 0.75;
    }

    private double compareHistograms(Mat img1, Mat img2) {
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        Imgproc.calcHist(Collections.singletonList(img1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
        Imgproc.calcHist(Collections.singletonList(img2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));
        Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
        Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);
        return Imgproc.compareHist(hist1, hist2, Imgproc.HISTCMP_CORREL);
    }

    private void promptFingerprintFallback(String username, String currentDate) {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(AttendanceActivity.this, "Fingerprint Verified!", Toast.LENGTH_SHORT).show();
                databaseHelper.markAttendance(username, currentDate, "P");
                updateAttendanceStatus(username);
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(AttendanceActivity.this, "Fingerprint not recognized!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(AttendanceActivity.this, "Biometric error: " + errString, Toast.LENGTH_SHORT).show();
                databaseHelper.markAttendance(username, currentDate, "A");
                updateAttendanceStatus(username);
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Fingerprint Authentication")
                .setSubtitle("Face not recognized. Use fingerprint to verify identity.")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}