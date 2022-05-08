package com.example.openDataCoursework;


import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;

import com.here.sdk.core.Color;
import com.here.sdk.core.GeoBox;
import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoCorridor;
import com.here.sdk.core.GeoPolygon;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.LanguageCode;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapview.MapCamera;
import com.here.sdk.mapview.MapImage;
import com.here.sdk.mapview.MapImageFactory;
import com.here.sdk.mapview.MapMarker;
import com.here.sdk.mapview.MapPolygon;
import com.here.sdk.mapview.MapPolyline;
import com.here.sdk.mapview.MapView;
import com.here.sdk.routing.CalculateIsolineCallback;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.ChargingConnectorType;
import com.here.sdk.routing.ChargingStation;
import com.here.sdk.routing.EVCarOptions;
import com.here.sdk.routing.EVDetails;
import com.here.sdk.routing.Isoline;
import com.here.sdk.routing.IsolineCalculationMode;
import com.here.sdk.routing.IsolineOptions;
import com.here.sdk.routing.IsolineRangeType;
import com.here.sdk.routing.OptimizationMode;
import com.here.sdk.routing.PostAction;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Section;
import com.here.sdk.routing.SectionNotice;
import com.here.sdk.routing.Waypoint;
import com.here.sdk.search.Place;
import com.here.sdk.search.SearchCallback;
import com.here.sdk.search.SearchEngine;
import com.here.sdk.search.SearchError;
import com.here.sdk.search.SearchOptions;
import com.here.sdk.search.TextQuery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Routing {
    private final Context context;
    private final MapView mapView;
    private final List<MapMarker> mapMarkers = new ArrayList<>();
    private final List<MapPolyline> mapPolylines = new ArrayList<>();
    private final List<MapPolygon> mapPolygons = new ArrayList<>();
    private final RoutingEngine routingEngine;
    private final SearchEngine searchEngine;
    private GeoCoordinates startGeoCoordinates= new GeoCoordinates(50.898833,-1.404845);
    private GeoCoordinates destinationGeoCoordinates= new GeoCoordinates(50.911704,-1.403742);
    private final List<String> chargingStationsIDs = new ArrayList<>();
    public Routing(Context context,MapView mapView){
        this.context = context;
        this.mapView = mapView;
        MapCamera camera = mapView.getCamera();
        double distanceInMeters = 1000 * 10;
        camera.lookAt(new GeoCoordinates(	50.909698, 	-1.404351), distanceInMeters);

        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            throw new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }
        try {
            // Add search engine to search for places along a route.
            searchEngine = new SearchEngine();
        } catch (InstantiationErrorException e) {
            throw new RuntimeException("Initialization of SearchEngine failed: " + e.error.name());
        }
    }
    public void addExampleRoute() {
        chargingStationsIDs.clear();

        Waypoint startWaypoint = new Waypoint(startGeoCoordinates);
        Waypoint destinationWaypoint = new Waypoint(destinationGeoCoordinates);
        List<Waypoint> waypoints =
                new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));

        routingEngine.calculateRoute(waypoints, getEVCarOptions(), new CalculateRouteCallback() {
            @Override
            public void onRouteCalculated(RoutingError routingError, List<Route> list) {
                if (routingError != null) {
                    showDialog("Error while calculating a route: ", routingError.toString());
                    return;
                }

                // When routingError is nil, routes is guaranteed to contain at least one route.
                Route route = list.get(0);
                showRouteOnMap(route);
                logRouteViolations(route);
                //searchAlongARoute(route);
            }
        });
    }
    private EVCarOptions getEVCarOptions()  {
        EVCarOptions evCarOptions = new EVCarOptions();

        // The below three options are the minimum you must specify or routing will result in an error.
        evCarOptions.consumptionModel.ascentConsumptionInWattHoursPerMeter = 9;
        evCarOptions.consumptionModel.descentRecoveryInWattHoursPerMeter = 4.3;
        evCarOptions.consumptionModel.freeFlowSpeedTable = new HashMap<Integer, Double>() {{
            put(0, 0.239);
            put(27, 0.239);
            put(60, 0.196);
            put(90, 0.238);
        }};

        // Ensure that the vehicle does not run out of energy along the way and charging stations are added as additional waypoints.
        evCarOptions.ensureReachability = true;

        // The below options are required when setting the ensureReachability option to true.
        evCarOptions.routeOptions.optimizationMode = OptimizationMode.FASTEST;
        evCarOptions.routeOptions.alternatives = 0;
        evCarOptions.batterySpecifications.connectorTypes =
                new ArrayList<>(Arrays.asList(ChargingConnectorType.TESLA,
                        ChargingConnectorType.IEC_62196_TYPE_1_COMBO, ChargingConnectorType.IEC_62196_TYPE_2_COMBO));
        evCarOptions.batterySpecifications.totalCapacityInKilowattHours = 80.0;
        evCarOptions.batterySpecifications.initialChargeInKilowattHours = 10.0;
        evCarOptions.batterySpecifications.targetChargeInKilowattHours = 72.0;
        evCarOptions.batterySpecifications.chargingCurve = new HashMap<Double, Double>() {{
            put(0.0, 239.0);
            put(64.0, 111.0);
            put(72.0, 1.0);
        }};

        // Note: More EV options are availeble, the above shows only the minimum viable options.

        return evCarOptions;
    }



    // A route may contain several warnings, for example, when a certain route option could not be fulfilled.
    // An implementation may decide to reject a route if one or more violations are detected.
    private void logRouteViolations(Route route) {
        List<Section> sections = route.getSections();
        for (Section section : sections) {
            for (SectionNotice notice : section.getSectionNotices()) {
                Log.d("RouteViolations", "This route contains the following warning: " + notice.code);
            }
        }
    }


    private void showRouteOnMap(Route route) {
        clearMap();

        // Show route as polyline.
        GeoPolyline routeGeoPolyline = route.getGeometry();
        float widthInPixels = 20;
        MapPolyline routeMapPolyline = new MapPolyline(routeGeoPolyline,
                widthInPixels,
                Color.valueOf(0, 0.56f, 0.54f, 0.63f)); // RGBA

        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);

        GeoCoordinates startPoint =
                route.getSections().get(0).getDeparturePlace().mapMatchedCoordinates;
        GeoCoordinates destination =
                route.getSections().get(route.getSections().size() - 1).getArrivalPlace().mapMatchedCoordinates;

        // Draw a circle to indicate starting point and destination.
        addCircleMapMarker(startPoint, R.drawable.green_dot);
        addCircleMapMarker(destination, R.drawable.green_dot);
    }
    public void clearMap() {
        clearWaypointMapMarker();
        clearRoute();
        clearIsolines();
    }

    private void clearWaypointMapMarker() {
        for (MapMarker mapMarker : mapMarkers) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkers.clear();
    }

    private void clearRoute() {
        for (MapPolyline mapPolyline : mapPolylines) {
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        }
        mapPolylines.clear();
    }

    private void clearIsolines() {
        for (MapPolygon mapPolygon : mapPolygons) {
            mapView.getMapScene().removeMapPolygon(mapPolygon);
        }
        mapPolygons.clear();
    }
    private void addCircleMapMarker(GeoCoordinates geoCoordinates, int resourceId) {
        MapImage mapImage = MapImageFactory.fromResource(context.getResources(), resourceId);
        MapMarker mapMarker = new MapMarker(geoCoordinates, mapImage);
        mapView.getMapScene().addMapMarker(mapMarker);
        mapMarkers.add(mapMarker);
    }

    private void showDialog(String title, String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }



}
