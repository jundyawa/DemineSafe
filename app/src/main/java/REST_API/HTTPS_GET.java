package REST_API;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.JsonReader;
import android.util.Log;

import com.deminesafe.deminesafe.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

import Classes.Field_Value;
import Methods.JSON_Methods;
import Methods.Shared_Preferences_Methods;

public class HTTPS_GET extends AsyncTask<Void,Void,Void> {

    // Attributes
    private String accessPoint_;
    private String webApiKey_;
    private String endPoint_;
    private String idToken_;
    private String username_;

    private HttpsURLConnection myHttpsConnection;

    // in ms
    private final int TIMEOUT = 20000;

    // Response Var
    private RESPONSE myRESPONSE_;
    private ArrayList<Field_Value> myResponsePayload;
    private int myResponseCode;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload);
    }

    // Constructor
    HTTPS_GET(Context myContext, String endPoint, RESPONSE myRESPONSE){
        this.myRESPONSE_ = myRESPONSE;
        this.accessPoint_ = myContext.getString(R.string.accessPoint);
        this.webApiKey_ = myContext.getString(R.string.webApiKey);
        this.endPoint_ = endPoint;

        Shared_Preferences_Methods mySharedPrefs = new Shared_Preferences_Methods(myContext);
        this.idToken_ = mySharedPrefs.getIdToken();
        this.username_ = mySharedPrefs.getUsername().split("@")[0];
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
        myResponsePayload = null;

        try {

            // Build URL String
            String URL_str = accessPoint_ + "/" + username_ + endPoint_ + "?key=" + webApiKey_;

            Log.d("HTTP",URL_str);

            // Create URL
            URL endPointURL = new URL(URL_str);

            // Create connection
            myHttpsConnection = (HttpsURLConnection) endPointURL.openConnection();

            // Set Request Method
            myHttpsConnection.setRequestMethod("GET");

            // Set Authorization header
            myHttpsConnection.setRequestProperty("Authorization", "Bearer " + idToken_);

            // Get Response Code
            myResponseCode = myHttpsConnection.getResponseCode();

            if (myResponseCode == HttpURLConnection.HTTP_OK) {

                // Get Input Stream
                InputStream streamReader = myHttpsConnection.getInputStream();
                InputStreamReader responseBodyReader = new InputStreamReader(streamReader, "UTF-8");

                // Buffer the inputstream
                BufferedReader br = new BufferedReader(responseBodyReader);

                // Create JsonReader from input stream
                JsonReader jsonReader = new JsonReader(br);

                // Convert the JSON to a document
                myResponsePayload = JSON_Methods.convertFirestoreJSON(jsonReader);

                // Close Streams
                jsonReader.close();
                br.close();
                responseBodyReader.close();
                streamReader.close();

            }else if(myResponseCode == HttpURLConnection.HTTP_UNAUTHORIZED){

                Log.d("HTTP","AUTH RETRY");

            }else{  // If unsuccessful

                Log.d("HTTP","CODE : " + myResponseCode);
            }

        } catch (IOException e) {
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
        myRESPONSE_.RESPONSE(myResponseCode,myResponsePayload);
    }
}
