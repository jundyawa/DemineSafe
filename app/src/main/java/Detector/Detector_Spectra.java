package Detector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.LinkedList;

public class Detector_Spectra extends View {

    // Canvas
    private int width, height;
    private float centerY;

    private short[] mySpectrogram_;
    private float maxAtSingleFreq;
    private int nbrOfDetection_;
    private float[] myBackgroundData_;


    // My Noise Threshold Line
    private float thresholdLine_;
    private int MinFrontPoints;

    private int averageWindow = 80;

    // Process Var
    private int MinFrontPointsIndex;
    private boolean peakDetected_;
    private int maxAmplitudePeak;
    private boolean inDraw;
    private int nbrOfSeconds_;

    private Paint mySpectrogramPaint;
    private Paint targetIsotopePaint_;

    private float targetIsotope1Line_;
    private String targetIsotope1Name_;
    private float targetIsotope2Line_;
    private String targetIsotope2Name_;
    private float targetIsotope3Line_;
    private String targetIsotope3Name_;
    private float tntIsotopeLine_;

    // Constant to resize the values
    private static final int shortMaxValue = Short.MAX_VALUE;
    private float resizeConstant = 0;

    // Scroll, Zoom variables
    private int start_x = 0;
    private int end_x = 0;

    private LinkedList<Float> myQueue;
    private float myQueueTotal;


    public Detector_Spectra(Context context) {
        super(context);
        init();
    }

    public Detector_Spectra(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Detector_Spectra(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // if canva change size
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        centerY = height / 2f;

        resizeConstant = (1.0f*width/shortMaxValue);
    }

    private void init(){

        // init spectrogram array
        mySpectrogram_ = new short[shortMaxValue];
        for (int i = 0; i < mySpectrogram_.length; ++i) {
            mySpectrogram_[i] = 0;
        }

        myQueue = new LinkedList<>();
        myQueueTotal = 0;

        // init process var
        inDraw = false;
        maxAtSingleFreq = 0;
        nbrOfDetection_ = 0;
        nbrOfSeconds_ = 0;
        start_x = 0;
        end_x = shortMaxValue - 2;


        // --- Spectrum ---
        mySpectrogramPaint = new Paint();
        mySpectrogramPaint.setColor(Color.parseColor("#3e86a0"));
        mySpectrogramPaint.setStrokeWidth(2.0f);
        mySpectrogramPaint.setTextSize(14);

        // --- Energy Line ---
        targetIsotopePaint_ = new Paint();
        targetIsotopePaint_.setColor(Color.GREEN);
        targetIsotopePaint_.setStrokeWidth(2.0f);
        targetIsotopePaint_.setTextSize(14);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mySpectrogram_ == null || resizeConstant == 0){
            return;
        }

        inDraw = true;

        // Reset Variables
        myQueue.clear();
        myQueueTotal = 0;
        maxAtSingleFreq = 0.2f;

        // Draw
        for(int i = start_x; i < end_x; ++i) {

            float y_val = movingAverage(mySpectrogram_[i]);

            if (y_val > 0) {

                float x_pos = i * resizeConstant;
                float y_pos = height - height * (y_val / maxAtSingleFreq);
                canvas.drawLine(x_pos, height, x_pos, y_pos, mySpectrogramPaint);
            }
        }

        canvas.drawText(String.valueOf(Math.round(100.0*nbrOfDetection_/(nbrOfSeconds_))/100.0),10,20,mySpectrogramPaint);


        if(tntIsotopeLine_ > 0) {
            float x_pos = (tntIsotopeLine_ - start_x) * resizeConstant;
            canvas.drawLine(x_pos, height, x_pos, 0, targetIsotopePaint_);
            canvas.drawText("TNT", x_pos + 5, 20, targetIsotopePaint_);
        }

        if(targetIsotope1Line_ > 0) {
            float x_pos = (targetIsotope1Line_ - start_x) * resizeConstant;
            canvas.drawLine(x_pos, height, x_pos, 0, targetIsotopePaint_);
            canvas.drawText(targetIsotope1Name_, x_pos + 5, 20, targetIsotopePaint_);
        }

        if(targetIsotope2Line_ > 0) {
            float x_pos = (targetIsotope2Line_ - start_x) * resizeConstant;
            canvas.drawLine(x_pos, height, x_pos, 0, targetIsotopePaint_);
            canvas.drawText(targetIsotope2Name_, x_pos + 5, 20, targetIsotopePaint_);
        }

        if(targetIsotope3Line_ > 0) {
            float x_pos = (targetIsotope3Line_ - start_x) * resizeConstant;
            canvas.drawLine(x_pos, height, x_pos, 0, targetIsotopePaint_);
            canvas.drawText(targetIsotope3Name_, x_pos + 5, 20, targetIsotopePaint_);
        }

        inDraw = false;
    }

