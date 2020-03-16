package com.example.coenelec390_uv_exposure.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    private Context context;

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, Config.DATABASE_NAME, null, Config.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_USER_INFO = "CREATE TABLE " + Config.USER_TABLE_NAME +      /** Defining user info table */
                " (" + Config.COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Config.COLUMN_USERNAME + " TEXT NOT NULL," +
                Config.COLUMN_PASSWORD + " TEXT NOT NULL," +
                Config.COLUMN_AGE + " TEXT NOT NULL," +
                Config.COLUMN_SKIN_TYPE + " TEXT NOT NULL," +
                Config.COLUMN_NOTIF + " TEXT NOT NULL)";

        db.execSQL(CREATE_TABLE_USER_INFO);     /** Creating the user info table */

        String CREATE_TABLE_UV_DATA = "CREATE TABLE " + Config.UV_TABLE_NAME +          /** Defining uv data table */
                " (" + Config.COLUMN_UV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Config.COLUMN_UV_USER_ID + " TEXT NOT NULL," +
                Config.COLUMN_DATE + " TEXT NOT NULL," +
                Config.COLUMN_UV_VALUE + " TEXT NOT NULL)";

        db.execSQL(CREATE_TABLE_UV_DATA);       /** Creating the uv data table */
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertUser(User user){

        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Config.COLUMN_USERNAME, user.getUsername() );
        contentValues.put(Config.COLUMN_PASSWORD, user.getPassword() );
        contentValues.put(Config.COLUMN_AGE, user.getAge() );
        contentValues.put(Config.COLUMN_SKIN_TYPE, user.getSkin() );
        contentValues.put(Config.COLUMN_NOTIF, user.getNotifications() );

        try {
            id = db.insertOrThrow(Config.USER_TABLE_NAME, null, contentValues);
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

        contentValues.put(Config.COLUMN_UV_USER_ID, uv.getUserId() );
        contentValues.put(Config.COLUMN_DATE, uv.getDate() );
        contentValues.put(Config.COLUMN_UV_VALUE, uv.getUv() );

        try {
            id = db.insertOrThrow(Config.UV_TABLE_NAME, null, contentValues);
        } catch (SQLException e) {
            Log.d(TAG, "EXCEPTION: " + e);                                      /** Catching any exception thrown and logging it*/
            Toast.makeText(context, " Operation Failed!: " + e, Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return id;                                                                    /**If no exception thrown, ID of the Row of the new record is returned */
    }
}
