package com.sehatsaathi.data.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "SehatSaathiPatients.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_PATIENTS = "patient_history";
    public static final String COL_KEY = "id"; // the row ID for SQLite
    public static final String COL_UNIQUE_ID = "unique_id";
    public static final String COL_NAME = "name";
    public static final String COL_DIAGNOSIS = "diagnosis";
    public static final String COL_CONFIDENCE = "confidence";
    public static final String COL_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_PATIENTS + " ("
                + COL_KEY + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_UNIQUE_ID + " TEXT, "
                + COL_NAME + " TEXT, "
                + COL_DIAGNOSIS + " TEXT, "
                + COL_CONFIDENCE + " TEXT, "
                + COL_DATE + " TEXT)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        onCreate(db);
    }
}
