package com.example.coenelec390_uv_exposure.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {

    /** To do:
     * 1. How to fetch/store datapoints for insertDataPoint() method?
     * 2. What other methods are needed to insert or parse the database?
     * */
    private Context context;

    private static final String TAG = "DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, Config.DATABASE_NAME, null, Config.DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_UV_EXPOSURE = "CREATE TABLE " + Config.TABLE_NAME +
                " (" + Config.COLUMN_SAMPLE_NUMBER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                Config.COLUMN_TIME + " TEXT NOT NULL," +
                Config.COLUMN_INTENSITY + " TEXT NOT NULL)";

        db.execSQL(CREATE_TABLE_UV_EXPOSURE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    public long insertDataPoint(Datapoint datapoint){                   /** Method currently takes a Datapoint object as a parameter to pass the Time and Sensor Value of the iteration */

        long id = -1;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(Config.COLUMN_TIME, datapoint.getTime() );                  /** getTime() is a placeholder for method to get time when reading value */
        contentValues.put(Config.COLUMN_INTENSITY, datapoint.getSensorValue );        /** getSensorValue() is a placeholder for method to get current sensor reading */

        try {
            id = db.insertOrThrow(Config.TABLE_NAME, null, contentValues);
        } catch (SQLException e) {
            Log.d(TAG, "EXCEPTION: " + e);                                      /** Catching any exception thrown and logging it*/
            Toast.makeText(context, " Operation Failed!: " + e, Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
        return id;                                                                    /**If no exception thrown, ID of the Row of the new record is returned */
    }
}
