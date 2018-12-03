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

public class Background_Spectra extends View {

    // Canvas
    private int width, height;
    private float centerY;

    private short[] mySpectrogram_;
    private float maxAtSingleFreq;
    private int nbrOfDetection_;
    private boolean peakDetected_;

    // My Noise Threshold Line
    private float thresholdLine_;

    // Var to know if we are drawing
    private int MinFrontPoints;
    private int MinFrontPointsIndex;
    private int maxAmplitudePeak;
    private boolean inDraw;

    private Paint mySpectrogramPaint;
    private Paint myStoppedPaint;


    // Constant to resize the values
    private static final int shortMaxValue = Short.MAX_VALUE;
    private float resizeConstant;

    private LinkedList<Short> myQueue;
    private int myQueueTotal;



    public Background_Spectra(Context context) {
        super(context);
        init();
    }

    public Background_Spectra(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public Background_Spectra(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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

        // --- Spectrum ---
        mySpectrogramPaint = new Paint();
        mySpectrogramPaint.setColor(Color.parseColor("#3e86a0"));
        mySpectrogramPaint.setStrokeWidth(2.0f);
        mySpectrogramPaint.setTextSize(14);

        myStoppedPaint = new Paint();
        myStoppedPaint.setColor(Color.RED);
        myStoppedPaint.setStrokeWidth(2.0f);
        myStoppedPaint.setTextSize(14);
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
        maxAtSingleFreq = 0;

        // Update Max
        for(int i = 2 ; i < mySpectrogram_.length-2; i++){
            float y_val = movingAverage(mySpectrogram_[i]);

            if (y_val > maxAtSingleFreq) {
                maxAtSingleFreq = y_val;
            }
        }

        myQueue.clear();
        myQueueTotal = 0;

        // Draw
        for(int i = 0; i < mySpectrogram_.length-2; ++i) {

            float y_val = movingAverage(mySpectrogram_[i]);

            if (y_val > 0) {

                float x_pos = i * resizeConstant;
                float y_pos = height - height * (y_val / maxAtSingleFreq);
                canvas.drawLine(x_pos, height, x_pos, y_pos, mySpectrogramPaint);
            }
        }

        canvas.drawText(String.valueOf(nbrOfDetection_),10,20,mySpectrogramPaint);

        inDraw = false;
    }


    private float movingAverage(short val){

        if(myQueue == null){
            return 0;
        }

        myQueueTotal += val;
        myQueue.addLast(val);
        if(myQueue.size() > 40){
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

            return;
        }

        if (myFreq < shortMaxValue - 2) {

            mySpectrogram_[myFreq] += 1;

            nbrOfDetection_++;
        }

        if(!inDraw){
            postInvalidate();
        }
    }

    public short[] getSpectraData(){
        return mySpectrogram_;
    }

    public int getNbrOfDetection() {
        return nbrOfDetection_;
    }


    public void setThresholdLineYPos(float y_pos_ratio) {
        this.thresholdLine_ = ((centerY - y_pos_ratio*height)/height)*shortMaxValue;
    }

    public void setMinFrontPoints(int minFrontPoints) {
        this.MinFrontPoints = minFrontPoints;
    }

    public void stopped(){

        mySpectrogramPaint = myStoppedPaint;
        postInvalidate();
    }
}
