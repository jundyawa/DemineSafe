package REST_API;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import Classes.Field_Value;
import Classes.Hazard;
import Classes.Map;
import Classes.PathPoint;
import Methods.Maps_DatabaseAdapter;

public class FirestoreRequest {

    // Attributes
    private Context myContext_;
    private Maps_DatabaseAdapter myDBAdapter_;

    private RESPONSE myResponse_;

    private ArrayList<String> toBeSyncedCloudIds;
    private int syncIndex;

    private ArrayList<Map> toBeSentMaps;
    private int mapIndex;

    private ArrayList<PathPoint> toBeSentPathPoints;
    private int pathPointIndex;

    private ArrayList<Hazard> toBeSentHazards;
    private int hazardIndex;

    // Map Cloud Names
    private static final String MAP_NAME = "Map_Name";
    private static final String MAP_ALL_BOUNDS = "Bounds";
    private static final String MAP_AREA = "Area";
    private static final String MAP_PROGRESS = "Progress";
    private static final String MAP_NBROFHAZARDS = "Nbr_Of_Hazards";

    // Hazard Cloud Names
    private static final String HAZARD_LATITUDE = "Latitude";
    private static final String HAZARD_LONGITUDE = "Longitude";
    private static final String HAZARD_NOTES = "Notes";
    private static final String HAZARD_PICTURE = "Picture";
    private static final String HAZARD_STATUS = "Status";

