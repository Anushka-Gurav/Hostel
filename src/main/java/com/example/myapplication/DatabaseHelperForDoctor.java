package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelperForDoctor extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "DoctorDB";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_DOCTORS = "doctors";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_SPECIALIZATION = "specialization";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelperForDoctor(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_DOCTORS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_NAME + " TEXT, "
                + COLUMN_SPECIALIZATION + " TEXT, "
                + COLUMN_PHONE + " TEXT, "
                + COLUMN_EMAIL + " TEXT,"
                + COLUMN_PASSWORD +" TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCTORS);
        onCreate(db);
    }

    // Insert Doctor Data
    public boolean addDoctor(String name, String specialization, String phone, String email,String pass) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_SPECIALIZATION, specialization);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, pass);

        long result = db.insert(TABLE_DOCTORS, null, values);
        db.close();
        return result != -1;
    }
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM doctors WHERE name = ? AND password = ?",
                new String[]{username, password});

        boolean userExists = cursor.getCount() > 0;
        cursor.close();
        return userExists;
    }
    public boolean checkOrUpdatePassword(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the user exists
        Cursor cursor = db.rawQuery("SELECT * FROM doctors WHERE name = ?", new String[]{username});
        boolean userExists = cursor.getCount() > 0;
        cursor.close();

        if (userExists) {
            // Update the password
            ContentValues values = new ContentValues();
            values.put("PASSWORD", password);
            db.update("doctors", values, "name = ?", new String[]{username});
        }

        return userExists;
    }
}
