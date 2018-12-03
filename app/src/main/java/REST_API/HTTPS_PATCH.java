package REST_API;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;

import com.deminesafe.deminesafe.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import javax.net.ssl.HttpsURLConnection;

import Classes.Field_Value;
import Methods.JSON_Methods;
import Methods.Shared_Preferences_Methods;

public class HTTPS_PATCH extends AsyncTask<Void,Void,Void> {

    // Attributes
    private String accessPoint_;
    private String webApiKey_;
    private String endPoint_;
    private String idToken_;
    private String username_;
    private ArrayList<Field_Value> fieldMasks_;

    private HttpsURLConnection myHttpsConnection;


    // in ms
    private final int TIMEOUT = 10000;

    // Response Var
    private RESPONSE myRESPONSE_;
    private int myResponseCode;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(int responseCode);
    }

    // Constructor
    HTTPS_PATCH(Context myContext, String endPoint, ArrayList<Field_Value> fieldMasks, RESPONSE myRESPONSE){
        this.myRESPONSE_ = myRESPONSE;
        this.accessPoint_ = myContext.getString(R.string.accessPoint);
        this.webApiKey_ = myContext.getString(R.string.webApiKey);
        this.endPoint_ = endPoint;
        this.fieldMasks_ = fieldMasks;

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

        try {

            // Build URL String
            StringBuilder URL_str = new StringBuilder(accessPoint_ + "/" + username_ + endPoint_ + "?");

            if(fieldMasks_ != null) {
                for (int i = 0; i < fieldMasks_.size(); ++i) {
                    URL_str.append("updateMask.fieldPaths=").append(fieldMasks_.get(i).getName()).append("&");
                }
            }

            URL_str.append("key=" + webApiKey_);

            // Create URL
            URL endPointURL = new URL(URL_str.toString());

            // Create connection
            myHttpsConnection = (HttpsURLConnection) endPointURL.openConnection();

            // Set Request Method
            myHttpsConnection.setRequestMethod("PATCH");

            // Set Writable
            myHttpsConnection.setDoOutput(true);

            // Set Authorization header
            myHttpsConnection.setRequestProperty("Authorization", "Bearer " + idToken_);

            // Set Https Connection properties
            myHttpsConnection.setRequestProperty("Content-Type", "application/json");

            // Generate JSON from the data
            JSONObject myJSON = JSON_Methods.buildFirestoreJSON(fieldMasks_);

            // Create output stream linked to our https connection
            OutputStreamWriter streamWriter = new OutputStreamWriter(myHttpsConnection.getOutputStream());

            // Write to buffer
            if (myJSON != null) {
                streamWriter.write(myJSON.toString());
            }

            // Send out the buffer
            streamWriter.flush();

            // Close the stream
            streamWriter.close();

            // Get Response Code
            myResponseCode = myHttpsConnection.getResponseCode();

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
        myRESPONSE_.RESPONSE(myResponseCode);
    }
}