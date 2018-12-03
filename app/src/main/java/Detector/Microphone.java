package Detector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;

public class Microphone {

    // Attributes
    private Context myContext_;
    private Waveform myWaveform_;
    private Calibration_Spectrometer myCalibrationSpectrometer_;
    private Background_Spectra myBackgroundSpectra_;
    private Detector_Spectra myDectectorSpectra_;

    private int width_;

    private boolean mShouldContinue;
    private Thread mThread;


    // Constructor
    public Microphone(Activity myActivity, Waveform myWaveform) {

        this.myContext_ = myActivity;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        myActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        this.width_ = displayMetrics.widthPixels;
        this.myWaveform_ = myWaveform;
        this.myCalibrationSpectrometer_ = null;

    }

    public Microphone(Activity myActivity, Calibration_Spectrometer myCalibrationSpectrometer) {

        this.myContext_ = myActivity;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        myActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        this.width_ = displayMetrics.widthPixels;
        this.myWaveform_ = null;
        this.myCalibrationSpectrometer_ = myCalibrationSpectrometer;
    }

    public Microphone(Activity myActivity, Background_Spectra myBackgroundSpectra) {

        this.myContext_ = myActivity;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        myActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        this.width_ = displayMetrics.widthPixels;
        this.myWaveform_ = null;
        this.myBackgroundSpectra_ = myBackgroundSpectra;
    }

    public Microphone(Activity myActivity, Detector_Spectra myDectectorSpectra) {

        this.myContext_ = myActivity;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        myActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        this.width_ = displayMetrics.widthPixels;
        this.myWaveform_ = null;
        this.myDectectorSpectra_ = myDectectorSpectra;
    }


    // Function to start recording, but checks if permissions
    public void startAudioRecordingSafe() {
        if (ContextCompat.checkSelfPermission(myContext_, android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startRecording();
        } else {

            // Send user to app settings
            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", myContext_.getPackageName(), null);
            i.setData(uri);
            myContext_.startActivity(i);
        }
    }


    private void startRecording() {

        // if we are already running a thread
        if (mThread != null) {
            return;
        }

        mShouldContinue = true;

        // start a new thread
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                record();
            }
        });

        mThread.start();
    }

    public void stopRecording() {

        // if the thread is already dead
        if (mThread == null) {
            return;
        }

        mShouldContinue = false;
        mThread = null;
    }

    private void record() {

        // We obtain a buffer size dependant on our parameters
        int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
        int SAMPLE_RATE = 44100;
        int bufferSize_ = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING)/2;

        if(width_ < 600){
            bufferSize_ = bufferSize_/2;
        }

        // We define our AudioRecord activity
        int AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
        AudioRecord myAudioRecord_ = new AudioRecord(AUDIO_SOURCE, SAMPLE_RATE, CHANNEL_CONFIG,
                AUDIO_ENCODING, bufferSize_);


        // Set thread priority
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Set the Audio Data buffer Size
        short[] audioBuffer_ = new short[bufferSize_];

        // Start Recording
        myAudioRecord_.startRecording();

        while (mShouldContinue) {
            myAudioRecord_.read(audioBuffer_, 0, audioBuffer_.length);

            // Send the audio data back to main activity once the buffer is full
            if(myCalibrationSpectrometer_ != null){
                myCalibrationSpectrometer_.setSamples(audioBuffer_);
            }else if(myWaveform_ != null){
                myWaveform_.setSamples(audioBuffer_);
            }else if(myBackgroundSpectra_ != null){
                myBackgroundSpectra_.setSamples(audioBuffer_);
            }else if(myDectectorSpectra_ != null){
                myDectectorSpectra_.setSamples(audioBuffer_);
            }
        }

        myAudioRecord_.stop();
        myAudioRecord_.release();
    }
}
