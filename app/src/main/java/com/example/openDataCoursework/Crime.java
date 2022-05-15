package com.example.openDataCoursework;
import com.here.sdk.core.GeoCoordinates;
public class Crime {
    String name;
    GeoCoordinates coordinates;
    int score;

    Crime(String name, GeoCoordinates coordinates,int score){
        this.name=name;
        this.coordinates=coordinates;
        this.score=score;
    }

    public GeoCoordinates getCoordinates() {
        return coordinates;
    }

    public int getScore() {
        return score;
    }

    public String getName() {
        return name;
    }
}
