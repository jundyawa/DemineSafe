package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import Classes.LatLng;
import Classes.Map;
import Methods.GPS_Methods;
import Methods.Maps_DatabaseAdapter;
import Methods.Shared_Preferences_Methods;
import REST_API.FirestoreRequest;

public class Map_Operation_Activity extends AppCompatActivity {

    // Attributes
    private Maps_DatabaseAdapter myDBAdapter_;
    private Shared_Preferences_Methods mySharedPrefs_;

    private ProgressBar myProgressBar_;

    private Map myMap_;

    private boolean gpsClicked_;
    private boolean hazardClicked_;
    private boolean detectorClicked_;

    private int releasedColor;
    private int pressedColor;

    private ImageView settings;

    private ImageView gpsImage;
    private TextView gpsText;

    private ImageView detectorImage;
    private TextView detectorText;

    private ImageView hazardImage;
    private TextView hazardText;

    private final String MAP_VIEW_FRAGMENT = "Map_View";
    private final String DETECTOR_FRAGMENT = "Detector";
    private final String HAZARD_FRAGMENT = "Hazard";
    private String currentFragment_;

    private boolean exiting_;

    private LatLng myCoordinates_;

    private String MapName;
    private String MapCloudId;


    private GPS_Methods myGPS_Methods;
    private boolean gpsLooking_;
    private boolean gpsOn_;

