package com.example.openDataCoursework;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Use instance of this class to get access to the DataBase
public class DatabaseAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess instance;
    Cursor c = null;

    // Private constructor of the class
    private DatabaseAccess(Context context){
        this.openHelper = new DatabaseOpenHelper(context);

    }

    // to return the single instance of database
    public static DatabaseAccess getInstance(Context context){
        if (instance==null){
            instance=new DatabaseAccess(context);
        }
        return instance;
    }

    // to open the database

    public void open(){
        this.db = openHelper.getWritableDatabase();
    }

    // to close the database connection

    public void close(){
        if(db!=null){
            this.db.close();
        }
    }

    // now lets create a method to query and return the result from database
    // we will query the crime type by passing name

    public String getCrimeType(Float longitude) {
        c = db.rawQuery("select Crimetype from crimet where Longitude = '"+longitude+"'", new String[]{});
        StringBuffer buffer = new StringBuffer();
        while(c.moveToNext()){
            String crimeType = c.getString(0);
            buffer.append(""+crimeType);

        }
        return buffer.toString();
    }

    public String getCrimeTypebyCoordinate(Float longitude, Float latitude) {
        c = db.rawQuery("select Crimetype from crimet where Longitude = '"+longitude+"' and Latitude = '"+latitude+"'", new String[]{});
        StringBuffer buffer = new StringBuffer();
        while(c.moveToNext()){
            String crimeType = c.getString(0);
            buffer.append(""+crimeType);

        }
        return buffer.toString();
    }


}
