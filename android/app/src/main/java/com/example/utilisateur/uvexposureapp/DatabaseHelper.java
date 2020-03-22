package com.example.utilisateur.uvexposureapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                DatabaseConfig.COLUMN_AGE + " INTEGER NOT NULL," +
                DatabaseConfig.COLUMN_SKIN_TYPE + " INT NOT NULL," + // boolean stored as 0 or 1
                DatabaseConfig.COLUMN_NOTIF + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_NEW_USER + " TEXT NOT NULL)";

        db.execSQL(CREATE_TABLE_USER_INFO);     /** Creating the user info table */

        String CREATE_TABLE_UV_DATA = "CREATE TABLE " + DatabaseConfig.UV_TABLE_NAME +          /** Defining uv data table */
                " (" + DatabaseConfig.COLUMN_UV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                DatabaseConfig.COLUMN_UV_USER_ID + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_DATE + " TEXT NOT NULL," +
                DatabaseConfig.COLUMN_UV_VALUE + " DOUBLE NOT NULL," +
                DatabaseConfig.COLUMN_UV_TIME + " DOUBLE NOT NULL)";

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
        contentValues.put(DatabaseConfig.COLUMN_NEW_USER, user.getNewUser());

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

    public long insertUV(UV uv){

        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(DatabaseConfig.COLUMN_UV_USER_ID, uv.getUserId() );
        contentValues.put(DatabaseConfig.COLUMN_DATE, uv.getDate() );
        contentValues.put(DatabaseConfig.COLUMN_UV_VALUE, uv.getUv() );
        contentValues.put(DatabaseConfig.COLUMN_UV_TIME, uv.getUvTime() );

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

    public List<UV> getAllUVData(int userId, String date) {
        SQLiteDatabase database = this.getReadableDatabase(); // We get the reference to the database to read
        Cursor cursor = null;

        try {
            // We select * UV values, groupBy their ID and having the user ID to show them per courses. Order them by uv time values
            cursor = database.query(DatabaseConfig.UV_TABLE_NAME, null, null, null, DatabaseConfig.COLUMN_UV_ID, DatabaseConfig.COLUMN_UV_USER_ID + "=" + "'" + userId + "' AND " + DatabaseConfig.COLUMN_DATE + "=" + "'" + date + "'", DatabaseConfig.COLUMN_UV_TIME);

            if (cursor != null && cursor.moveToFirst()) {
                cursor.moveToFirst();
                // Create a new list
                List<UV> uvList = new ArrayList<>();

                do {
                    // We get all the parameters
                    int id = cursor.getInt(cursor.getColumnIndex(DatabaseConfig.COLUMN_UV_ID));
                    String uvUserId = cursor.getString(cursor.getColumnIndex(DatabaseConfig.COLUMN_UV_USER_ID));
                    double time = cursor.getDouble(cursor.getColumnIndex(DatabaseConfig.COLUMN_UV_TIME));
                    double value = cursor.getDouble(cursor.getColumnIndex(DatabaseConfig.COLUMN_UV_VALUE));
                    UV uv = new UV(uvUserId, time, value, date);
                    uvList.add(uv);
                } while (cursor.moveToNext());
                return uvList;
            }
        } catch (SQLException exception) {
            Toast.makeText(context,"Error: " + exception.getMessage(), Toast.LENGTH_LONG);
        } finally {
            if (cursor != null)
                cursor.close();
            database.close();
        }
        return Collections.emptyList(); // Nothing to display
    }

}
