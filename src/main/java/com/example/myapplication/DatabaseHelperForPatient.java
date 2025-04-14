package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelperForPatient extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "PatientDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_PATIENTS = "patients"; // Updated Table Name
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelperForPatient(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PATIENTS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_EMAIL + " TEXT, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_PASSWORD + " TEXT)"; // Fixed missing space before "TEXT"
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        onCreate(db);
    }

    // Insert Patient Data
    public boolean addPatient(String name, String phone, String email, String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, phone);
        values.put(COLUMN_PHONE, email);
        values.put(COLUMN_PASSWORD, pass);

        long result = db.insert(TABLE_PATIENTS, null, values);
        db.close();

        if (result == -1) {
            Log.e("DB_ERROR", "Failed to insert into database");
            return false;
        } else {
            Log.d("DB_SUCCESS", "Patient registered successfully");
            return true;
        }
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PATIENTS + " WHERE name = ? AND password = ?",
                new String[]{username, password});

        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        return userExists;
    }
    public boolean checkOrUpdatePassword(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the user exists
        Cursor cursor = db.rawQuery("SELECT * FROM patients WHERE name = ?", new String[]{username});
        boolean userExists = cursor.getCount() > 0;
        cursor.close();

        if (userExists) {
            // Update the password
            ContentValues values = new ContentValues();
            values.put("PASSWORD", password);
            db.update("patients", values, "name = ?", new String[]{username});
        }

        return userExists;
    }
}
