package com.deminesafe.deminesafe;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
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
import Detector.Microphone;
import Detector.Detector_Spectra;
import Methods.Isotopes_ListViewAdapter;

public class Detector extends Fragment {

    // Attributes
    private Map_Operation_Activity myParentActivity_;
    private Context myContext_;

    private Microphone myMicRecord;
    private Detector_Spectra mySpectra;

    private CountDownTimer myTimer;

    private Handler handler;
    private Runnable myRunnable;

    private Isotopes_ListViewAdapter myListViewAdapter_;
    private ListView myListView;

    private double energy_delta;
    private double min_energy;


    private ArrayList<Isotope> myTargerIsotopes_;

    // Touch events
    private float last_down_x = 0;
    private float last_down_y = 0;
    private float last_up_x = 0;
    private float last_up_y = 0;
    private boolean steadyClick = false;
    private CountDownTimer myLongClickTimer;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detector, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Map_Operation_Activity) getActivity();
        myContext_ = getActivity();

        // init preferences
        int minFrontPoints_ = myParentActivity_.getMySharedPrefs().getMinFrontPoints();
        final float noiseThreshold_ = myParentActivity_.getMySharedPrefs().getNoiseThreshold();
        float[] myBackgroundData = myParentActivity_.getMySharedPrefs().getBackgroundNoise();
        double[] myEnergyWidth = myParentActivity_.getMySharedPrefs().getEnergyWidth();
        energy_delta = myEnergyWidth[1];
        min_energy = myEnergyWidth[0];

        myListView = rootView.findViewById(R.id.isotope_listview);
        FloatingActionButton add_floatingbutton = rootView.findViewById(R.id.add_button);

        myTargerIsotopes_ = new ArrayList<>();

        myListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new Yes_No_AlertDialog(myContext_, "Delete Isotope", "Are you sure you want to remove " + myListViewAdapter_.getItem(position).getName(), new Yes_No_AlertDialog.RESPONSE() {
                    @Override
                    public void RESPONSE(boolean pressedOk) {
                        if(pressedOk) {
                            removeIsotope(myListViewAdapter_.getItem(position));
                        }
                    }
                });
                return true;
            }
        });

        add_floatingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(myTargerIsotopes_.size() < 4) {
                    addIsotope();
                }else{
                    myParentActivity_.toastMessage("Maximum of two isotopes for calibration");
                }
            }
        });

        ImageView myGrid = rootView.findViewById(R.id.gridLayout);

        mySpectra = rootView.findViewById(R.id.spectrometer);

        myGrid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
        });


        mySpectra.setMinFrontPoints(minFrontPoints_);
        mySpectra.setBackgroundData(myBackgroundData);

        // Classes Constructors
        myMicRecord = new Microphone(getActivity(), mySpectra);

        // Update UI periodically
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                mySpectra.setThresholdLineYPos(noiseThreshold_);

                if(energy_delta == 0){

                    myParentActivity_.toastMessage("Calibrate your detector");

                }else{

                    updateAdapter();

                    // Start Recording Microphone
                    myMicRecord.startAudioRecordingSafe();


                    triggerDraw();
                }


            }
        }, 1000);

        return rootView;
    }

    private void removeIsotope(Isotope myIsotope){

        if(myTargerIsotopes_ == null){
            return;
        }

        if(myTargerIsotopes_.size() < 1){
            return;
        }

        for(int i = 0 ; i < myTargerIsotopes_.size() ; i++){
            if(myTargerIsotopes_.get(i).getName().equals(myIsotope.getName())){
                myTargerIsotopes_.remove(i);
                break;
            }
        }

        updateAdapter();
    }

    private void addIsotope(){


        new Add_Preset_Isotope_AlertDialog(myContext_,0,0, new Add_Preset_Isotope_AlertDialog.RESPONSE() {
            @Override
            public void RESPONSE(String input1, String input2) {

                Isotope newIsotope = new Isotope(input1,Double.parseDouble(input2));

                myTargerIsotopes_.add(newIsotope);

                updateAdapter();
            }
        });
    }

    private void updateAdapter(){

        // Build custom adapter
        myListViewAdapter_ = new Isotopes_ListViewAdapter(getActivity(), myTargerIsotopes_);

        // Set adapter to the listview
        myListView.setAdapter(myListViewAdapter_);

        // We update the x pos of the red line
        if(myTargerIsotopes_ == null){
            return;
        }

        if(myTargerIsotopes_.size() == 0){

            mySpectra.setTargetIsotope1Line(0,null);
            mySpectra.setTargetIsotope2Line(0,null);
            mySpectra.setTargetIsotope3Line(0,null);

        }else if(myTargerIsotopes_.size() == 1){

            float target1_x_pos = (float) (myTargerIsotopes_.get(0).getEnergy() * energy_delta + min_energy);

            mySpectra.setTargetIsotope1Line(target1_x_pos,myTargerIsotopes_.get(0).getName());
            mySpectra.setTargetIsotope2Line(0,null);
            mySpectra.setTargetIsotope3Line(0,null);
        }else if(myTargerIsotopes_.size() == 2){

            float target1_x_pos = (float) (myTargerIsotopes_.get(0).getEnergy() * energy_delta + min_energy);
            float target2_x_pos = (float) (myTargerIsotopes_.get(1).getEnergy() * energy_delta + min_energy);

            mySpectra.setTargetIsotope1Line(target1_x_pos,myTargerIsotopes_.get(0).getName());
            mySpectra.setTargetIsotope2Line(target2_x_pos,myTargerIsotopes_.get(1).getName());
            mySpectra.setTargetIsotope3Line(0,null);
        }else if(myTargerIsotopes_.size() == 3){

            float target1_x_pos = (float) (myTargerIsotopes_.get(0).getEnergy() * energy_delta + min_energy);
            float target2_x_pos = (float) (myTargerIsotopes_.get(1).getEnergy() * energy_delta + min_energy);
            float target3_x_pos = (float) (myTargerIsotopes_.get(2).getEnergy() * energy_delta + min_energy);

            mySpectra.setTargetIsotope1Line(target1_x_pos,myTargerIsotopes_.get(0).getName());
            mySpectra.setTargetIsotope2Line(target2_x_pos,myTargerIsotopes_.get(1).getName());
            mySpectra.setTargetIsotope3Line(target3_x_pos,myTargerIsotopes_.get(2).getName());
        }

        float tnt_x_pos = (float) (5269.0 * energy_delta + min_energy);
        mySpectra.setTNTIsotope(tnt_x_pos);

    }

    private void checkEvent(){

        if(Math.abs(last_up_x - last_down_x) > 200){

            if(last_down_x < last_up_x){
                mySpectra.scrollLeft();
            }else if(last_down_x > last_up_x){
                mySpectra.scrollRight();
            }
        }else if(checkForSteadyClick()){

            if(steadyClick){
                mySpectra.zoomIn(last_up_x);
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

                mySpectra.resetZoom();
            }

        }.start();
    }


    private void triggerDraw(){

        myTimer = new CountDownTimer(3000000, 1000) {

            public void onTick(long millisUntilFinished) {

                mySpectra.draw();
            }

            public void onFinish() {}

        }.start();
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop UI handler
        if(handler != null) {
            handler.removeCallbacks(myRunnable);
        }

        if(myTimer != null){
            myTimer.cancel();
        }

        if(myLongClickTimer != null) {
            myLongClickTimer.cancel();
        }

        // Stop recording audio
        myMicRecord.stopRecording();

    }
}
