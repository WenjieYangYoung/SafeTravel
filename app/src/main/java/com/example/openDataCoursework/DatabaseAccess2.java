package com.example.openDataCoursework;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.here.sdk.core.GeoCoordinates;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;

// Use instance of this class to get access to the DataBase
public class DatabaseAccess2 {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase db;
    private static DatabaseAccess2 instance;
    Cursor c = null;

    // Private constructor of the class
    private DatabaseAccess2(Context context){
            this.openHelper = new DatabaseOpenHelper2(context);

    }


    // to return the single instance of database
    public static DatabaseAccess2 getInstance(Context context){
        if (instance==null){
            instance=new DatabaseAccess2(context);
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
    public ArrayList<ArrayList<GeoCoordinates>> getRegion(){
        ArrayList<ArrayList<GeoCoordinates>> list = new ArrayList<>();

        c = db.rawQuery("select DISTINCT Boundary from Crimeboundary ", new String[]{});
        while(c.moveToNext()) {
            String area= c.getString(0);
            ArrayList<GeoCoordinates> aux= new ArrayList<>();
            try {
                JSONArray arrayJson= new JSONArray(area);
                for(int i=0;i<arrayJson.length();i++){
                    Object auxString1 = arrayJson.get(i);
                    String auxString=auxString1.toString();
                    String firstC= auxString.replaceAll("[\\[\\]]","").split(",")[0];
                    String secondC= auxString.replaceAll("[\\[\\]]","").split(",")[1];
                    aux.add(new GeoCoordinates(Double.parseDouble(secondC),Double.parseDouble(firstC)));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            list.add(aux);
        }
        return  list;
    }
    public ArrayList<Integer> getDangerRate(){
        ArrayList<Integer> aux = new ArrayList<>();
        c = db.rawQuery("select dangerrate from areascore ", new String[]{});
        while(c.moveToNext()) {
            aux.add(Integer.valueOf(c.getString(0)));
        }

        return aux;
    }




}

