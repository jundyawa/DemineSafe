package com.deminesafe.deminesafe;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Timer;

import Detector.Microphone;
import Detector.Background_Spectra;

public class Background_Noise_Adjust extends Fragment {

    // Attributes
    private Detector_Config_Activity myParentActivity_;
    private Context myContext_;

    private Microphone myMicRecord;

    private TextView timerTextview;

    private Background_Spectra myBackgroundSpectra_;

    private Handler handler;
    private Runnable myRunnable;
    private CountDownTimer myTimer;

    private boolean overload_;

    private int lastCount;

    private int timer_duration = 0; // in seconds

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_background_noise_adjust, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Detector_Config_Activity) getActivity();
        myContext_ = getActivity();

        // Fetch Ui objects
        myBackgroundSpectra_ = rootView.findViewById(R.id.spectrometer);
        timerTextview = rootView.findViewById(R.id.timer_textview);

        // init preferences
        final int minFrontPoints_ = myParentActivity_.getMySharedPrefs().getMinFrontPoints();
        final float noiseThreshold_ = myParentActivity_.getMySharedPrefs().getNoiseThreshold();
        myBackgroundSpectra_.setMinFrontPoints(minFrontPoints_);

        overload_ = false;
        lastCount= 0;

        // Classes Constructors
        myMicRecord = new Microphone(getActivity(), myBackgroundSpectra_);

        // Update UI periodically
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                myBackgroundSpectra_.setThresholdLineYPos(noiseThreshold_);
                myBackgroundSpectra_.setMinFrontPoints(minFrontPoints_);

                // Start Recording Microphone
                myMicRecord.startAudioRecordingSafe();

                timerStart();
            }
        },1000);

        return rootView;
    }

    private void timerStart(){

        myTimer = new CountDownTimer(600000, 1000) {

            public void onTick(long millisUntilFinished) {

                timerTextview.setText(getTime());
                timer_duration++;

                if(myBackgroundSpectra_.getNbrOfDetection() - lastCount > 1000){
                    overload_ = true;

                    myMicRecord.stopRecording();

                    myBackgroundSpectra_.stopped();

                    timerTextview.setTextColor(Color.RED);

                    myParentActivity_.toastMessage("Overload, adjust your trigger");

                    this.cancel();

                }else if(timer_duration % 10 == 9 && !overload_){

                    lastCount = myBackgroundSpectra_.getNbrOfDetection();

                    myParentActivity_.getMySharedPrefs().saveBackgroundNoise(myBackgroundSpectra_.getSpectraData(), timer_duration);

                }

            }

            public void onFinish() {
                timerTextview.setText("Done");
            }

        }.start();
    }

    public String getTime() {

        int hours;
        int minutes;
        int seconds;

        if(timer_duration >= 3600){
            hours = (int) Math.round(Math.floor(timer_duration/3600));
        }else{
            hours = 0;
        }

        if(timer_duration >= 60){
            minutes = (int) Math.round(Math.floor((timer_duration - hours*3600)/60));
        }else{
            minutes = 0;
        }

        seconds = timer_duration % 60;

        String secondsSTR = "";
        if(seconds > 9){
            secondsSTR = String.valueOf(seconds);
        }else{
            secondsSTR = "0" + String.valueOf(seconds);
        }

        return hours + "" + minutes + ":" + secondsSTR;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop recording audio 
        if(!overload_) {
            myMicRecord.stopRecording();

            if(myTimer != null) {
                myTimer.cancel();
            }
        }

        // Stop UI handler
        handler.removeCallbacks(myRunnable);

    }
}
