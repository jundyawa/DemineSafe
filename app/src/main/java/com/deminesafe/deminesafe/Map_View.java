package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;

import java.util.ArrayList;
import java.util.List;

import AlertDialogs.Hazard_AlertDialog;
import Classes.LatLng;
import Classes.PathPoint;

public class Map_View extends Fragment {

    // Attributes
    private Map_Operation_Activity myParentActivity_;
    private Context myContext_;

    private MapView myMapView_;
    private IMapController myMapController_;

    private List<GeoPoint> myFrame;
    private Marker lastLocationMarker;

    private final double AREA_OF_DETECTION = 2.0;

    private Drawable myPathIcon;
    private Drawable myMarkerIcon;
    private Drawable myHazardIcon;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get Context
        myContext_ = getActivity();

        // Init MapVIew
        Configuration.getInstance().load(myContext_, PreferenceManager.getDefaultSharedPreferences(myContext_));

        // Inflate Layout
        View rootView = inflater.inflate(R.layout.fragment_map_view, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Map_Operation_Activity) getActivity();

        // Fetch UI Objects
        myMapView_ = rootView.findViewById(R.id.myMapView);

        // Init Map
        myMapController_ = myMapView_.getController();

        // On Click Listeners
        MapEventsOverlay myOverlay_ = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        });
        myMapView_.getOverlays().add(0,myOverlay_);

        // Define Frame
        myFrame = new ArrayList<>();
        myFrame.add(new GeoPoint(85,0.1));
        myFrame.add(new GeoPoint(85,-179.9));
        myFrame.add(new GeoPoint(0,-179.9));
        myFrame.add(new GeoPoint(-85,-179.9));
        myFrame.add(new GeoPoint(-85,0.1));
        myFrame.add(new GeoPoint(-85,179.9));
        myFrame.add(new GeoPoint(0,179.9));
        myFrame.add(new GeoPoint(85,179.9));

        // set icons
        myPathIcon = myContext_.getResources().getDrawable(R.drawable.path_marker);
        myMarkerIcon = myContext_.getResources().getDrawable(R.drawable.marker_icon);
        myHazardIcon = myContext_.getResources().getDrawable(R.drawable.hazard_marker);


        // Set Map Settings
        setMapSettings();

        return rootView;
    }

    public void receiveGPS(LatLng myLatLng){

        if(myParentActivity_.isInsideBounds(myLatLng) && myParentActivity_.isGpsClicked()){


            // Create PathPoint
            PathPoint myPathPoint = new PathPoint(myLatLng);

            // Add to map object
            myParentActivity_.getMyMap().addToPath(myPathPoint);

            // Add to local db
            myParentActivity_.getMyDBAdapter().createPathPoint(myParentActivity_.getMyMap().getCloudID(), myPathPoint);

            // Update Progress
            myParentActivity_.getMyMap().setProgress(Math.round(myParentActivity_.getMyMap().getPath().size()*AREA_OF_DETECTION*10.0 / myParentActivity_.getMyMap().getTotalArea())/10.0);

            // Update Map
            myParentActivity_.getMyDBAdapter().updateMap(myParentActivity_.getMyMap());

            // Paint the Map
            drawPath(myLatLng);

            // Animate to new location
            myMapController_.animateTo(myLatLng.getGeoPoint());
        }

        // Draw position marker
        dropLocationMarker(myLatLng.getGeoPoint());
    }


    private void drawPath(LatLng myLatLng){

        if(myLatLng == null){
            return;
        }
        Marker newMarker = new Marker(myMapView_);
        newMarker.setPosition(myLatLng.getGeoPoint());
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(myPathIcon);
        newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                return false;
            }
        });
        myMapView_.getOverlays().add(newMarker);

        // Refresh overlay
        myMapView_.invalidate();
    }

    private void dropLocationMarker(GeoPoint p){


        Marker newMarker = new Marker(myMapView_);
        newMarker.setPosition(p);
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(myMarkerIcon);
        newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                return false;
            }
        });

        myMapView_.getOverlays().remove(lastLocationMarker);

        lastLocationMarker = newMarker;

        myMapView_.getOverlays().add(lastLocationMarker);

        // Refresh overlay
        myMapView_.invalidate();

    }

    private void setMapSettings(){

        // Set Map Type
        myMapView_.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        // Set control
        myMapView_.setBuiltInZoomControls(false);
        myMapView_.setMultiTouchControls(true);
        myMapView_.setMapOrientation(0);

        // Get Rect Bounds
        LatLng myMax = myParentActivity_.getMyMap().getRectBounds().getMaxLatLng();
        LatLng myMin = myParentActivity_.getMyMap().getRectBounds().getMinLatLng();
        double startLat = myMin.getMyLatitude();
        double endLat = myMax.getMyLatitude();
        double startLng = myMin.getMyLongitude();
        double endLng = myMax.getMyLongitude();
        double Center_Lat = (endLat + startLat)/2.0;
        double Center_Lon = (endLng + startLng)/2.0;

        // Get Min Zoom
        float [] distance = new float[1];
        Location.distanceBetween(startLat,startLng,endLat,endLng,distance);
        double dist = distance[0]/1000;
        double minZOOM = (16 - Math.log(dist) / Math.log(2))*0.95;

        // Zoom on center
        myMapView_.setMinZoomLevel(minZOOM);
        myMapController_.setZoom(minZOOM);
        GeoPoint startPoint = new GeoPoint(Center_Lat, Center_Lon);
        myMapController_.setCenter(startPoint);

        // Limit map scrollable with rect bounds
        myMapView_.setScrollableAreaLimitLatitude(endLat,startLat,50);
        myMapView_.setScrollableAreaLimitLongitude(startLng,endLng,50);


        // Draw All bounds polygon
        List<GeoPoint> geoPoints = new ArrayList<>();

        for(int i = 0 ; i < myParentActivity_.getMyMap().getAllBounds().size() ; i++){
            geoPoints.add(myParentActivity_.getMyMap().getAllBounds().get(i).getGeoPoint());
        }

        Polygon polygon = new Polygon();    //see note below
        polygon.setFillColor(Color.argb(70,0,0,0));
        polygon.setStrokeWidth(1);
        polygon.setStrokeColor(Color.RED);
        polygon.setPoints(myFrame);

        //polygons supports holes too, points should be in a counter-clockwise order
        List<List<GeoPoint>> holes = new ArrayList<>();
        holes.add(geoPoints);
        polygon.setHoles(holes);

        myMapView_.getOverlayManager().add(polygon);

        // Get Last Path Data
        drawLastPath();

        // Get Last Hazard Data
       drawLastHazards();

    }

    private void drawLastHazards(){
        if(myParentActivity_.getMyMap().getHazards() == null){
            return;
        }
        if(myParentActivity_.getMyMap().getHazards().size() < 1){
            return;
        }
        List<Marker> myMakers = new ArrayList<>();
        Marker newMarker;
        for(int j = 0 ; j < myParentActivity_.getMyMap().getHazards().size(); ++j){
            newMarker = new Marker(myMapView_);
            newMarker.setPosition(myParentActivity_.getMyMap().getHazards().get(j).getLocation().getGeoPoint());
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setIcon(myHazardIcon);

            final int finalJ = j;
            newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    new Hazard_AlertDialog(myContext_,myParentActivity_.getMyMap().getHazards().get(finalJ));
                    return false;
                }
            });

            if(myParentActivity_.getMyMap().getHazards().get(j).getCloudSynced() == 0){
                newMarker.setDraggable(true);
                newMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {

                        // Change Location
                        myParentActivity_.getMyMap().getHazards().get(finalJ).setPosition(new LatLng(marker.getPosition()));

                        // Update local db
                        myParentActivity_.getMyDBAdapter().updateHazard(myParentActivity_.getMyMap().getHazards().get(finalJ));
                    }

                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }
                });
            }

            myMakers.add(newMarker);
        }
        myMapView_.getOverlays().addAll(myMakers);
        myMapView_.invalidate();
    }

    private void drawLastPath(){
        if(myParentActivity_.getMyMap().getPath() == null){
            return;
        }
        if(myParentActivity_.getMyMap().getPath().size() < 1){
            return;
        }
        List<Marker> myMakers = new ArrayList<>();
        Marker newMarker;
        for(int j = 0 ; j < myParentActivity_.getMyMap().getPath().size(); ++j){
            newMarker = new Marker(myMapView_);
            newMarker.setPosition(myParentActivity_.getMyMap().getPath().get(j).getLatlng().getGeoPoint());
            newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            newMarker.setIcon(myPathIcon);
            newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker, MapView mapView) {
                    return false;
                }
            });

            myMakers.add(newMarker);
        }
        myMapView_.getOverlays().addAll(myMakers);
        myMapView_.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();

        myMapView_.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        myMapView_.onPause();
    }
}
