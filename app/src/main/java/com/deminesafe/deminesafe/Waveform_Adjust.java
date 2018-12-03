package com.deminesafe.deminesafe;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import Detector.Microphone;
import Detector.Waveform;

public class Waveform_Adjust extends Fragment {

    // Attributes
    private Detector_Config_Activity myParentActivity_;
    private Context myContext_;

    private Microphone myMicRecord;
    private Waveform myWaveform;

    private int minFrontPoints_;
    private float noiseThreshold_;

    private Handler handler;
    private Runnable myRunnable;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_waveform_adjust, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Detector_Config_Activity) getActivity();
        myContext_ = getActivity();

        // Init UI
        myWaveform = rootView.findViewById(R.id.waveform);
        final ImageView myGrid = rootView.findViewById(R.id.gridLayout);
        Button addButton = rootView.findViewById(R.id.more_button);
        Button subButton = rootView.findViewById(R.id.less_button);
        final TextView minfrontTEXTVIEW = rootView.findViewById(R.id.minfrontpoints_textview);

        // Click Listeners
        myGrid.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN){
                    noiseThreshold_ = event.getY()/myGrid.getHeight();
                    myWaveform.setThresholdLineYPos(noiseThreshold_);

                    // Save preferences
                    myParentActivity_.getMySharedPrefs().saveNoiseLevel(myWaveform.getThresholdLineYPos());

                }
                return false;
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(minFrontPoints_ < 100) {
                    minFrontPoints_ += 1;
                    minfrontTEXTVIEW.setText(String.valueOf(minFrontPoints_));
                    myWaveform.setMinFrontPoints(minFrontPoints_);

                    myParentActivity_.getMySharedPrefs().saveMinFrontPoints(myWaveform.getMinFrontPoints());

                }
            }
        });

        subButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(minFrontPoints_ > 1) {
                    minFrontPoints_ -= 1;
                    minfrontTEXTVIEW.setText(String.valueOf(minFrontPoints_));
                    myWaveform.setMinFrontPoints(minFrontPoints_);

                    myParentActivity_.getMySharedPrefs().saveMinFrontPoints(myWaveform.getMinFrontPoints());

                }
            }
        });

        // Set parameters of my waveform
        minFrontPoints_ = myParentActivity_.getMySharedPrefs().getMinFrontPoints();
        noiseThreshold_ = myParentActivity_.getMySharedPrefs().getNoiseThreshold();
        minfrontTEXTVIEW.setText(String.valueOf(minFrontPoints_));
        myWaveform.setMinFrontPoints(minFrontPoints_);

        // Classes Constructors
        myMicRecord = new Microphone(getActivity(), myWaveform);


        // Update UI periodically
        handler = new Handler();
        handler.postDelayed(myRunnable = new Runnable() {
            @Override
            public void run() {

                myWaveform.setThresholdLineYPos(noiseThreshold_);

                myMicRecord.startAudioRecordingSafe();
            }
        },1000);


        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Stop UI handler
        handler.removeCallbacks(myRunnable);

        // Stop recording audio
        myMicRecord.stopRecording();

    }
}
