package com.deminesafe.deminesafe;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.HttpURLConnection;

import AlertDialogs.Sign_Up_AlertDialog;
import Methods.Input_Methods;
import Methods.Shared_Preferences_Methods;
import REST_API.HTTPS_AUTH;
import REST_API.OSMRequest;

public class Login_Activity extends AppCompatActivity {

    // UI
    private Button signin_BUTTON;
    private TextView createAccount_TEXTVIEW;
    private TextInputEditText username_EDITTEXT;
    private TextInputEditText password_EDITTEXT;
    private ProgressBar wait_PROGRESSBAR;


    // Shared Preferences
    private Shared_Preferences_Methods mySharedPrefs_;

    // Input Methods
    private Input_Methods myInputMethods_;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // UI init
        signin_BUTTON = findViewById(R.id.signin_button);
        username_EDITTEXT = findViewById(R.id.username_edittext);
        password_EDITTEXT = findViewById(R.id.password_edittext);
        wait_PROGRESSBAR = findViewById(R.id.wait_progressBar);
        createAccount_TEXTVIEW = findViewById(R.id.createAccount_textview);
        TextInputLayout username_INPUTLAYOUT = findViewById(R.id.username_inputlayout);
        TextInputLayout password_INPUTLAYOUT = findViewById(R.id.password_inputlayout);
        username_EDITTEXT.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        password_EDITTEXT.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        final TextInputEditText[] mySignInEditTexts = new TextInputEditText[2];
        mySignInEditTexts[0] = username_EDITTEXT;
        mySignInEditTexts[1] = password_EDITTEXT;

        final TextInputLayout[] mySignInInputLayouts = new TextInputLayout[2];
        mySignInInputLayouts[0] = username_INPUTLAYOUT;
        mySignInInputLayouts[1] = password_INPUTLAYOUT;

        // Init the other stuff
        myInputMethods_ = new Input_Methods(this);

        // Set click listeners
        signin_BUTTON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(myInputMethods_.isValid(mySignInInputLayouts,mySignInEditTexts)){

                    // Check if network available
                    if(!isNetworkAvailable()){
                        toastMessage("Connect to the internet");

                    }else{
                        freezeUI();

                        String username = username_EDITTEXT.getText().toString().trim();
                        String password = password_EDITTEXT.getText().toString().trim();

                        signIn(username,password);
                    }
                }
            }
        });

        createAccount_TEXTVIEW.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Check if network available
                if(!isNetworkAvailable()){
                    toastMessage("Connect to the internet");

                }else {
                    createAccount();
                }
            }
        });

        // Fetch Intent
        Intent i = getIntent();
        boolean fromMain = i.getBooleanExtra("fromMain",false);


        // Fetch saved username
        mySharedPrefs_ = new Shared_Preferences_Methods(this);
        String saved_username = mySharedPrefs_.getUsername();
        if(saved_username != null){

            if(fromMain){

                username_EDITTEXT.setText(saved_username);
                password_EDITTEXT.requestFocus();

            }else {

                freezeUI();

                HTTPS_AUTH auth_attempt = new HTTPS_AUTH(this, "SIGN_IN", saved_username, mySharedPrefs_.getPassword(saved_username), new HTTPS_AUTH.RESPONSE() {
                    @Override
                    public void RESPONSE(int responseCode, String idToken) {
                        if (responseCode == HttpURLConnection.HTTP_OK && idToken != null) {

                            // Save idToken
                            mySharedPrefs_.saveIdToken(idToken);

                        } else {
                            toastMessage("Offline Login");
                        }

                        startMainMenu();
                    }
                });
                auth_attempt.execute();
            }

        }else{
            username_EDITTEXT.requestFocus();
        }

    }

    private void createAccount(){

        new Sign_Up_AlertDialog(this, new Sign_Up_AlertDialog.RESPONSE() {
            @Override
            public void RESPONSE(String input1, String input2) {
                signUp(input1,input2);
            }
        });
    }

    private void signUp(final String username, final String password){

        final HTTPS_AUTH auth_attempt = new HTTPS_AUTH(getBaseContext(), "SIGN_UP", username, password, new HTTPS_AUTH.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, String idToken) {

                if(responseCode == HttpURLConnection.HTTP_OK && idToken != null){

                    // Set Input Text
                    username_EDITTEXT.setText(username);
                    password_EDITTEXT.setText(password);

                    // Save idToken
                    mySharedPrefs_.saveIdToken(idToken);

                    // Save username
                    mySharedPrefs_.saveUsername(username);

                    // If account doesnt exist locally
                    if (!mySharedPrefs_.accountCheck(username,password)) {
                        // Save new account
                        mySharedPrefs_.saveAccount(username,password);
                    }

                    startMainMenu();

                }else{
                    toastMessage("Bad request, error : " + responseCode);
                }

                unfreezeUI();
            }
        });
        auth_attempt.execute();
    }

    private void startMainMenu(){

        OSMRequest myRequest = new OSMRequest(this);
        myRequest.renewChangeset();

        finish();
        Intent i = new Intent(Login_Activity.this,Main_Activity.class);
        startActivity(i);
    }

    private void signIn(final String username, final String password){

        final HTTPS_AUTH auth_attempt = new HTTPS_AUTH(getBaseContext(), "SIGN_IN", username, password, new HTTPS_AUTH.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, String idToken) {

                if(responseCode == HttpURLConnection.HTTP_OK && idToken != null){

                    // Save idToken
                    mySharedPrefs_.saveIdToken(idToken);

                    // Save username
                    mySharedPrefs_.saveUsername(username);

                    // If account doesnt exist locally
                    if (!mySharedPrefs_.accountCheck(username,password)) {
                        // Save new account
                        mySharedPrefs_.saveAccount(username,password);
                    }

                    startMainMenu();


                }else if(responseCode == HttpURLConnection.HTTP_NOT_FOUND){
                    toastMessage("Account doesn't exist");

                }else if(responseCode == HttpURLConnection.HTTP_BAD_REQUEST){
                    toastMessage("Wrong username or password");

                }else{
                    toastMessage("Bad request, error : " + responseCode);
                }

                // Free ui
                unfreezeUI();
            }
        });
        auth_attempt.execute();
    }

    private void freezeUI(){

        signin_BUTTON.setEnabled(false);
        wait_PROGRESSBAR.setVisibility(View.VISIBLE);
        username_EDITTEXT.setEnabled(false);
        password_EDITTEXT.setEnabled(false);
        createAccount_TEXTVIEW.setEnabled(false);
    }

    private void unfreezeUI(){

        signin_BUTTON.setEnabled(true);
        wait_PROGRESSBAR.setVisibility(View.INVISIBLE);
        username_EDITTEXT.setEnabled(true);
        password_EDITTEXT.setEnabled(true);
        createAccount_TEXTVIEW.setEnabled(true);
    }

    // Show message to user through a short pop up
    private void toastMessage(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
