package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import AlertDialogs.Text_AlertDialog;
import AlertDialogs.Yes_No_AlertDialog;
import Classes.Hazard;
import Methods.Maps_DatabaseAdapter;
import Methods.Shared_Preferences_Methods;

public class Main_Activity extends AppCompatActivity {

    // Attributes
    private String fragmentPosition_;

    private BottomNavigationView myNavView_;
    private ProgressBar myProgressBar_;

    private ArrayList<Hazard> myHazards;

    private Maps_DatabaseAdapter myDBAdapter_;
    private Shared_Preferences_Methods mySharedPrefs_;

    private final String DASHBOARD_FRAGMENT = "Dashboard";
    private final String MAPS_LIST_FRAGMENT = "Maps_List";
    private final String ADD_MAP_FRAGMENT = "Add_Map";

    private boolean uiFrozen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Init UI
        myNavView_ = findViewById(R.id.bottom_navigation);
        myProgressBar_ = findViewById(R.id.wait_progressBar);
        unfreezeUI();

        myNavView_.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                if(uiFrozen) {
                    return false;
                }

                switch (item.getItemId()) {
                    case R.id.action_home:
                        changeFragment(DASHBOARD_FRAGMENT);
                        break;
                    case R.id.action_maps:
                        changeFragment(MAPS_LIST_FRAGMENT);
                        break;
                    case R.id.action_help:
                        helpBox();
                        return false;
                }
                return true;
            }
        });

        // Init database
        myDBAdapter_ = new Maps_DatabaseAdapter(this);
        myDBAdapter_.open();

        // Get All hazards
        myHazards = myDBAdapter_.getAllHazards();

        // Init Shared Prefs
        mySharedPrefs_ = new Shared_Preferences_Methods(this);

        // Select Fragment
        Intent i = getIntent();
        boolean fromMapView = i.getBooleanExtra("fromMapView",false);

        if(fromMapView){
            myNavView_.setSelectedItemId(R.id.action_maps);
        }else{
            changeFragment(DASHBOARD_FRAGMENT);
        }
    }

    public void freezeUI(){

        myProgressBar_.setVisibility(View.VISIBLE);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);


        uiFrozen = true;
    }

    public void unfreezeUI(){

        myProgressBar_.setVisibility(View.INVISIBLE);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        uiFrozen = false;
    }

    public int getNbrOfHazards(){

        if(myHazards != null) {
            return myHazards.size();
        }else{
            return 0;
        }
    }

    public String getMapsListFragment() {
        return MAPS_LIST_FRAGMENT;
    }

    public String getAddMapFragment() {
        return ADD_MAP_FRAGMENT;
    }

    public Maps_DatabaseAdapter getMyDBAdapter() {
        return myDBAdapter_;
    }

    public Shared_Preferences_Methods getMySharedPrefs() {
        return mySharedPrefs_;
    }

    public void changeFragment(String myFragmentStr) {

        fragmentPosition_ = myFragmentStr;

        Fragment newFragment = null;

        if (myFragmentStr.equals(DASHBOARD_FRAGMENT)) {
            newFragment = new Dashboard();
        } else if (myFragmentStr.equals(MAPS_LIST_FRAGMENT)) {
            newFragment = new Maps_List();
        } else if (myFragmentStr.equals(ADD_MAP_FRAGMENT)) {
            newFragment = new Add_Map();
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, newFragment).commit();
    }

    private void helpBox(){

        String title = "";
        String subtitle = "";

        if(fragmentPosition_.equals(DASHBOARD_FRAGMENT)){
            title = getString(R.string.Dashboard_Title);
            subtitle = getString(R.string.Dashboard_SubTitle);

        }else if(fragmentPosition_.equals(MAPS_LIST_FRAGMENT)){
            title = getString(R.string.Maps_List_Title);
            subtitle = getString(R.string.Maps_List_SubTitle);

        } else if(fragmentPosition_.equals(ADD_MAP_FRAGMENT)){
            title = getString(R.string.Add_Map_Title);
            subtitle = getString(R.string.Add_Map_SubTitle);
        }

        new Text_AlertDialog(this,title,subtitle);
    }

    private void signOut(){

        new Yes_No_AlertDialog(this, "Sign Out", "Are you sure you want to sign out?", new Yes_No_AlertDialog.RESPONSE() {
            @Override
            public void RESPONSE(boolean pressedOk) {
                if(pressedOk){

                    myDBAdapter_.forceRefresh();

                    finish();
                    Intent i = new Intent(Main_Activity.this,Login_Activity.class);
                    i.putExtra("fromMain",true);
                    startActivity(i);
                }
            }
        });
    }

    // Show message to user through a short pop up
    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {

        if(fragmentPosition_.equals(DASHBOARD_FRAGMENT)){
            signOut();

        }else if(fragmentPosition_.equals(MAPS_LIST_FRAGMENT)){
            myNavView_.setSelectedItemId(R.id.action_home);

        } else if(fragmentPosition_.equals(ADD_MAP_FRAGMENT)){
            myNavView_.setSelectedItemId(R.id.action_maps);
        }

    }
}
