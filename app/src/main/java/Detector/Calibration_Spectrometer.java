package Detector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.LinkedList;

import Classes.Isotope;


public class Calibration_Spectrometer extends View {

    // Canvas
    private int width, height;
    private float centerY;

    private float[] mySpectrogram_;
    private float[] myBackgroundData_;

    private float maxAtSingleFreq;

    // My Noise Threshold Line
    private float thresholdLine_;

    // Var to know if we are drawing
    private int MinFrontPoints;
    private int MinFrontPointsIndex;
    private int maxAmplitudePeak;
    private boolean inDraw;

    private int averageWindow = 40;



    private Paint mySpectrogramPaint;


    // Constant to resize the values
    private static final int shortMaxValue = Short.MAX_VALUE;
    private float resizeConstant;

    private float minEnergyLine_;
    private String minEnergyLineName_;
    private boolean minEnergyLineOn_;
    private Paint EnergyLinePaint_;

    private float maxEnergyLine_;
    private String maxEnergyLineName_;
    private boolean maxEnergyLineOn_;

    // Scroll, Zoom variables
    private int start_x = 0;
    private int end_x = 0;

    private int nbrOfSeconds_;

    private boolean peakDetected_;

    private LinkedList<Float> myQueue;
    private float myQueueTotal;

    public Calibration_Spectrometer(Context context) {
        super(context);
        init();
    }

    public Calibration_Spectrometer(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Calibration_Spectrometer(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        mySpectrogram_ = new float[shortMaxValue];
        for (int i = 0; i < mySpectrogram_.length; ++i) {
            mySpectrogram_[i] = 0;
        }

        myQueue = new LinkedList<>();
        myQueueTotal = 0;

        // init process var
        inDraw = false;
        maxAtSingleFreq = 0;
        nbrOfSeconds_ = 0;
        start_x = 0;
        end_x = shortMaxValue - 2;
        minEnergyLine_ = 0;

        // --- Spectrum ---
        mySpectrogramPaint = new Paint();
        mySpectrogramPaint.setColor(Color.parseColor("#3e86a0"));
        mySpectrogramPaint.setStrokeWidth(2.0f);
        mySpectrogramPaint.setTextSize(14);

        // --- Energy Line ---
        EnergyLinePaint_ = new Paint();
        EnergyLinePaint_.setColor(Color.RED);
        EnergyLinePaint_.setStrokeWidth(2.0f);
        EnergyLinePaint_.setTextSize(14);

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
        maxAtSingleFreq = 0.5f;


        // Draw
        for(int i = 0; i < mySpectrogram_.length - 2; ++i) {

            float y_val = movingAverage(mySpectrogram_[i]);

            if (y_val > 0) {

                float x_pos = i * resizeConstant;
                float y_pos = height - height * (y_val / maxAtSingleFreq);
                canvas.drawLine(x_pos, height, x_pos, y_pos, mySpectrogramPaint);
            }
        }

        if(minEnergyLineOn_) {
            float x_pos = minEnergyLine_ * resizeConstant;
            canvas.drawLine(x_pos, height, x_pos, 0, EnergyLinePaint_);
            canvas.drawText(minEnergyLineName_, x_pos + 5,20, EnergyLinePaint_);
        }

        if(maxEnergyLineOn_){
            float x_pos = maxEnergyLine_ * resizeConstant;
            canvas.drawLine(x_pos, height, x_pos, 0, EnergyLinePaint_);
            canvas.drawText(maxEnergyLineName_, x_pos + 5,20,EnergyLinePaint_);
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
            // init here
            return;
        }

        if (myFreq < shortMaxValue - 2) {

            mySpectrogram_[myFreq] += 1;
        }
    }

    public void setMinEnergyLine(Isotope minIsotope){

        if(minIsotope == null){
            minEnergyLineOn_ = false;
        }else{
            minEnergyLineOn_ = true;
            minEnergyLine_ = minIsotope.getX_pos();
            minEnergyLineName_ = minIsotope.getName();
        }

        if (!inDraw) {
            postInvalidate();
        }
    }

    public float getMinEnergyLine() {
        return minEnergyLine_;
    }

    public float getMaxEnergyLine() {
        return maxEnergyLine_;
    }

    public float getPointerLine(float pointer_x_pos){
        return (pointer_x_pos/width)*shortMaxValue;
    }

    public void setMaxEnergyLine(Isotope maxIsotope){

        if(maxIsotope == null){
            maxEnergyLineOn_ = false;
        }else{
            maxEnergyLineOn_ = true;
            maxEnergyLine_ = maxIsotope.getX_pos();
            maxEnergyLineName_ = maxIsotope.getName();
        }

        if (!inDraw) {
            postInvalidate();
        }
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

        for(int i = averageWindow/2; i < mySpectrogram_.length - averageWindow/2 ; ++i){

            for(int j = 0 ; j < averageWindow/2 ; ++j){
                mySpectrogram_[i-averageWindow/4+j] -= myBackgroundData_[i]*(1.0/(1+Math.abs(averageWindow/4 - j)));
            }
        }

        if (!inDraw) {
            postInvalidate();
        }
    }

    /*

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
*/
}
