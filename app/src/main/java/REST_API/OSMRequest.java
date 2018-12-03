package REST_API;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.HttpURLConnection;

import Classes.Hazard;
import Methods.Shared_Preferences_Methods;

public class OSMRequest {

    // Attributes
    private Context myContext_;

    private Shared_Preferences_Methods mySharedPrefs_;
    private String username_;
    private String changesetId_;


    public OSMRequest(Context myContext){
        this.myContext_ = myContext;
        this.mySharedPrefs_ = new Shared_Preferences_Methods(myContext_);
        this.username_ = mySharedPrefs_.getUsername();
        this.changesetId_ = mySharedPrefs_.getChangeSetId();
    }

    public void renewChangeset() {

        if (!isNetworkAvailable()) {
            return;
        }

        String payload = "<osm><changeset><tag k=\"created_by\" v=\"" + username_ +
                "\"/><tag k=\"comment\" v=\"Adding a hazard\"/></changeset></osm>";

        OSM_PUT myRequest = new OSM_PUT(myContext_, "/changeset/create", payload, new OSM_PUT.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, String payload) {

                if(responseCode == HttpURLConnection.HTTP_OK){
                    mySharedPrefs_.saveChangeSetId(payload);
                }else{
                    Log.d("OSM", "Changeset failed");
                }
            }
        });
        myRequest.execute();

    }

    public void sendHazardtoOSM(Hazard myHazard){

        if (!isNetworkAvailable()) {
            return;
        }

        if(changesetId_ == null){
            renewChangeset();
            return;
        }

        String payload = "<osm><node changeset=\"" + changesetId_ + "\" lat=\"" + myHazard.getLocation().getMyLatitude() +
                "\" lon=\"" + myHazard.getLocation().getMyLongitude() + "\"><tag k=\"note\" v=\"" + myHazard.getNotes() +
                "\"/></node></osm>";

        OSM_PUT myRequest = new OSM_PUT(myContext_, "/node/create", payload, new OSM_PUT.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, String payload) {

                if(responseCode == HttpURLConnection.HTTP_OK){

                }

            }
        });
        myRequest.execute();
    }

    private boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager = (ConnectivityManager) myContext_.getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        return false;
    }
}
