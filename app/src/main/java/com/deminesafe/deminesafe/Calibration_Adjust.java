package com.deminesafe.deminesafe;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;

import AlertDialogs.Add_Preset_Isotope_AlertDialog;
import AlertDialogs.Yes_No_AlertDialog;
import Classes.Isotope;
import Detector.Calibration_Spectrometer;
import Detector.Microphone;
import Methods.Isotopes_ListViewAdapter;

public class Calibration_Adjust extends Fragment {

    // Attributes
    private Detector_Config_Activity myParentActivity_;
    private Context myContext_;

    private Microphone myMicRecord;

    private Isotopes_ListViewAdapter myListViewAdapter_;
    private ListView myListView;

    private Calibration_Spectrometer myCalibrationSpectrometer;

    private CountDownTimer myTimer;


    private float energyXpost;

    private Isotope minIsotope;
    private Isotope maxIsotope;
    private int pressedIsotope_;

    private Handler handler;
    private Runnable myRunnable;

    // Touch events
    private float last_down_x = 0;
    private float last_down_y = 0;
    private float last_up_x = 0;
    private float last_up_y = 0;
    private boolean steadyClick = false;
    private CountDownTimer myLongClickTimer;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calibration_adjust, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Detector_Config_Activity) getActivity();
        myContext_ = getActivity();

        pressedIsotope_= -1;

        myCalibrationSpectrometer = rootView.findViewById(R.id.spectrometer);
        ImageView myGrid = rootView.findViewById(R.id.gridLayout);
        myListView = rootView.findViewById(R.id.isotope_listview);
        FloatingActionButton add_floatingbutton = rootView.findViewById(R.id.add_button);

        // On Click listeners
        myGrid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (pressedIsotope_ >= 0) {

                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        // We get the x pos
                        energyXpost = myCalibrationSpectrometer.getPointerLine(event.getX());

                        if (pressedIsotope_ == 0) { //min pressed

                            if (maxIsotope == null) {

                            } else if (energyXpost >= myCalibrationSpectrometer.getMaxEnergyLine()) {
                                return false;
                            }

                            minIsotope.setX_pos(energyXpost);

                            // We save to shared pref its new x pos
                            myParentActivity_.getMySharedPrefs().updateIsotope(minIsotope);

                            // We update the x pos of the red line
                            myCalibrationSpectrometer.setMinEnergyLine(minIsotope);

                        } else { // max pressed

                            if (minIsotope == null) {

                            } else if (energyXpost <= myCalibrationSpectrometer.getMinEnergyLine()) {
                                return false;
                            }

                            maxIsotope.setX_pos(energyXpost);

                            // We save to shared pref its new x pos
                            myParentActivity_.getMySharedPrefs().updateIsotope(maxIsotope);

                            // We update the x pos of the red line
                            myCalibrationSpectrometer.setMaxEnergyLine(maxIsotope);
                        }

                        if (minIsotope != null && maxIsotope != null) {

                            // energy(width) = energy_delta*width + min_energy
                            double energy_delta = (maxIsotope.getX_pos() - minIsotope.getX_pos()) / (maxIsotope.getEnergy() - minIsotope.getEnergy());
                            double min_energy = maxIsotope.getX_pos() - (maxIsotope.getEnergy() * energy_delta);

                            myParentActivity_.getMySharedPrefs().saveEnergyWidth(energy_delta, min_energy);

                        }
                    }
                }

                return false;

                /*else{
                    // zoom, scroll mode
                    switch(event.getAction()){

                        case MotionEvent.ACTION_DOWN:{

                            last_down_x = event.getX();
                            last_down_y = event.getY();

                            checkForLongClick();

                            return true;
                        }case MotionEvent.ACTION_UP:{

                            if(myLongClickTimer != null) {
                                myLongClickTimer.cancel();
                            }

                            last_up_x = event.getX();
                            last_up_y = event.getY();

                            checkEvent();

                            return false;
                        }
                    }
                    return false;
                }
                */

            }
        });


        add_floatingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(maxIsotope == null || minIsotope == null) {
                    addIsotope();
                }else{
                    myParentActivity_.toastMessage("Maximum of two isotopes for calibration");
                }
            }
        });

        myListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(android.widget.AdapterView<?> parent, View view, int position, long id) {

                pressedIsotope_ = myListViewAdapter_.changeColor(position);

            }
        });

        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new Yes_No_AlertDialog(myContext_, "Delete Isotope", "Are you sure you want to remove " + myListViewAdapter_.getItem(position).getName(), new Yes_No_AlertDialog.RESPONSE() {
                    @Override
                    public void RESPONSE(boolean pressedOk) {
                        if(pressedOk) {
                            myParentActivity_.getMySharedPrefs().removeIsotope(myListViewAdapter_.getItem(position));
                            pressedIsotope_ = -1;
                            updateAdapter();
                        }
                    }
                });
                return true;
            }
        });

        // init preferences
        int minFrontPoints_ = myParentActivity_.getMySharedPrefs().getMinFrontPoints();
        final float noiseThreshold_ = myParentActivity_.getMySharedPrefs().getNoiseThreshold();
        float[] myBackgroundData = myParentActivity_.getMySharedPrefs().getBackgroundNoise();
        myCalibrationSpectrometer.setMinFrontPoints(minFrontPoints_);
        myCalibrationSpectrometer.setBackgroundData(myBackgroundData);

        // Classes Constructors
        myMicRecord = new Microphone(getActivity(), myCalibrationSpectrometer);

        // Update UI periodically
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                myCalibrationSpectrometer.setThresholdLineYPos(noiseThreshold_);

                // Start Recording Microphone
                myMicRecord.startAudioRecordingSafe();

                triggerDraw();

            }
        },1000);

        // Update Isotopes
        updateAdapter();

        return rootView;
    }

    /*

    private void checkEvent(){

        if(Math.abs(last_up_x - last_down_x) > 200){

            if(last_down_x < last_up_x){
                myCalibrationSpectrometer.scrollLeft();
            }else if(last_down_x > last_up_x){
                myCalibrationSpectrometer.scrollRight();
            }
        }else if(checkForSteadyClick()){

            if(steadyClick){
                myCalibrationSpectrometer.zoomIn(last_up_x);
            }else {
                steadyClick = true;
                return;
            }
        }
        steadyClick = false;
    }

    private boolean checkForSteadyClick(){
        return Math.abs(last_down_x - last_up_x) < 10 && Math.abs(last_down_y - last_up_y) < 10;
    }

    private void checkForLongClick(){

        myLongClickTimer = new CountDownTimer(2000, 2000) {

            public void onTick(long millisUntilFinished) {
            }

            public void onFinish() {

                myCalibrationSpectrometer.resetZoom();
            }

        }.start();
    }

    */

    private void triggerDraw(){

        myTimer = new CountDownTimer(3000000, 1000) {

            public void onTick(long millisUntilFinished) {

                myCalibrationSpectrometer.draw();

            }

            public void onFinish() {}

        }.start();
    }

    private void addIsotope(){

        double maxEnergy = 0;
        double minEnergy = 0;

        if(maxIsotope != null){
            maxEnergy = maxIsotope.getEnergy();
        }

        if(minIsotope != null){
            minEnergy = minIsotope.getEnergy();
        }

        new Add_Preset_Isotope_AlertDialog(myContext_, minEnergy, maxEnergy, new Add_Preset_Isotope_AlertDialog.RESPONSE() {
            @Override
            public void RESPONSE(String input1, String input2) {

                pressedIsotope_ = -1;

                Isotope newIsotope = new Isotope(input1,Double.parseDouble(input2));

                if(minIsotope == null && maxIsotope == null){
                    minIsotope = newIsotope;
                }else if(minIsotope != null){

                    if(newIsotope.getEnergy() > minIsotope.getEnergy()){

                        maxIsotope = newIsotope;
                        maxIsotope.setX_pos(minIsotope.getX_pos() + 10);
                    }else{
                        maxIsotope = minIsotope;
                        minIsotope = newIsotope;

                        if(maxIsotope.getX_pos() <= minIsotope.getX_pos()) {
                            maxIsotope.setX_pos(minIsotope.getX_pos() + 10);
                        }
                    }
                }

                myParentActivity_.getMySharedPrefs().addIsotope(newIsotope);
                updateAdapter();
            }
        });
    }

    private void updateAdapter(){

        // Import Maps from database
        ArrayList<Isotope> myIsotopes = myParentActivity_.getMySharedPrefs().getIsotopes();

        minIsotope = null;
        maxIsotope = null;

        if(myIsotopes != null){

            if(myIsotopes.size() == 1){
                minIsotope = myIsotopes.get(0);

            }else if(myIsotopes.size() == 2){

                minIsotope = myIsotopes.get(0);

                if(myIsotopes.get(1).getEnergy() < minIsotope.getEnergy()){
                    maxIsotope = minIsotope;
                    minIsotope = myIsotopes.get(1);
                }else{
                    maxIsotope = myIsotopes.get(1);
                }

                myIsotopes.clear();
                myIsotopes.add(minIsotope);
                myIsotopes.add(maxIsotope);

            }else{
                myParentActivity_.getMySharedPrefs().saveIsotopes(null);
            }
        }

        // Build custom adapter
        myListViewAdapter_ = new Isotopes_ListViewAdapter(getActivity(), myIsotopes);

        // Set adapter to the listview
        myListView.setAdapter(myListViewAdapter_);

        // We update the x pos of the red line
        myCalibrationSpectrometer.setMinEnergyLine(minIsotope);
        myCalibrationSpectrometer.setMaxEnergyLine(maxIsotope);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop recording audio
        myMicRecord.stopRecording();

        if(myTimer != null){
            myTimer.cancel();
        }

        if(myLongClickTimer != null) {
            myLongClickTimer.cancel();
        }

        // Stop UI handler
        handler.removeCallbacks(myRunnable);


    }
}
