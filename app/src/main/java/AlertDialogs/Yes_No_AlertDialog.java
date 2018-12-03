package AlertDialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.deminesafe.deminesafe.R;

public class Yes_No_AlertDialog {

    // Attributes
    private Context myContext_;
    private Activity myActivity_;

    private String title_;
    private String subtitle_;

    private AlertDialog myAlertDialog_;

    private RESPONSE myResponse_;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(boolean pressedOk);
    }

    // Constructor
    public Yes_No_AlertDialog(Context myContext, String title, String subtitle, RESPONSE myResponse){
        this.myContext_ = myContext;
        this.myActivity_ = (Activity) myContext;
        this.title_ = title;
        this.subtitle_ = subtitle;
        this.myResponse_ = myResponse;

        buildAlertDialog();
    }

    private void buildAlertDialog(){

        // We build the dialog box
        final AlertDialog.Builder builder = new AlertDialog.Builder(myContext_);

        // We fetch our custom layout
        LayoutInflater myInflater = myActivity_.getLayoutInflater();
        final View myDialogView = myInflater.inflate(R.layout.dialogbox_yesno,null);

        // We set our custom layout in the dialog box
        builder.setView(myDialogView);

        // Fetch UI objects
        TextView titleTextView_ = myDialogView.findViewById(R.id.title_textview);
        TextView subTitleTextView_ = myDialogView.findViewById(R.id.subtitle_textview);
        TextView okButton_ = myDialogView.findViewById(R.id.ok_textview);
        TextView cancelButton_ = myDialogView.findViewById(R.id.cancel_textview);

        // Set Text
        titleTextView_.setText(title_);
        subTitleTextView_.setText(subtitle_);

        // Set the click listeners
        okButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog_.cancel();

                myResponse_.RESPONSE(true);
            }
        });

        cancelButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myAlertDialog_.cancel();

                myResponse_.RESPONSE(false);
            }
        });

        // We create the Box
        myAlertDialog_ = builder.create();

        // We show the box
        myAlertDialog_.show();
    }
}
