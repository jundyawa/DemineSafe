package AlertDialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.deminesafe.deminesafe.R;

import Classes.Hazard;
import Methods.Input_Methods;

public class Hazard_AlertDialog {

    // Attributes
    private Context myContext_;
    private Activity myActivity_;

    private String myAddInfo_;
    private Bitmap myBitmap_;

    private InputMethodManager imm;

    private AlertDialog myAlertDialog_;

    private Input_Methods myInputMethods_;

    private Hazard myHazard_;

    private boolean textChanged_;

    public Hazard_AlertDialog(Context myContext, Hazard myHazard){
        this.myContext_ = myContext;
        this.myActivity_ = (Activity) myContext;

        this.myHazard_ = myHazard;
        this.myAddInfo_ = myHazard.getNotes();
        this.myBitmap_ = myHazard.getPicture();

        this.myInputMethods_ = new Input_Methods(myContext_);
        this.imm = ((InputMethodManager) myContext_.getSystemService(Context.INPUT_METHOD_SERVICE));

        buildAlertDialog();
    }

    private void buildAlertDialog(){

        // We build the dialog box
        final AlertDialog.Builder builder = new AlertDialog.Builder(myContext_);

        // We fetch our custom layout
        LayoutInflater myInflater = myActivity_.getLayoutInflater();
        final View myDialogView = myInflater.inflate(R.layout.dialogbox_hazard,null);

        // We set our custom layout in the dialog box
        builder.setView(myDialogView);

        // Fetch UI objects
        TextView confirmButton_ = myDialogView.findViewById(R.id.ok_textview);
        final EditText add_info_EDITTEXT = myDialogView.findViewById(R.id.add_info_edittext);
        final ImageView IMAGE_VIEW = myDialogView.findViewById(R.id.imageView);

        add_info_EDITTEXT.setInputType(InputType.TYPE_CLASS_TEXT);
        add_info_EDITTEXT.setText(myAddInfo_);
        if(myBitmap_ != null) {
            IMAGE_VIEW.setImageBitmap(myBitmap_);
        }

        textChanged_ = false;
        add_info_EDITTEXT.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                textChanged_ = true;
            }
        });

        // Set the click listeners
        confirmButton_.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(myInputMethods_.isAddInfoValid(add_info_EDITTEXT)){

                    if(textChanged_) {

                        String myAddInfo = add_info_EDITTEXT.getText().toString().trim();

                        myHazard_.setNotes(myAddInfo);

                        myHazard_.setCloudSynced(0);
                    }

                    // Hide Keyboard
                    hideKeyboard(add_info_EDITTEXT);

                    myAlertDialog_.cancel();
                }
            }
        });

        // We create the Box
        myAlertDialog_ = builder.create();

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