    // Path Cloud Names
    private static final String PATH_LATITUDE = "Latitude";
    private static final String PATH_LONGITUDE = "Longitude";

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(int nbr_of_objects);
    }

    public FirestoreRequest(Context myContext, RESPONSE myResponse){
        this.myContext_ = myContext;
        this.myResponse_ = myResponse;
        this.myDBAdapter_ = new Maps_DatabaseAdapter(myContext_);
        myDBAdapter_.open();

        // Init Variables
        this.syncIndex = 0;
        this.toBeSyncedCloudIds = new ArrayList<>();
    }


    public void sendMaps(final ArrayList<Map> myMaps){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        if(myMaps == null){
            myResponse_.RESPONSE(0);
            return;
        }

        if(myMaps.size() < 1){
            myResponse_.RESPONSE(0);
            return;
        }

        ArrayList<Map> myMapsToBeSent = new ArrayList<>();
        for(int i = 0 ; i < myMaps.size() ; ++i){

            if(myMaps.get(i).getCloudSynced() == 0){
                myMapsToBeSent.add(myMaps.get(i));
            }
        }

        toBeSentMaps = new ArrayList<>();
        if (myMapsToBeSent.size() > 0) {
            toBeSentMaps = myMapsToBeSent;
            mapIndex = 0;
            sendMap(toBeSentMaps.get(mapIndex++));

        }else{
            myResponse_.RESPONSE(0);
        }

    }

    public void sendPathPoints(String mapCloudId, ArrayList<PathPoint> myPathPoints){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        if(myPathPoints == null){
            myResponse_.RESPONSE(0);
            return;
        }

        if(myPathPoints.size() < 1){
            myResponse_.RESPONSE(0);
            return;
        }

        ArrayList<PathPoint> myPathPointsToBeSent = new ArrayList<>();
        for(int i = 0 ; i < myPathPoints.size() ; ++i){

            if(myPathPoints.get(i).getCloudSynced() == 0){
                myPathPointsToBeSent.add(myPathPoints.get(i));
            }
        }

        toBeSentPathPoints = new ArrayList<>();
        if (myPathPointsToBeSent.size() > 0) {
            toBeSentPathPoints = myPathPointsToBeSent;
            pathPointIndex = 0;
            sendPathPoint(mapCloudId, toBeSentPathPoints.get(pathPointIndex++));

        }else{
            myResponse_.RESPONSE(0);
        }
    }

    public void sendPathPoint(final String mapCloudId, final PathPoint myPathPoint){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endpoint = "/" + mapCloudId + "/Path/" + myPathPoint.getCloudID();

        ArrayList<Field_Value> myFields = new ArrayList<>();
        myFields.add(new Field_Value(PATH_LATITUDE,myPathPoint.getLatlng().getMyLatitude()));
        myFields.add(new Field_Value(PATH_LONGITUDE,myPathPoint.getLatlng().getMyLongitude()));

        HTTPS_PATCH myRequest = new HTTPS_PATCH(myContext_, endpoint, myFields, new HTTPS_PATCH.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode) {

                if(responseCode == HttpURLConnection.HTTP_OK){

                    // Update sync field
                    myPathPoint.setCloudSynced(1);

                    // Save Locally Result
                    myDBAdapter_.updatePathPointsCloudSynced(myPathPoint);

                    if(toBeSentPathPoints == null){
                        myResponse_.RESPONSE(1);
                    }else if(pathPointIndex < toBeSentPathPoints.size()){
                        sendPathPoint(mapCloudId,toBeSentPathPoints.get(pathPointIndex++));
                    }else{
                        myResponse_.RESPONSE(pathPointIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(pathPointIndex-1);
            }
        });
        myRequest.execute();
    }

    public void sendMap(final Map myMap){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        ArrayList<Field_Value> myFields = new ArrayList<>();
        myFields.add(new Field_Value(MAP_NAME,myMap.getMapName()));
        myFields.add(new Field_Value(MAP_AREA,myMap.getTotalArea()));
        myFields.add(new Field_Value(MAP_PROGRESS,myMap.getProgress()));
        myFields.add(new Field_Value(MAP_NBROFHAZARDS,myMap.getNbrOfHazards()));
        myFields.add(new Field_Value(MAP_ALL_BOUNDS,myMap.getAllBoundsString()));

        String endpoint = "/" + myMap.getCloudID();

        HTTPS_PATCH myRequest = new HTTPS_PATCH(myContext_, endpoint, myFields, new HTTPS_PATCH.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode) {

                if(responseCode == HttpURLConnection.HTTP_OK){

                    // Update sync field
                    myMap.setCloudSynced(1);

                    // Save Locally Result
                    myDBAdapter_.updateMapCloudSynced(myMap);

                    if(toBeSentMaps == null){
                        myResponse_.RESPONSE(1);
                    }else if(mapIndex < toBeSentMaps.size()){
                        sendMap(toBeSentMaps.get(mapIndex++));
                    }else{
                        myResponse_.RESPONSE(mapIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(mapIndex -1);
            }
        });
        myRequest.execute();
    }

    public void updateMap(final Map myMap){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        ArrayList<Field_Value> myFields = new ArrayList<>();
        myFields.add(new Field_Value(MAP_PROGRESS,myMap.getProgress()));
        myFields.add(new Field_Value(MAP_NBROFHAZARDS,myMap.getNbrOfHazards()));

        String endpoint = "/" + myMap.getCloudID();

        HTTPS_PATCH myRequest = new HTTPS_PATCH(myContext_, endpoint, myFields, new HTTPS_PATCH.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode) {

                if(responseCode == HttpURLConnection.HTTP_OK){

                    // Update sync field
                    myMap.setCloudSynced(1);

                    // Save Locally Result
                    myDBAdapter_.updateMapCloudSynced(myMap);

                    if(mapIndex < toBeSentMaps.size()){
                        sendMap(toBeSentMaps.get(mapIndex++));
                    }else{
                        myResponse_.RESPONSE(mapIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(mapIndex -1);
            }
        });
        myRequest.execute();
    }

    public void sendHazards(String mapCloudId, ArrayList<Hazard> myHazards){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        if(myHazards == null){
            myResponse_.RESPONSE(0);
            return;
        }

        if(myHazards.size() < 1){
            myResponse_.RESPONSE(0);
            return;
        }

        ArrayList<Hazard> myHazardsToBeSent = new ArrayList<>();
        for(int i = 0 ; i < myHazards.size() ; ++i){

            if(myHazards.get(i).getCloudSynced() == 0){
                myHazardsToBeSent.add(myHazards.get(i));
            }
        }

        toBeSentHazards = new ArrayList<>();
        if (myHazardsToBeSent.size() > 0) {
            toBeSentHazards = myHazardsToBeSent;
            hazardIndex = 0;
            sendHazard(mapCloudId, toBeSentHazards.get(hazardIndex++));

        }else{
            myResponse_.RESPONSE(0);
        }
    }


    public void sendHazard(final String mapCloudID, final Hazard myHazard){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        ArrayList<Field_Value> myFields = new ArrayList<>();
        myFields.add(new Field_Value(HAZARD_LATITUDE,myHazard.getLocation().getMyLatitude()));
        myFields.add(new Field_Value(HAZARD_LONGITUDE,myHazard.getLocation().getMyLongitude()));
        myFields.add(new Field_Value(HAZARD_NOTES,myHazard.getNotes()));
        myFields.add(new Field_Value(HAZARD_PICTURE,myHazard.getPictureString()));
        myFields.add(new Field_Value(HAZARD_STATUS,myHazard.getStatus()));

        String endpoint = "/" + mapCloudID + "/Hazards/" + myHazard.getCloudID();

        HTTPS_PATCH myRequest = new HTTPS_PATCH(myContext_, endpoint, myFields, new HTTPS_PATCH.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode) {

                if(responseCode == HttpURLConnection.HTTP_OK){

                    // Update sync field
                    myHazard.setCloudSynced(1);

                    // Save Locally Result
                    myDBAdapter_.updateHazardCloudSynced(myHazard);

                    if(toBeSentHazards == null){
                        myResponse_.RESPONSE(1);
                    }else if(hazardIndex < toBeSentHazards.size()){
                        sendHazard(mapCloudID,toBeSentHazards.get(hazardIndex++));
                    }else{
                        myResponse_.RESPONSE(hazardIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(hazardIndex -1);

            }
        });
        myRequest.execute();
    }

    public void getPathPoints(final Map myMap){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endpoint = "/" + myMap.getCloudID() + "/Path";

        HTTPS_GET myRequest = new HTTPS_GET(myContext_, endpoint, new HTTPS_GET.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload) {
                if(responseCode == 200 && responsePayload != null){

                    if(responsePayload.size() < 1){
                        myResponse_.RESPONSE(0);
                        return;
                    }

                    // We get all the map cloud ids from the cloud
                    ArrayList<String> myCloudIds = new ArrayList<>();
                    for(int i = 0 ; i < responsePayload.size() ; ++i){
                        if(responsePayload.get(i).getName().equals("name")){

                            String[] path = responsePayload.get(i).getValue().split("/");
                            myCloudIds.add(path[path.length-1]);
                        }
                    }

                    // We get our local path points cloud ids
                    ArrayList<String> myPathPointsCloudIds = new ArrayList<>();
                    for(int i = 0 ; i < myMap.getPath().size() ; ++i){
                        myPathPointsCloudIds.add(myMap.getPath().get(i).getCloudID());
                    }

                    // For every path point that we dont have locally we get
                    ArrayList<String> newPathPointsCloudIds = new ArrayList<>();
                    for (String item : myCloudIds) {
                        if (!myPathPointsCloudIds.contains(item)) {
                            newPathPointsCloudIds.add(item);
                        }
                    }

                    syncIndex = 0;
                    if (newPathPointsCloudIds.size() > 0) {
                        toBeSyncedCloudIds = newPathPointsCloudIds;
                        getPathPointById(myMap.getCloudID(),toBeSyncedCloudIds.get(syncIndex++));

                    }else{
                        myResponse_.RESPONSE(syncIndex);
                    }
                }else{
                    myResponse_.RESPONSE(0);
                }
            }
        });
        myRequest.execute();
    }

    public void getPathPointById(final String mapCloudID, final String cloudID){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endPoint = "/" + mapCloudID + "/Path/" + cloudID;

        HTTPS_GET myRequest = new HTTPS_GET(myContext_, endPoint, new HTTPS_GET.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload) {
                if(responseCode == HttpURLConnection.HTTP_OK && responsePayload != null){

                    PathPoint newPathPoint = PathPoint.PathPointFromCloud(responsePayload,cloudID);

                    myDBAdapter_.createPathPoint(mapCloudID,newPathPoint);

                    if(syncIndex < toBeSyncedCloudIds.size()){
                        getPathPointById(mapCloudID,toBeSyncedCloudIds.get(syncIndex++));
                    }else{
                        myResponse_.RESPONSE(syncIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(syncIndex-1);
            }
        });
        myRequest.execute();
    }

    public void getMaps(final ArrayList<Map> myMaps){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endpoint = "";

        HTTPS_GET myRequest = new HTTPS_GET(myContext_, endpoint, new HTTPS_GET.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload) {
                if(responseCode == 200 && responsePayload != null){

                    if(responsePayload.size() < 1){
                        myResponse_.RESPONSE(0);
                        return;
                    }

                    // We get all the map cloud ids from the cloud
                    ArrayList<String> myCloudIds = new ArrayList<>();
                    for(int i = 0 ; i < responsePayload.size() ; ++i){
                        if(responsePayload.get(i).getName().equals("name")){

                            String[] path = responsePayload.get(i).getValue().split("/");
                            myCloudIds.add(path[path.length-1]);
                        }
                    }

                    // We get our local maps cloud ids
                    ArrayList<String> myMapsCloudIds = new ArrayList<>();
                    for(int i = 0 ; i < myMaps.size() ; ++i){
                        myMapsCloudIds.add(myMaps.get(i).getCloudID());
                    }

                    // For every map that we dont have locally we get
                    ArrayList<String> newMapsCloudIds = new ArrayList<>();
                    for (String item : myCloudIds) {
                        if (!myMapsCloudIds.contains(item)) {
                            newMapsCloudIds.add(item);
                        }
                    }

                    syncIndex = 0;
                    if (newMapsCloudIds.size() > 0) {
                        toBeSyncedCloudIds = newMapsCloudIds;
                        getMapById(toBeSyncedCloudIds.get(syncIndex++));

                    }else{
                        myResponse_.RESPONSE(syncIndex);
                    }
                }else{
                    myResponse_.RESPONSE(0);
                }
            }
        });
        myRequest.execute();
    }

    public void getMapById(final String cloudID){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endPoint = "/" + cloudID;

        HTTPS_GET myRequest = new HTTPS_GET(myContext_, endPoint, new HTTPS_GET.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload) {
                if(responseCode == HttpURLConnection.HTTP_OK && responsePayload != null){

                    Map newMap = Map.MapFromCloud(responsePayload,cloudID);

                    myDBAdapter_.createMap(newMap);

                    if(syncIndex < toBeSyncedCloudIds.size()){
                        getMapById(toBeSyncedCloudIds.get(syncIndex++));
                    }else{
                        myResponse_.RESPONSE(syncIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(syncIndex-1);
            }
        });
        myRequest.execute();
    }

    public void getHazards(final Map myMap){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endpoint = "/" + myMap.getCloudID() + "/Hazards";

        HTTPS_GET myRequest = new HTTPS_GET(myContext_, endpoint, new HTTPS_GET.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload) {
                if(responseCode == 200 && responsePayload != null){

                    if(responsePayload.size() < 1){
                        myResponse_.RESPONSE(0);
                        return;
                    }

                    // We get all the map cloud ids from the cloud
                    ArrayList<String> myCloudIds = new ArrayList<>();
                    for(int i = 0 ; i < responsePayload.size() ; ++i){
                        if(responsePayload.get(i).getName().equals("name")){

                            String[] path = responsePayload.get(i).getValue().split("/");
                            myCloudIds.add(path[path.length-1]);
                        }
                    }

                    // We get our local path points cloud ids
                    ArrayList<String> myHazardsCloudIds = new ArrayList<>();
                    for(int i = 0 ; i < myMap.getHazards().size() ; ++i){
                        myHazardsCloudIds.add(myMap.getHazards().get(i).getCloudID());
                    }

                    // For every path point that we dont have locally we get
                    ArrayList<String> newHazardsCloudIds = new ArrayList<>();
                    for (String item : myCloudIds) {
                        if (!myHazardsCloudIds.contains(item)) {
                            newHazardsCloudIds.add(item);
                        }
                    }

                    syncIndex = 0;
                    if (newHazardsCloudIds.size() > 0) {
                        toBeSyncedCloudIds = newHazardsCloudIds;
                        getHazardById(myMap.getCloudID(),toBeSyncedCloudIds.get(syncIndex++));

                    }else{
                        myResponse_.RESPONSE(syncIndex);
                    }
                }else{
                    myResponse_.RESPONSE(0);
                }
            }
        });
        myRequest.execute();
    }


    public void getHazardById(final String mapCloudID, final String cloudID){

        if(!isNetworkAvailable()){
            myResponse_.RESPONSE(0);
            return;
        }

        String endPoint = "/" + mapCloudID + "/Hazards/" + cloudID;

        HTTPS_GET myRequest = new HTTPS_GET(myContext_, endPoint, new HTTPS_GET.RESPONSE() {
            @Override
            public void RESPONSE(int responseCode, ArrayList<Field_Value> responsePayload) {
                if(responseCode == HttpURLConnection.HTTP_OK && responsePayload != null){

                    Hazard newHazard = Hazard.HazardFromCloud(responsePayload,cloudID);

                    myDBAdapter_.createHazard(mapCloudID,newHazard);

                    if(syncIndex < toBeSyncedCloudIds.size()){
                        getHazardById(mapCloudID,toBeSyncedCloudIds.get(syncIndex++));
                    }else{
                        myResponse_.RESPONSE(syncIndex);
                    }

                    return;

                }else if(responseCode == HttpURLConnection.HTTP_UNAUTHORIZED){
                    new HTTPS_RENEW_AUTH(myContext_).execute();
                }

                myResponse_.RESPONSE(syncIndex-1);
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
