package com.example.openDataCoursework;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.here.sdk.core.GeoCoordinates;

import java.util.ArrayList;

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
        System.out.println(db.getPath());
        System.out.println(db.isDatabaseIntegrityOk());
    }

    // to close the database connection

    public void close(){
        if(db!=null){
            this.db.close();
        }
    }

    // now lets create a method to query and return the result from database
    // we will query the crime type by passing name

    public ArrayList<Crime> getCrimeType() {
        c = db.rawQuery("select Crimetype,Coordinates,CrimeScore from Crimeboundary", new String[]{});
        ArrayList<Crime> crimeList= new ArrayList<>();
        while(c.moveToNext()){
            String crimeType = c.getString(0);
            String coord = c.getString(1).replaceAll("[()\\[\\]\\']", "");
            Double coord2=Double.parseDouble(coord.split(",")[1]);
            Double coord1=Double.parseDouble(coord.split(",")[0]);

            int crimeScore = Integer.parseInt(c.getString(2));

            crimeList.add(new Crime(crimeType,new GeoCoordinates(coord1,coord2),crimeScore));

        }
        return crimeList;
    }




}
