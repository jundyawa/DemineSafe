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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.deminesafe.deminesafe.R;

import Methods.Input_Methods;

public class Sign_Up_AlertDialog {

    // Attributes
    private Context myContext_;
    private Activity myActivity_;

    private InputMethodManager imm;

    private AlertDialog myAlertDialog_;

    private Input_Methods myInputMethods_;

    private RESPONSE myResponse_;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(String input1, String input2);
    }

    // Constructor
    public Sign_Up_AlertDialog(Context myContext, RESPONSE myResponse){
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
        final View myDialogView = myInflater.inflate(R.layout.dialogbox_signup,null);

        // We set our custom layout in the dialog box
        builder.setView(myDialogView);

        // Fetch UI objects
        TextView confirmButton_ = myDialogView.findViewById(R.id.ok_textview);
        TextView cancelButton_ = myDialogView.findViewById(R.id.cancel_textview);
        TextInputLayout input1_layout = myDialogView.findViewById(R.id.input1_layout);
        TextInputLayout input2_layout = myDialogView.findViewById(R.id.input2_layout);
        final TextInputEditText input1_edittext = myDialogView.findViewById(R.id.input1_edittext);
        final TextInputEditText input2_edittext = myDialogView.findViewById(R.id.input2_edittext);

        input1_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input2_edittext.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        final TextInputEditText[] myEditTexts = new TextInputEditText[2];
        myEditTexts[0] = input1_edittext;
        myEditTexts[1] = input2_edittext;

        final TextInputLayout[] myInputLayouts = new TextInputLayout[2];
        myInputLayouts[0] = input1_layout;
        myInputLayouts[1] = input2_layout;


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

                if(myInputMethods_.isValid(myInputLayouts,myEditTexts)){

                    String myInput1 = input1_edittext.getText().toString().trim();
                    String myInput2 = input2_edittext.getText().toString().trim();

                    // Hide Keyboard
                    hideKeyboard(input1_edittext);

                    myAlertDialog_.cancel();

                    myResponse_.RESPONSE(myInput1,myInput2);

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
