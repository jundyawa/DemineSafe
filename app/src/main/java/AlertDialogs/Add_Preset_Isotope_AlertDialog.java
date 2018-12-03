package AlertDialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.deminesafe.deminesafe.R;

import java.util.ArrayList;


public class Add_Preset_Isotope_AlertDialog {
    // Attributes
    private Context myContext_;
    private Activity myActivity_;

    private AlertDialog myAlertDialog_;

    private RESPONSE myResponse_;

    private double minEnergy_;
    private double maxEnergy_;


    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(String input1, String input2);
    }

    // Constructor
    public Add_Preset_Isotope_AlertDialog(Context myContext, double minEnergy, double maxEnergy, RESPONSE myResponse){
        this.myContext_ = myContext;
        this.myActivity_ = (Activity) myContext;
        this.myResponse_ = myResponse;

        this.minEnergy_ = minEnergy;
        this.maxEnergy_ = maxEnergy;

        buildAlertDialog();
    }

    private void buildAlertDialog(){

        // We build the dialog box
        final AlertDialog.Builder builder = new AlertDialog.Builder(myContext_);

        // We fetch our custom layout
        LayoutInflater myInflater = myActivity_.getLayoutInflater();
        final View myDialogView = myInflater.inflate(R.layout.dialogbox_add_preset_isotope,null);

        // We set our custom layout in the dialog box
        builder.setView(myDialogView);

        TextView sodiumTextView = myDialogView.findViewById(R.id.sodium_textview);
        TextView potassiumTextView = myDialogView.findViewById(R.id.potassium_textview);
        TextView cobalt1TextView = myDialogView.findViewById(R.id.cobalt_textview);
        TextView cobalt2TextView = myDialogView.findViewById(R.id.cobalt_textview2);
        TextView bariumTextView = myDialogView.findViewById(R.id.barium_textview);
        TextView caesiumTextView = myDialogView.findViewById(R.id.caesium_textview);
        TextView lutetium1TextView = myDialogView.findViewById(R.id.lutetium_textview);
        TextView lutetium2TextView = myDialogView.findViewById(R.id.lutetium_textview2);
        TextView americiumTextView = myDialogView.findViewById(R.id.americium_textview);

        for (int i = 0; i < 2; ++i) {

            double myEnergy = 0;

            if(i == 0){
                myEnergy = minEnergy_;
            }else if(i == 1){
                myEnergy = maxEnergy_;
            }

            if(myEnergy > 500 && myEnergy < 520){
                sodiumTextView.setEnabled(false);
                sodiumTextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 1450 && myEnergy < 1470){
                potassiumTextView.setEnabled(false);
                potassiumTextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 1160 && myEnergy < 1180){
                cobalt1TextView.setEnabled(false);
                cobalt1TextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 1320 && myEnergy < 1340){
                cobalt2TextView.setEnabled(false);
                cobalt2TextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 340 && myEnergy < 365){
                bariumTextView.setEnabled(false);
                bariumTextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 650 && myEnergy < 670){
                caesiumTextView.setEnabled(false);
                caesiumTextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 190 && myEnergy < 210){
                lutetium1TextView.setEnabled(false);
                lutetium1TextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 290 && myEnergy < 315){
                lutetium2TextView.setEnabled(false);
                lutetium2TextView.setTextColor(Color.parseColor("#a8a8a8"));
            }else if(myEnergy > 50 && myEnergy < 70){
                americiumTextView.setEnabled(false);
                americiumTextView.setTextColor(Color.parseColor("#a8a8a8"));
            }
        }

        sodiumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Sodium 20","511");
                myAlertDialog_.cancel();
            }
        });

        potassiumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Potassium 40","1460.8");
                myAlertDialog_.cancel();
            }
        });

        cobalt1TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Cobalt 60 Peak 1","1173.2");
                myAlertDialog_.cancel();
            }
        });

        cobalt2TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Cobalt 60 Peak 2","1332.5");
                myAlertDialog_.cancel();
            }
        });

        bariumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Barium 133","356");
                myAlertDialog_.cancel();
            }
        });

        caesiumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Caesium 137","662");
                myAlertDialog_.cancel();
            }
        });

        lutetium1TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Lutetium 176 Peak 1","201.83");
                myAlertDialog_.cancel();
            }
        });

        lutetium2TextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Lutetium 176 Peak 2","306.78");
                myAlertDialog_.cancel();
            }
        });

        americiumTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myResponse_.RESPONSE("Americium 241","59.6");
                myAlertDialog_.cancel();
            }
        });

        // We create the Box
        myAlertDialog_ = builder.create();

        // We show the box
        myAlertDialog_.show();
    }
}