    private Map_View myMapViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_operation);

        exiting_ = false;
        gpsLooking_ = true;
        gpsOn_ = false;

        // Fetch Intent
        Intent i = getIntent();
        MapName = i.getStringExtra("map_name");
        MapCloudId = i.getStringExtra("map_id");


        // Fetch UI Objects
        ImageView myBackArrow = findViewById(R.id.back_image);
        TextView title_TEXTVIEW = findViewById(R.id.title_textview);
        myProgressBar_= findViewById(R.id.wait_progressBar);
        settings = findViewById(R.id.settings_button);
        settings.setVisibility(View.INVISIBLE);
        title_TEXTVIEW.setText(MapName);

        myBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change Activity
                finish();
                Intent i = new Intent(Map_Operation_Activity.this,Detector_Config_Activity.class);
                i.putExtra("map_id",MapCloudId);
                i.putExtra("map_name",MapName);
                startActivity(i);
            }
        });

        // Init Bottom Navigation Bar
        releasedColor = ContextCompat.getColor(this,R.color.colorWhite);
        pressedColor = ContextCompat.getColor(this,R.color.colorAccent);

        gpsClicked_ = false;
        LinearLayout gpsLinLay = findViewById(R.id.gps_linlay);
        gpsImage = findViewById(R.id.gps_imageview);
        gpsText = findViewById(R.id.gps_textview);
        gpsLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gpsClicked_){
                    unclickGPS();
                }else{
                    clickGPS();
                }
            }
        });

        detectorClicked_ = false;
        LinearLayout detectorLinLay = findViewById(R.id.detector_linlay);
        detectorImage = findViewById(R.id.detector_imageview);
        detectorText = findViewById(R.id.detector_textview);
        detectorLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(detectorClicked_){

                }else{
                    clickDetector();
                }
            }
        });

        hazardClicked_ = false;
        LinearLayout hazardLinLay = findViewById(R.id.hazard_linlay);
        hazardImage = findViewById(R.id.hazard_imageview);
        hazardText = findViewById(R.id.hazard_textview);
        hazardLinLay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hazardClicked_){

                }else{
                    clickHazard();
                }
            }
        });

        // Init database
        myDBAdapter_ = new Maps_DatabaseAdapter(this);
        myDBAdapter_.open();

        // Get my map
        myMap_ = myDBAdapter_.getMapByCloudId(MapCloudId);

        // Init Shared Prefs
        mySharedPrefs_ = new Shared_Preferences_Methods(this);

        // Get Cloud data
        if(i.getBooleanExtra("fromConfig",false)){
            clickDetector();
            startGPS();
        }else{
            getData(MapCloudId);
        }
    }

    public void startGPS(){

        startProgressBar();

        // Start GPS
        myGPS_Methods = new GPS_Methods(this, new GPS_Methods.RESPONSE() {
            @Override
            public void RESPONSE(LatLng myLatLng) {
                if(myLatLng != null){

                    myCoordinates_ = myLatLng;

                    if(myMapViewFragment == null && currentFragment_.equals(MAP_VIEW_FRAGMENT)) {
                        myMapViewFragment = (Map_View) getFragmentManager().findFragmentByTag("Shown_Fragment");
                    }

                    if (myMapViewFragment != null) {

                        myMapViewFragment.receiveGPS(myLatLng);
                    }
                }else{
                    unclickGPS();
                }

                if(gpsLooking_){
                    gpsLooking_ = false;
                    if(myLatLng == null){
                        gpsOn_ = false;
                    }else{
                        gpsOn_ = true;
                    }
                    stopProgressBar();
                }
            }
        });
        myGPS_Methods.startGPSListener(1000,1,25);
    }

    public boolean getGPSState(){
        return gpsOn_;
    }

    public LatLng getMyCoordinates() {
        return myCoordinates_;
    }

    private void getData(final String MapCloudId){

        freezeUI();

        FirestoreRequest myRequest = new FirestoreRequest(this, new FirestoreRequest.RESPONSE() {
            @Override
            public void RESPONSE(int nbr_of_objects) {

                FirestoreRequest myRequest = new FirestoreRequest(getBaseContext(), new FirestoreRequest.RESPONSE() {
                    @Override
                    public void RESPONSE(int nbr_of_objects) {

                        // Get my map
                        myMap_ = myDBAdapter_.getMapByCloudId(MapCloudId);

                        // Start Fragment
                        changeFragment(MAP_VIEW_FRAGMENT);

                        startGPS();

                        unfreezeUI();
                    }
                });
                myRequest.getPathPoints(myMap_);
            }
        });
        myRequest.getHazards(myMap_);
    }

    public void clickGPS(){

        unclickDetector();
        unclickHazard();

        if(gpsLooking_){
            startProgressBar();
        }

        if(gpsOn_) {
            gpsImage.setColorFilter(pressedColor);
            gpsText.setTextColor(pressedColor);
            gpsClicked_ = true;
        }else{
            toastMessage("Waiting to receive a GPS signal");
        }

        if(currentFragment_ == null){
            changeFragment(MAP_VIEW_FRAGMENT);
            return;
        }
        if(!currentFragment_.equals(MAP_VIEW_FRAGMENT)) {
            changeFragment(MAP_VIEW_FRAGMENT);
        }
    }

    public void unclickGPS(){
        gpsImage.setColorFilter(releasedColor);
        gpsText.setTextColor(releasedColor);
        gpsClicked_ = false;
    }

    private void clickDetector(){

        unclickGPS();
        unclickHazard();
        stopProgressBar();

        settings.setVisibility(View.VISIBLE);
        detectorImage.setColorFilter(pressedColor);
        detectorText.setTextColor(pressedColor);
        detectorClicked_ = true;

        if(currentFragment_ == null){
            changeFragment(DETECTOR_FRAGMENT);
            return;
        }
        if(!currentFragment_.equals(DETECTOR_FRAGMENT)) {
            changeFragment(DETECTOR_FRAGMENT);
        }
    }

    private void unclickDetector(){

        settings.setVisibility(View.INVISIBLE);
        detectorImage.setColorFilter(releasedColor);
        detectorText.setTextColor(releasedColor);
        detectorClicked_ = false;
    }

    private void clickHazard(){

        unclickGPS();
        unclickDetector();

        if(gpsLooking_){
            startProgressBar();
        }

        hazardImage.setColorFilter(pressedColor);
        hazardText.setTextColor(pressedColor);
        hazardClicked_ = true;

        if(currentFragment_ == null){
            changeFragment(HAZARD_FRAGMENT);
            return;
        }
        if(!currentFragment_.equals(HAZARD_FRAGMENT)) {
            changeFragment(HAZARD_FRAGMENT);
        }
    }

    public void unclickHazard(){
        hazardImage.setColorFilter(releasedColor);
        hazardText.setTextColor(releasedColor);
        hazardClicked_ = false;
    }

    public boolean isInsideBounds(LatLng myLatLng){
        // thx to cffk

        int n = myMap_.getAllBounds().size();

        boolean result = false;

        for (int i = 0 ; i < n ; ++i) {

            int j = (i + 1) % n;

            double p0_lat = myLatLng.getMyLatitude();
            double p0_lng = myLatLng.getMyLongitude();

            LatLng p_i = myMap_.getAllBounds().get(i);
            LatLng p_j = myMap_.getAllBounds().get(j);

            if (
                // Does p0.y lies in half open y range of edge.
                // N.B., horizontal edges never contribute
                    ( (p_j.getMyLongitude() <= p0_lng && p0_lng < p_i.getMyLongitude()) ||
                            (p_i.getMyLongitude() <= p0_lng && p0_lng < p_j.getMyLongitude()) ) &&
                            // is p to the left of edge?
                            ( p0_lat < p_j.getMyLatitude() + (p_i.getMyLatitude() - p_j.getMyLatitude()) * (p0_lng - p_j.getMyLongitude()) /
                                    (p_i.getMyLongitude() - p_j.getMyLongitude()) )
                    )
                result = !result;
        }
        return result;
    }

    public void changeFragment(String myFragmentStr) {

        Fragment newFragment = null;
        myMapViewFragment = null;

        if (myFragmentStr.equals(MAP_VIEW_FRAGMENT)) {
            newFragment = new Map_View();
            currentFragment_ = MAP_VIEW_FRAGMENT;
        } else if (myFragmentStr.equals(DETECTOR_FRAGMENT)) {
            newFragment = new Detector();
            currentFragment_ = DETECTOR_FRAGMENT;
        }else if (myFragmentStr.equals(HAZARD_FRAGMENT)) {
            newFragment = new Add_Hazard();
            currentFragment_ = HAZARD_FRAGMENT;
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, newFragment, "Shown_Fragment").commit();
    }

    public Shared_Preferences_Methods getMySharedPrefs() {
        return mySharedPrefs_;
    }

    public Maps_DatabaseAdapter getMyDBAdapter(){
        return myDBAdapter_;
    }

    public Map getMyMap(){
        return myMap_;
    }

    public boolean isGpsClicked(){
        return gpsClicked_;
    }

    public void stopProgressBar(){
        myProgressBar_.setVisibility(View.INVISIBLE);
    }

    public void startProgressBar(){
        myProgressBar_.setVisibility(View.VISIBLE);
    }


    public void freezeUI(){

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        startProgressBar();
    }

    public void unfreezeUI(){

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        if(!gpsLooking_) {
            stopProgressBar();
        }
    }

    // Show message to user through a short pop up
    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {

        if (currentFragment_.equals(MAP_VIEW_FRAGMENT)) {
            if(!exiting_) {
                sendData();
                myGPS_Methods.stopGPSListener();
            }
        } else if (currentFragment_.equals(DETECTOR_FRAGMENT)) {
            clickGPS();
        }else if (currentFragment_.equals(HAZARD_FRAGMENT)) {
            clickGPS();
        }
    }

    private void sendData(){

        exiting_= true;

        freezeUI();

        FirestoreRequest myRequest = new FirestoreRequest(this, new FirestoreRequest.RESPONSE() {
            @Override
            public void RESPONSE(int nbr_of_objects) {

                FirestoreRequest myRequest = new FirestoreRequest(getBaseContext(), new FirestoreRequest.RESPONSE() {
                    @Override
                    public void RESPONSE(int nbr_of_objects) {

                        unfreezeUI();

                        // Change Activity
                        finish();
                        Intent i = new Intent(Map_Operation_Activity.this,Main_Activity.class);
                        i.putExtra("fromMapView",true);
                        startActivity(i);
                    }
                });
                myRequest.sendPathPoints(myMap_.getCloudID(),myMap_.getPath());
            }
        });
        myRequest.sendHazards(myMap_.getCloudID(),myMap_.getHazards());

    }

}
