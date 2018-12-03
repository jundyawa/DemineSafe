package AlertDialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.deminesafe.deminesafe.R;

import Methods.Input_Methods;

public class Save_Map_AlertDialog {

    // Attributes
    private Context myContext_;
    private Activity myActivity_;

    private InputMethodManager imm;

    private AlertDialog myAlertDialog_;

    private Input_Methods myInputMethods_;

    private RESPONSE myResponse_;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(String map_name);
    }

    // Constructor
    public Save_Map_AlertDialog(Context myContext, RESPONSE myResponse){
        this.myContext_ = myContext;
        this.myActivity_ = (Activity) myContext;
        this.myResponse_ = myResponse;

        this.myInputMethods_ = new Input_Methods(myContext_);
        this.imm = ((InputMethodManager) myContext_.getSystemService(Context.INPUT_METHOD_SERVICE));

        buildAlertDialog();
    }

    private void buildAlertDialog(){

        // We build the dialog box
        final AlertDialog.Builder builder = new AlertDialog.Builder(myContext_);

        // We fetch our custom layout
        LayoutInflater myInflater = myActivity_.getLayoutInflater();
        final View myDialogView = myInflater.inflate(R.layout.dialogbox_savemap,null);

        // We set our custom layout in the dialog box
        builder.setView(myDialogView);

        // Fetch UI objects
        TextView confirmButton_ = myDialogView.findViewById(R.id.ok_textview);
        TextView cancelButton_ = myDialogView.findViewById(R.id.cancel_textview);
        final TextInputLayout input1_layout = myDialogView.findViewById(R.id.input1_layout);
        final TextInputEditText input1_edittext = myDialogView.findViewById(R.id.input1_edittext);

        input1_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);


        // Set the click listeners
        cancelButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Hide Keyboard
                hideKeyboard(input1_edittext);

                myAlertDialog_.cancel();
            }
        });

        confirmButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(myInputMethods_.isValid(input1_layout,input1_edittext)){

                    String myInput1 = input1_edittext.getText().toString().trim();

                    // Hide Keyboard
                    hideKeyboard(input1_edittext);

                    myAlertDialog_.cancel();

                    myResponse_.RESPONSE(myInput1);

                }
            }
        });

        // We create the Box
        myAlertDialog_ = builder.create();

        // Show Keyboard
        input1_edittext.requestFocus();
        showKeyboard();

        // We show the box
        myAlertDialog_.show();
    }

    private void showKeyboard(){
        if(imm != null){
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void hideKeyboard(EditText myEditText) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
        }
    }
}
