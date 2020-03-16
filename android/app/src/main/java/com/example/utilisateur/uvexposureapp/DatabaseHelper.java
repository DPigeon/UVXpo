package com.example.utilisateur.uvexposureapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DatabaseConfig.DATABASE_NAME, null, DatabaseConfig.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_USER_INFO = "CREATE TABLE " + DatabaseConfig.USER_TABLE_NAME +      /** Defining user info table */
                " (" + DatabaseConfig.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseConfig.COLUMN_USERNAME + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_PASSWORD + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_AGE + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_SKIN_TYPE + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_NOTIF + " TEXT NOT NULL)";

        db.execSQL(CREATE_TABLE_USER_INFO);     /** Creating the user info table */

        String CREATE_TABLE_UV_DATA = "CREATE TABLE " + DatabaseConfig.UV_TABLE_NAME +          /** Defining uv data table */
                " (" + DatabaseConfig.COLUMN_UV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseConfig.COLUMN_UV_USER_ID + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_DATE + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_UV_VALUE + " TEXT NOT NULL)";

        db.execSQL(CREATE_TABLE_UV_DATA);       /** Creating the uv data table */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertUser(User user){

        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseConfig.COLUMN_USERNAME, user.getUsername() );
        contentValues.put(DatabaseConfig.COLUMN_PASSWORD, user.getPassword() );
        contentValues.put(DatabaseConfig.COLUMN_AGE, user.getAge() );
        contentValues.put(DatabaseConfig.COLUMN_SKIN_TYPE, user.getSkin() );
        contentValues.put(DatabaseConfig.COLUMN_NOTIF, user.getNotifications() );

        try {
            id = db.insertOrThrow(DatabaseConfig.USER_TABLE_NAME, null, contentValues);
        } catch (SQLException e) {
            Log.d(TAG, "EXCEPTION: " + e);                                      /** Catching any exception thrown and logging it*/
            Toast.makeText(context, " Operation Failed!: " + e, Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return id;                                                                    /**If no exception thrown, ID of the Row of the new record is returned */
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public long insertUV(UV uv){

        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseConfig.COLUMN_UV_USER_ID, uv.getUserId() );
        contentValues.put(DatabaseConfig.COLUMN_DATE, uv.getDate() );
        contentValues.put(DatabaseConfig.COLUMN_UV_VALUE, uv.getUv() );

        try {
            id = db.insertOrThrow(DatabaseConfig.UV_TABLE_NAME, null, contentValues);
        } catch (SQLException e) {
            Log.d(TAG, "EXCEPTION: " + e);                                      /** Catching any exception thrown and logging it*/
            Toast.makeText(context, " Operation Failed!: " + e, Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return id;                                                                    /**If no exception thrown, ID of the Row of the new record is returned */
    }
}
