package com.deminesafe.deminesafe;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import Classes.Hazard;
import Classes.LatLng;
import Methods.Input_Methods;
import REST_API.FirestoreRequest;
import REST_API.OSMRequest;

import static android.app.Activity.RESULT_OK;


public class Add_Hazard extends Fragment {

    // Attributes
    private Map_Operation_Activity myParentActivity_;
    private Context myContext_;
    private Input_Methods myInputMethods_;
    private InputMethodManager imm;

    // --- Camera ---
    static final int REQUEST_IMAGE_CAPTURE = 10;
    private Bitmap aBitmap = null;
    private ImageView picture_IMAGEVIEW;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_hazard, container, false);

        // --- Get Methods Access from Parent Activity ---
        myParentActivity_ = (Map_Operation_Activity) getActivity();
        myContext_ = getActivity();

        myInputMethods_ = new Input_Methods(myContext_);
        imm = ((InputMethodManager) myContext_.getSystemService(Context.INPUT_METHOD_SERVICE));

        // Fetch UI objects
        Button picture_BUTTON = rootView.findViewById(R.id.picture_but);
        final EditText add_info_EDITTEXT = rootView.findViewById(R.id.add_info_edittext);
        Button confirm_BUTTON = rootView.findViewById(R.id.confirm_but);
        picture_IMAGEVIEW =rootView.findViewById(R.id.image_view);
        add_info_EDITTEXT.setInputType(InputType.TYPE_CLASS_TEXT);

        // Set onclick listeners
        picture_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        confirm_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!myParentActivity_.getGPSState()){
                    myParentActivity_.toastMessage("Waiting to receive a GPS signal");
                    return;
                }

                if(myInputMethods_.isAddInfoValid(add_info_EDITTEXT)) {

                    // Freeze UI
                    myParentActivity_.freezeUI();

                    String addInfo = add_info_EDITTEXT.getText().toString().trim();

                    // Hide Keyboard
                    hideKeyboard(add_info_EDITTEXT);

                    // Add Hazard
                    LatLng myCoordinates = myParentActivity_.getMyCoordinates();
                    if(myCoordinates == null){
                        myCoordinates = myParentActivity_.getMyMap().getRectBounds().getMinLatLng();
                    }
                    Hazard newHazard = new Hazard(myCoordinates,addInfo,aBitmap);

                    // Add to object
                    myParentActivity_.getMyMap().addHazard(newHazard);

                    // Save locally
                    myParentActivity_.getMyDBAdapter().createHazard(myParentActivity_.getMyMap().getCloudID(),newHazard);

                    // Save to OSM
                    sendHazardToOSM(newHazard);

                    // Save on cloud
                    FirestoreRequest myRequest = new FirestoreRequest(myContext_, new FirestoreRequest.RESPONSE() {
                        @Override
                        public void RESPONSE(int nbr_of_objects) {

                            myParentActivity_.unfreezeUI();

                            exit();
                        }
                    });
                    myRequest.sendHazard(myParentActivity_.getMyMap().getCloudID(),newHazard);

                    aBitmap = null;
                }

            }
        });

        return rootView;
    }

    private void sendHazardToOSM(Hazard myHazard){
        OSMRequest myRequest = new OSMRequest(myContext_);
        myRequest.sendHazardtoOSM(myHazard);
    }

    private void exit(){
        myParentActivity_.clickGPS();
    }

    private void hideKeyboard(EditText myEditText) {
        if (imm != null) {
            imm.hideSoftInputFromWindow(myEditText.getWindowToken(), 0);
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(myContext_.getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            picture_IMAGEVIEW.setImageBitmap(imageBitmap);

            // Prep marker picture to Map Database
            aBitmap = imageBitmap;
        }
    }
}
