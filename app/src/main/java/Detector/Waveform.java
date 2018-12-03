package Detector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;

public class Waveform extends View {

    // Because we do a lot of adding and removal we use linkedlist for better performance
    private LinkedList<float[]> mySamplesXY_;

    // My Noise Threshold Line
    private float thresholdLine_;

    // Var to know if we are drawing
    private boolean inDraw;
    private boolean mustRedraw;
    private int MinFrontPoints;
    private int MinFrontPointsIndex;

    // Constant to resize the values
    private static final float shortMaxValue = Short.MAX_VALUE;

    // Canvas
    private int width, height;
    private float centerY;

    private boolean peakDetected_;

    // UI
    private Paint myThresholdPaint, mySignalPaint;

    // Constructors
    public Waveform(Context context) {
        super(context);
        init();
    }

    public Waveform(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Waveform(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    // Init layout attributes
    private void init() {

        // Init List of Samples if first time
        if (mySamplesXY_ == null) {
            mySamplesXY_ = new LinkedList<>();
        }

        // Init Number of detections
        inDraw = false;

        // --- Points ---
        myThresholdPaint = new Paint();
        myThresholdPaint.setColor(Color.RED);
        myThresholdPaint.setStrokeWidth(3.0f);

        // --- Signal ---
        mySignalPaint = new Paint();
        mySignalPaint.setColor(Color.parseColor("#3e86a0"));
        mySignalPaint.setStrokeWidth(2.0f);
    }

    public void setThresholdLineYPos(float thresholdLineYPos){

        this.thresholdLine_ = thresholdLineYPos*height;
        postInvalidate();
    }

    public int getMinFrontPoints() {
        return MinFrontPoints;
    }

    public void setMinFrontPoints(int nbrOfMinFrontPoints){
        this.MinFrontPoints = nbrOfMinFrontPoints;
    }

    public float getThresholdLineYPos(){
        return thresholdLine_ /height;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // if canva change size
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        centerY = height / 2f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        inDraw = true;

        try {
            // We draw the samples points
            for (float[] sampleXY : mySamplesXY_) {
                canvas.drawLines(sampleXY,mySignalPaint);
            }

            // We draw threshold line
            canvas.drawLine(0, thresholdLine_,width, thresholdLine_,myThresholdPaint);

        }catch (Exception ex){
            ex.printStackTrace();
        }

        inDraw = false;
    }

    public void setSamples(short[] samples) {

        // This float array contains the (x,y) of each point
        float[] waveformPoints = new float[width*2];
        MinFrontPointsIndex = 0;
        int pointIndex = 0;
        mustRedraw = false;

        // Size of canvas
        for (int x = 0; x < width; x++) {

            // Resize the y coordinate to the canvas height
            float y = centerY - ((samples[x] / shortMaxValue) * centerY);

            if(!mustRedraw){
                detectPeak(y);
            }

            waveformPoints[pointIndex++] = x;
            waveformPoints[pointIndex++] = y;
        }

        // Calls onDraw() as soon as it can
        if(!inDraw && mustRedraw) {

            mySamplesXY_.clear();
            mySamplesXY_.add(waveformPoints);
            postInvalidate();
        }
    }

    private void detectPeak(float y){

        if(thresholdLine_ > 0){
            // Positive Trigger
            if(y > thresholdLine_) {

                MinFrontPointsIndex++;

                if(MinFrontPointsIndex >= MinFrontPoints && !peakDetected_) {
                    peakDetected_ = true;
                }

                return;
            }else{
                if(peakDetected_){
                    peakDetected_ = false;
                    mustRedraw = true;
                }
            }
        }else{
            // Negative Trigger
            if(y < thresholdLine_) {

                MinFrontPointsIndex++;


                if(MinFrontPointsIndex >= MinFrontPoints && !peakDetected_) {
                    peakDetected_ = true;
                }

                return;
            }else{
                if(peakDetected_){
                    peakDetected_ = false;
                    mustRedraw = true;
                }
            }
        }

        MinFrontPointsIndex = 0;
    }
}