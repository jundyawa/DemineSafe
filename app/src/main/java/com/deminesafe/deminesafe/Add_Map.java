package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

import AlertDialogs.Save_Map_AlertDialog;
import Classes.LatLng;
import Classes.Map;
import Methods.GPS_Methods;
import REST_API.FirestoreRequest;

public class Add_Map extends Fragment {

    // Attributes
    private Main_Activity myParentActivity_;
    private Context myContext_;

    private MapView myMapView_;
    private IMapController myMapController_;
    private FloatingActionButton myFloatingButton_;
    private ProgressBar myProgressBar_;

    private ArrayList<Marker> myMarkers;
    private List<GeoPoint> myFrame;
    private Polygon lastPolygon;

    private GPS_Methods myGPS_Methods;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Get Context
        myContext_ = getActivity();

        // Init MapVIew
        Configuration.getInstance().load(myContext_, PreferenceManager.getDefaultSharedPreferences(myContext_));

        // Inflate Layout
        View rootView = inflater.inflate(R.layout.fragment_add_map, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Main_Activity) getActivity();

        // Fetch UI Objects
        ImageView myBackArrow = rootView.findViewById(R.id.back_image);
        ImageView mySaveImage = rootView.findViewById(R.id.save_image);
        TextView title_TEXTVIEW = rootView.findViewById(R.id.title_textview);
        myMapView_ = rootView.findViewById(R.id.myMapView);
        myFloatingButton_ = rootView.findViewById(R.id.location_button);
        myProgressBar_= rootView.findViewById(R.id.wait_progressBar);
        title_TEXTVIEW.setText("Add Map");

        // Init Map
        myMapController_ = myMapView_.getController();
        setMapSettings();

        // Set on click listeners
        myBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exit();
            }
        });

        mySaveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myMarkers.size() < 3){
                    myParentActivity_.toastMessage("At least 3 markers are needed to create a map");
                }else{
                    new Save_Map_AlertDialog(myContext_, new Save_Map_AlertDialog.RESPONSE() {
                        @Override
                        public void RESPONSE(String map_name) {

                            ArrayList<LatLng> allBounds = new ArrayList<>();

                            for(int i = 0 ; i < myMarkers.size() ; i++){
                                allBounds.add(new LatLng(myMarkers.get(i).getPosition()));
                            }

                            // Create new Map Objecct
                            Map newMap = new Map(map_name,allBounds);

                            // Save Locally
                            myParentActivity_.getMyDBAdapter().createMap(newMap);

                            // Save on the cloud
                            myParentActivity_.freezeUI();

                            FirestoreRequest myRequest = new FirestoreRequest(getActivity(), new FirestoreRequest.RESPONSE() {
                                @Override
                                public void RESPONSE(int responseCode) {
                                    myParentActivity_.unfreezeUI();
                                    exit();
                                }
                            });
                            myRequest.sendMap(newMap);
                        }
                    });
                }
            }
        });

        myFloatingButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGPSLocation();
            }
        });

        MapEventsOverlay myOverlay_ = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                dropMarker(p);
                return false;
            }
        });
        myMapView_.getOverlays().add(0,myOverlay_);

        // Start Process
        myFrame = new ArrayList<>();
        myFrame.add(new GeoPoint(85,0.1));
        myFrame.add(new GeoPoint(85,-179.9));
        myFrame.add(new GeoPoint(0,-179.9));
        myFrame.add(new GeoPoint(-85,-179.9));
        myFrame.add(new GeoPoint(-85,0.1));
        myFrame.add(new GeoPoint(-85,179.9));
        myFrame.add(new GeoPoint(0,179.9));
        myFrame.add(new GeoPoint(85,179.9));

        myMarkers = new ArrayList<>();

        return rootView;
    }

    private void exit(){
        // Save in SharedPref the current location
        myParentActivity_.getMySharedPrefs().saveLocationZoom(myMapView_.getMapCenter().getLatitude(), myMapView_.getMapCenter().getLongitude(), myMapView_.getZoomLevelDouble());

        // Exit fragment
        myParentActivity_.changeFragment(myParentActivity_.getMapsListFragment());
    }

    private void getGPSLocation(){

        // Disable button
        myFloatingButton_.setEnabled(false);

        // Display Spinner
        myProgressBar_.setVisibility(View.VISIBLE);

        // Start GPS
        myGPS_Methods = new GPS_Methods(myContext_, new GPS_Methods.RESPONSE() {
            @Override
            public void RESPONSE(LatLng myLatLng) {
                if(myLatLng != null){

                    // Move to the received coordinates
                    moveToLatLng(myLatLng,14);

                    // Stop the GPS
                    myGPS_Methods.stopGPSListener();
                }

                // Release the UI
                grantUI();
            }
        });
        myGPS_Methods.startGPSListener(0,0,25);
    }

    private void grantUI(){
        // Hide Spinner
        myProgressBar_.setVisibility(View.INVISIBLE);

        // Disable button
        myFloatingButton_.setEnabled(true);
    }


    private void moveToLatLng(LatLng newPosition, double zoom){

        myMapController_.setZoom(zoom);
        GeoPoint startPoint = new GeoPoint(newPosition.getMyLatitude(), newPosition.getMyLongitude());
        myMapController_.setCenter(startPoint);
    }

    private void setMapSettings(){

        // Set Map Type
        myMapView_.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        // Set control
        myMapView_.setBuiltInZoomControls(false);
        myMapView_.setMultiTouchControls(true);
        myMapView_.setMapOrientation(0);
        myMapView_.setMinZoomLevel(4.0);

        double zoom = myParentActivity_.getMySharedPrefs().getSavedZoom();
        LatLng myLatLng = myParentActivity_.getMySharedPrefs().getSavedLocation();

        moveToLatLng(myLatLng,zoom);
    }

    private void dropMarker(GeoPoint p){

        Marker newMarker = new Marker(myMapView_);
        newMarker.setPosition(p);
        newMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        newMarker.setIcon(getResources().getDrawable(R.drawable.marker_icon));

        newMarker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker, MapView mapView) {
                return false;
            }
        });
        newMarker.setDraggable(true);
        newMarker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                refreshMap();
            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });

        // Add to array
        myMarkers.add(newMarker);

        // Update Map
        refreshMap();

    }

    private void refreshMap(){

        // Draw Polygon if more than 2 markrs
        if(myMarkers.size() > 2){
            drawPolygon();
        }

        // Add to map
        for(int i = 0 ; i < myMarkers.size() ; ++i){
            myMapView_.getOverlays().add(myMarkers.get(i));
        }

        // Refresh overlay
        myMapView_.invalidate();
    }

    private void drawPolygon(){

        if(lastPolygon != null){
            myMapView_.getOverlays().remove(lastPolygon);
        }

        List<GeoPoint> geoPoints = new ArrayList<>();

        for(int i = 0 ; i < myMarkers.size() ; i++){
            geoPoints.add(myMarkers.get(i).getPosition());
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

        lastPolygon = polygon;

        myMapView_.getOverlayManager().add(polygon);
    }

    private void removeMarker(Marker marker){
        myMapView_.getOverlays().remove(marker);
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