    private float movingAverage(float val){

        if(myQueue == null){
            return 0;
        }

        myQueueTotal += val;
        myQueue.addLast(val);
        if(myQueue.size() > averageWindow){
            myQueueTotal -= myQueue.getFirst();
            myQueue.removeFirst();
        }

        return myQueueTotal/(1.0f*myQueue.size());
    }


    public void setSamples(short[] samples) {
        // -32768 to 32768

        MinFrontPointsIndex = 0;
        maxAmplitudePeak = 0;
        peakDetected_ = false;

        // Size of canvas
        for (short sample : samples) {
            detectPeak(sample);
        }

    }

    private void detectPeak(short y){

        if(thresholdLine_ > 0){
            // Positive Trigger
            if(y > thresholdLine_) {

                MinFrontPointsIndex++;

                if(y > maxAmplitudePeak) {
                    maxAmplitudePeak = y;
                }

                if(MinFrontPointsIndex >= MinFrontPoints && !peakDetected_) {
                    peakDetected_ = true;
                }

                return;
            }else{
                if(peakDetected_){
                    addDetection(maxAmplitudePeak);
                    maxAmplitudePeak = 0;
                    peakDetected_ = false;
                }
            }
        }else{
            // Negative Trigger
            if(y < thresholdLine_) {

                MinFrontPointsIndex++;


                if(y < maxAmplitudePeak) {
                    maxAmplitudePeak = 0 - y;
                }

                if(MinFrontPointsIndex >= MinFrontPoints && !peakDetected_) {
                    peakDetected_ = true;
                }

                return;
            }else{
                if(peakDetected_){
                    addDetection(maxAmplitudePeak);
                    maxAmplitudePeak = 0;
                    peakDetected_ = false;
                }
            }
        }

        MinFrontPointsIndex = 0;
    }

    public void addDetection(int myFreq) {

        if (mySpectrogram_ == null) {

            return;
        }

        if (myFreq < shortMaxValue - 2) {

            mySpectrogram_[myFreq] += 1;

            nbrOfDetection_++;

        }
    }

    public void setTNTIsotope(float energy_pos){

        tntIsotopeLine_ = energy_pos;
    }

    public void setTargetIsotope1Line(float energy_pos, String name){

        targetIsotope1Line_ = energy_pos;
        targetIsotope1Name_ = name;
    }

    public void setTargetIsotope2Line(float energy_pos, String name){

        targetIsotope2Line_ = energy_pos;
        targetIsotope2Name_ = name;
    }

    public void setTargetIsotope3Line(float energy_pos, String name){

        targetIsotope3Line_ = energy_pos;
        targetIsotope3Name_ = name;
    }

    public void setThresholdLineYPos(float y_pos_ratio) {
        this.thresholdLine_ = ((centerY - y_pos_ratio*height)/height)*shortMaxValue;
    }

    public void setMinFrontPoints(int minFrontPoints) {
        this.MinFrontPoints = minFrontPoints;
    }

    public void setBackgroundData(float[] myBackgroundData) {

        this.myBackgroundData_ = myBackgroundData;
    }

    public void draw(){

        nbrOfSeconds_++;

        for(int i = averageWindow/4; i < mySpectrogram_.length - averageWindow/4 ; ++i){

            for(int j = 0 ; j < averageWindow/4 ; ++j){
                mySpectrogram_[i-averageWindow/8+j] -= myBackgroundData_[i]*(1.0/(1+Math.abs(averageWindow/8 - j)));
            }
        }

        if (!inDraw) {
            postInvalidate();
        }
    }

    public void zoomIn(float center_x_pos){

        if(!inDraw) {

            int centerFreq = Math.round((center_x_pos / width)*(end_x - start_x) + start_x);
            int delta = Math.round((end_x - start_x)/4.0f);

            resizeConstant = resizeConstant*2.0f;

            start_x = centerFreq - delta;
            if (start_x < 0) {
                start_x = 0;
                end_x = 2*delta;
                return;
            }

            end_x = centerFreq + delta;
            if (end_x > shortMaxValue - 2) {
                start_x = shortMaxValue - 2*delta;
                end_x = shortMaxValue - 2;
            }

        }

    }

    public void scrollLeft(){
        if(!inDraw && start_x > 2) {

            int delta = Math.round((end_x - start_x) / 3);

            start_x = start_x - delta;

            if (start_x < 0) {
                end_x = end_x + start_x;
                start_x = 0;
            } else {
                end_x = end_x - delta;
            }
        }
    }

    public void scrollRight(){
        if(!inDraw && end_x < shortMaxValue - 3) {

            int delta = Math.round((end_x - start_x) / 3);

            end_x = end_x + delta;

            if (end_x > shortMaxValue) {
                start_x = start_x + (shortMaxValue - end_x);
                end_x = shortMaxValue;
            }else{
                start_x = start_x + delta;
            }
        }
    }

    public void resetZoom(){

        if(!inDraw) {
            resizeConstant = (1.0f*width/shortMaxValue);
            start_x = 0;
            end_x = shortMaxValue - 2;
        }
    }



}
