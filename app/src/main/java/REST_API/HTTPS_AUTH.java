package REST_API;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.JsonReader;
import android.util.Log;

import com.deminesafe.deminesafe.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

import Classes.Field_Value;
import Methods.JSON_Methods;

public class HTTPS_AUTH extends AsyncTask<Void,Void,Void> {

    // Attributes
    private String webApiKey_;
    private String username_;
    private String password_;
    private String methodType_;

    private HttpsURLConnection myHttpsConnection;

    // in ms
    private final int TIMEOUT = 10000;

    // Response Var
    private RESPONSE myRESPONSE_;
    private int myResponseCode;
    private String myIdToken;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(int responseCode, String idToken);
    }

    // Constructor
    public HTTPS_AUTH(Context myContext, String methodType, String username, String password, RESPONSE myRESPONSE){
        this.myRESPONSE_ = myRESPONSE;
        this.methodType_ = methodType;
        this.webApiKey_ = myContext.getString(R.string.webApiKey);
        this.username_ = username.toLowerCase();
        this.password_ = password.toLowerCase();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // We start a timer of 10 seconds
        new CountDownTimer(TIMEOUT,TIMEOUT){
            @Override
            public void onTick(long millisUntilFinished) {}

            @Override
            public void onFinish() {
                // When the timer is done we check if the task is still running
                if (getStatus() == AsyncTask.Status.RUNNING || getStatus() == AsyncTask.Status.PENDING) {

                    // if it is we cancel the task
                    cancel();

                    // and we call our custom ending method
                    endTask();
                }
            }
        }.start();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        myResponseCode = 0;
        myIdToken = null;

        try {
            // Build URL
            String authenticationURL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty";

            if(methodType_.equals("SIGN_UP")){
                authenticationURL = authenticationURL + "/signupNewUser";
            }else if(methodType_.equals("SIGN_IN")){
                authenticationURL = authenticationURL + "/verifyPassword";
            }else{
                return null;
            }
            authenticationURL = authenticationURL + "?key=" + webApiKey_;

            // Create URL
            URL endPointURL = new URL(authenticationURL);

            // Create connection
            myHttpsConnection = (HttpsURLConnection) endPointURL.openConnection();

            // Set Request Method
            myHttpsConnection.setRequestMethod("POST");

            // Set Writable
            myHttpsConnection.setDoOutput(true);

            // Set Https Connection properties
            myHttpsConnection.setRequestProperty("Content-Type", "application/json");

            // Generate JSON from the data
            String myJSON_str = "{\"email\":\"" + username_ + "\",\"password\":\"" +
                    password_ + "\",\"returnSecureToken\":true}";

            JSONObject myJSON = new JSONObject(myJSON_str);

            // Create output stream linked to our https connection
            OutputStreamWriter streamWriter = new OutputStreamWriter(myHttpsConnection.getOutputStream());

            // Write to buffer
            streamWriter.write(myJSON.toString());

            // Send out the buffer
            streamWriter.flush();

            // Close the stream
            streamWriter.close();

            // Get Response Code
            myResponseCode = myHttpsConnection.getResponseCode();

            // If connection successful
            if (myResponseCode == HttpURLConnection.HTTP_OK) {

                // Get Input Stream
                InputStream streamReader = myHttpsConnection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(streamReader, "UTF-8");

                // Buffer the inputstream
                BufferedReader br = new BufferedReader(responseBodyReader);

                // Create JsonReader from input stream
                JsonReader jsonReader = new JsonReader(br);

                // Convert the JSON to a document
                ArrayList<Field_Value> myFields = JSON_Methods.convertFirestoreJSON(jsonReader);

                // Close Streams
                jsonReader.close();
                br.close();
                responseBodyReader.close();
                streamReader.close();

                // Get The IdToken Field
                myIdToken = Field_Value.getFieldValue(myFields,"idToken");

            }else{  // If unsuccessful

                Log.d("HTTP","CODE : " + myResponseCode);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {

            // Disconnect
            myHttpsConnection.disconnect();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

        endTask();
    }

    private void endTask(){

        // Return null callback
        myRESPONSE_.RESPONSE(myResponseCode,myIdToken);
    }
}
