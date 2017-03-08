package com.oliverwang.fantasia;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 48oli on 3/4/2017.
 */

public class NearFieldDatabaseHelper  extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "Contacts.db";
    private static final String TABLE_NAME = "contact_table";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "NAME";
    private static final String COL_3 = "LAT";
    private static final String COL_4 = "LONG";
    private static final String COL_5 = "RADIUS";
    private static final String COL_6 = "TOPIC";
    private static final String COL_7 = "ACTIVATEMESSAGE";
    private static final String COL_8 = "DEACTIVATEMESSAGE";
    private static final String COL_9 = "STATE";

    private static NearFieldDatabaseHelper instance;

    public NearFieldDatabaseHelper(Context context) {
        //super(context, name, factory, version); //bump version if updating db format
        super(context, DATABASE_NAME, null, 1);
        SQLiteDatabase db = this.getWritableDatabase();  //FOR TEST ONLY!!
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //Create the table in the db
        //with the TABLE_NAME, column name, and attributes
        //ID - Integer, is a primary key with autoincrement
        //NAME - Text
        //LAT - Real
        //LONG - Real
        //RADIUS - Integer
        //TOPIC - Text
        //MESSAGE - Text
        //STATE - Integer
        //  0: off
        //  1: on
        //  2: off for 1 cycle
        //  3: off for 2 cycles
        //MAKE SURE HAVE SPACE AFTER CREATE TABLE OR MAY CRASH APP!!!
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, NAME TEXT, LAT REAL, LONG REAL, RADIUS INTEGER, TOPIC TEXT, ACTIVATEMESSAGE TEXT, DEACTIVATEMESSAGE TEXT, STATE INTEGER)");


    }

    public boolean insertData(String name, Double latitude, Double longitude, int radius, String topic, String activateMessage, String deactivateMessage){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2, name);
        contentValues.put(COL_3, latitude);
        contentValues.put(COL_4, longitude);
        contentValues.put(COL_5, radius);
        contentValues.put(COL_6, topic);
        contentValues.put(COL_7, activateMessage);
        contentValues.put(COL_8, deactivateMessage);
        contentValues.put(COL_9, 0);

        long result = db.insert(TABLE_NAME, null, contentValues);
        if (result == -1) return false;
        else return true;
    }
    public boolean updateState(int row, int value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_8, value);
        long result = db.update(TABLE_NAME, contentValues, COL_8 + "= ?", new String[] {Integer.toString(row)});
        if (result == -1) return false;
        else return true;
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("select * from " + TABLE_NAME, null);
        return res;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
