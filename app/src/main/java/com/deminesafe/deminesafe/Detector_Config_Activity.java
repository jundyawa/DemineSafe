package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import Methods.Shared_Preferences_Methods;

public class Detector_Config_Activity extends AppCompatActivity {

    private Shared_Preferences_Methods mySharedPrefs_;

    private final String WAVEFORM_FRAGMENT = "Waveform";
    private final String BACKGROUND_NOISE_FRAGMENT = "Background";
    private final String CALIBRATION_FRAGMENT = "Spectrum";
    private String currentFragment_;

    private TextView titleTextView;

    private String MapName;
    private String MapCloudId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detector_config);

        // Fetch Intent
        Intent i = getIntent();
        MapName = i.getStringExtra("map_name");
        MapCloudId = i.getStringExtra("map_id");

        // Fetch UI Objects
        ImageView myBackArrow = findViewById(R.id.back_image);
        ImageView myNextArrow = findViewById(R.id.next_image);
        titleTextView = findViewById(R.id.title_textview);

        myBackArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        myNextArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextPressed();
            }
        });

        // Init Shared Prefs
        mySharedPrefs_ = new Shared_Preferences_Methods(this);


        changeFragment(WAVEFORM_FRAGMENT);
    }

    public Shared_Preferences_Methods getMySharedPrefs() {
        return mySharedPrefs_;
    }

    public void changeFragment(String myFragmentStr) {

        Fragment newFragment = null;

        if (myFragmentStr.equals(WAVEFORM_FRAGMENT)) {
            newFragment = new Waveform_Adjust();
            titleTextView.setText("Step 1 : Trigger");
            currentFragment_ = WAVEFORM_FRAGMENT;
        } else if (myFragmentStr.equals(BACKGROUND_NOISE_FRAGMENT)) {
            newFragment = new Background_Noise_Adjust();
            titleTextView.setText("Step 2 : Background");
            currentFragment_ = BACKGROUND_NOISE_FRAGMENT;
        }else if (myFragmentStr.equals(CALIBRATION_FRAGMENT)) {
            newFragment = new Calibration_Adjust();
            titleTextView.setText("Step 3 : Calibration");
            currentFragment_ = CALIBRATION_FRAGMENT;
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, newFragment).commit();
    }

    public String getWAVEFORM_FRAGMENT() {
        return WAVEFORM_FRAGMENT;
    }

    // Show message to user through a short pop up
    public void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    public void onNextPressed(){

        if (currentFragment_.equals(WAVEFORM_FRAGMENT)) {
            changeFragment(BACKGROUND_NOISE_FRAGMENT);
        } else if (currentFragment_.equals(BACKGROUND_NOISE_FRAGMENT)) {
            changeFragment(CALIBRATION_FRAGMENT);

        }else if (currentFragment_.equals(CALIBRATION_FRAGMENT)) {
            finish();
            Intent i = new Intent(Detector_Config_Activity.this,Map_Operation_Activity.class);
            i.putExtra("map_id",MapCloudId);
            i.putExtra("map_name",MapName);
            i.putExtra("fromConfig",true);
            startActivity(i);
        }
    }

    @Override
    public void onBackPressed() {
        if (currentFragment_.equals(WAVEFORM_FRAGMENT)) {
            finish();
            Intent i = new Intent(Detector_Config_Activity.this,Map_Operation_Activity.class);
            i.putExtra("map_id",MapCloudId);
            i.putExtra("map_name",MapName);
            i.putExtra("fromConfig",true);
            startActivity(i);
        } else if (currentFragment_.equals(BACKGROUND_NOISE_FRAGMENT)) {
            changeFragment(WAVEFORM_FRAGMENT);

        }else if (currentFragment_.equals(CALIBRATION_FRAGMENT)) {
            changeFragment(BACKGROUND_NOISE_FRAGMENT);
        }
    }
}
