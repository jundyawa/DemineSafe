package Classes;

import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.UUID;

public class PathPoint {

    // Attributes
    private int localID_;
    private String cloudID_;
    private LatLng latlng_;
    private int cloudSynced_;

    // Constructor
    public PathPoint(LatLng latlng){
        this.localID_ = 0;
        this.cloudID_ = UUID.randomUUID().toString();
        this.latlng_ = latlng;
        this.cloudSynced_ = 0;
    }

    public PathPoint(String cloudID, double latitude, double longitude){
        this.localID_ = 0;
        this.cloudID_ = cloudID;
        this.latlng_ = new LatLng(latitude,longitude);
        this.cloudSynced_ = 1;
    }

    public PathPoint(String cloudID, String myLatLngString, int cloudSynced){
        this.localID_ = 0;
        this.cloudID_ = cloudID;
        this.latlng_ = new LatLng(myLatLngString);
        this.cloudSynced_ = cloudSynced;
    }

    public static PathPoint PathPointFromCloud(ArrayList<Field_Value> myFields, String cloudID){

        double latitude = -1;
        double longitude = -1;

        String field_name;

        for(int i = 0 ; i < myFields.size() ; ++i){

            field_name = myFields.get(i).getName();

            if(field_name.equals("Latitude")){
                latitude = Double.parseDouble(myFields.get(i).getValue());
            }else if(field_name.equals("Longitude")){
                longitude = Double.parseDouble(myFields.get(i).getValue());
            }
        }

        if(latitude == -1){
            return null;

        }else if(longitude == -1){
            return null;
        }

        return new PathPoint(cloudID,latitude,longitude);
    }

    // Getter
    public String getCloudID() {
        return cloudID_;
    }

    public LatLng getLatlng() {
        return latlng_;
    }

    public int getCloudSynced() {
        return cloudSynced_;
    }

    // Setter

    public void setCloudSynced(int cloudSynced) {
        this.cloudSynced_ = cloudSynced;
    }
}
