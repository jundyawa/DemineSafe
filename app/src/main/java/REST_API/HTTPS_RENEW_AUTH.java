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
import Methods.Shared_Preferences_Methods;

public class HTTPS_RENEW_AUTH extends AsyncTask<Void,Void,Void> {

    // Attributes
    private Shared_Preferences_Methods mySharedPrefs_;

    private String webApiKey_;
    private String username_;
    private String password_;

    private HttpsURLConnection myHttpsConnection;

    // in ms
    private final int TIMEOUT = 10000;

    // Constructor
    public HTTPS_RENEW_AUTH(Context myContext){
        this.webApiKey_ = myContext.getString(R.string.webApiKey);

        this.mySharedPrefs_ = new Shared_Preferences_Methods(myContext);
        this.username_ = mySharedPrefs_.getUsername();
        this.password_ = mySharedPrefs_.getPassword(username_);
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
                }
            }
        }.start();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        try {
            // Build URL
            String authenticationURL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=" + webApiKey_;

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
            int myResponseCode = myHttpsConnection.getResponseCode();

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
                String myIdToken = Field_Value.getFieldValue(myFields,"idToken");
                mySharedPrefs_.saveIdToken(myIdToken);

                // Close Streams
                jsonReader.close();
                br.close();
                responseBodyReader.close();
                streamReader.close();

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
}
