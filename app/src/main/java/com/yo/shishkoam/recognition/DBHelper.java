package com.yo.shishkoam.recognition;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Created by User on 07.02.2017
 */

public class DBHelper extends SQLiteOpenHelper {

    private final static String AccelerometerX = "AccelerometerX";
    private final static String AccelerometerY = "AccelerometerY";
    private final static String AccelerometerZ = "AccelerometerZ";
    private final static String MotionX = "MotionX";
    private final static String MotionY = "MotionY";
    private final static String MotionZ = "MotionZ";
    private final static String LinX = "LinX";
    private final static String LinY = "LinY";
    private final static String LinZ = "LinZ";
    private final static String ID = "id";


    public DBHelper(Context context) {
        super(context, "myDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // create table
        db.execSQL("create table mytable ("
                + ID + " integer primary key autoincrement,"
                + AccelerometerX + " double,"
                + AccelerometerY + " double,"
                + AccelerometerZ + " double,"
                + MotionX + " double,"
                + MotionY + " double,"
                + MotionZ + " double,"
                + LinX + " double,"
                + LinY + " double,"
                + LinZ + " double"
                + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public long addData(float[] accelerometer, float[] motion, float[] lin) {
        SQLiteDatabase db = getWritableDatabase();
        // create object for data
        ContentValues cv = new ContentValues();
        // prepare pairs to insert
        cv.put(AccelerometerX, accelerometer[0]);
        cv.put(AccelerometerY, accelerometer[1]);
        cv.put(AccelerometerZ, accelerometer[2]);
        cv.put(MotionX, motion[0]);
        cv.put(MotionY, motion[1]);
        cv.put(MotionZ, motion[2]);
        cv.put(LinX, lin[0]);
        cv.put(LinY, lin[1]);
        cv.put(LinZ, lin[2]);
        //insert object to db
        long rowID = db.insert("mytable", null, cv);
        close();
        return rowID;
    }

    public String readData() {
        SQLiteDatabase db = getWritableDatabase();
        // request all data (cursor) from table
        Cursor c = db.query("mytable", null, null, null, null, null, null);

        StringBuilder builder = new StringBuilder();
        // check that table has data
        if (c.moveToFirst()) {
            // get column index by name
            int idColIndex = c.getColumnIndex(ID);
            int accelerometerXColIndex = c.getColumnIndex(AccelerometerX);
            int accelerometerYColIndex = c.getColumnIndex(AccelerometerY);
            int accelerometerZColIndex = c.getColumnIndex(AccelerometerZ);
            int motionXColIndex = c.getColumnIndex(MotionX);
            int motionYColIndex = c.getColumnIndex(MotionY);
            int motionZColIndex = c.getColumnIndex(MotionZ);
            int linXColIndex = c.getColumnIndex(LinX);
            int linYColIndex = c.getColumnIndex(LinY);
            int linZColIndex = c.getColumnIndex(LinZ);
            do {

                // get data by column indexes
                int id = c.getInt(idColIndex);
                float accelerometerX = c.getFloat(accelerometerXColIndex);
                float accelerometerY = c.getFloat(accelerometerYColIndex);
                float accelerometerZ = c.getFloat(accelerometerZColIndex);
                float motionX = c.getFloat(motionXColIndex);
                float motionY = c.getFloat(motionYColIndex);
                float motionZ = c.getFloat(motionZColIndex);
                float linX = c.getFloat(linXColIndex);
                float linY = c.getFloat(linYColIndex);
                float linZ = c.getFloat(linZColIndex);
                builder.append(id).append(";")
                        .append(accelerometerX).append(";")
                        .append(accelerometerY).append(";")
                        .append(accelerometerZ).append(";")
                        .append(motionX).append(";")
                        .append(motionY).append(";")
                        .append(motionZ).append(";")
                        .append(linX).append(";")
                        .append(linY).append(";")
                        .append(linZ).append("\n");
            } while (c.moveToNext());
        }
        c.close();
        close();
        return builder.toString();
    }



    public int clearDataBase() {
        SQLiteDatabase db = getWritableDatabase();
        int clearCount = db.delete("mytable", null, null);
        close();
        return clearCount;
    }
}
