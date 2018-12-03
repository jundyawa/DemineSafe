package REST_API;

import android.content.Context;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Base64;
import android.util.JsonReader;
import android.util.Log;

import com.deminesafe.deminesafe.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import Classes.Field_Value;
import Methods.JSON_Methods;
import Methods.Shared_Preferences_Methods;

public class OSM_PUT extends AsyncTask<Void,Void,Void> {

    // Attributes
    private String accessPoint_;
    private String endPoint_;
    private String username_;
    private String password_;
    private String payload_;
    private String returnPayload_;

    private HttpsURLConnection myHttpsConnection;

    // in ms
    private final int TIMEOUT = 10000;

    // Response Var
    private RESPONSE myRESPONSE_;
    private int myResponseCode;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(int responseCode, String returnPayload);
    }

    // Constructor
    OSM_PUT(Context myContext, String endPoint, String payload_body, RESPONSE myRESPONSE){
        this.myRESPONSE_ = myRESPONSE;
        this.accessPoint_ = myContext.getString(R.string.osm_accessPoint);
        this.endPoint_ = endPoint;
        this.payload_ = payload_body;

        Shared_Preferences_Methods mySharedPrefs = new Shared_Preferences_Methods(myContext);
        this.username_ = mySharedPrefs.getUsername();
        this.password_ = mySharedPrefs.getPassword(username_);
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
        returnPayload_ = null;

        try {

            // Create URL
            URL endPointURL = new URL(accessPoint_ + endPoint_);

            // Create connection
            myHttpsConnection = (HttpsURLConnection) endPointURL.openConnection();

            // Set Request Method
            myHttpsConnection.setRequestMethod("PUT");

            // Set Basic Authentification
            String basicAuth = username_ + ":" + password_;
            basicAuth = "Basic " + new String(Base64.encode(basicAuth.getBytes(),Base64.NO_WRAP));
            myHttpsConnection.setRequestProperty("Authorization",basicAuth);

            // Set Content Type
            myHttpsConnection.setRequestProperty("Content-Type", "text/xml");

            // Set Writable
            myHttpsConnection.setDoOutput(true);

            // Create output stream linked to our https connection
            DataOutputStream streamWriter = new DataOutputStream(myHttpsConnection.getOutputStream());
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(streamWriter, "UTF-8"));

            // Write to buffer
            writer.write(payload_);

            // Send out the buffer
            writer.flush();

            // Close the stream
            writer.close();

            // Get Response Code
            myResponseCode = myHttpsConnection.getResponseCode();

            // If connection successful
            if (myResponseCode == HttpURLConnection.HTTP_OK) {

                // Get Input Stream
                InputStream streamReader = myHttpsConnection.getInputStream();

                // Read stream
                BufferedInputStream bis = new BufferedInputStream(streamReader);
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                int result = bis.read();
                while(result != -1) {
                    buf.write((byte) result);
                    result = bis.read();
                }

                // Set return payload
                returnPayload_ = buf.toString("UTF-8");

                // Close Streams
                bis.close();
                streamReader.close();

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
        myRESPONSE_.RESPONSE(myResponseCode,returnPayload_);
    }
}
