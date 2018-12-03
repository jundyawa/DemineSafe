package Classes;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import org.osmdroid.util.GeoPoint;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class Hazard {

    // Attributes
    private int localID_;
    private String cloudID_;
    private LatLng location_;
    private String notes_;
    private Bitmap picture_;
    private int status_;
    private int cloudSynced_;

    private static final String HAZARD_LATITUDE = "Latitude";
    private static final String HAZARD_LONGITUDE = "Longitude";
    private static final String HAZARD_NOTES = "Notes";
    private static final String HAZARD_PICTURE = "Picture";
    private static final String HAZARD_STATUS = "Status";

    // Constructor
    public Hazard(LatLng location, String notes, Bitmap picture){
        this.localID_ = 0;
        this.cloudID_ = UUID.randomUUID().toString();
        this.location_ = location;
        this.notes_ = notes;
        this.picture_ = picture;
        this.status_ = 0;
        this.cloudSynced_ = 0;
    }

    // Full pull from sqlite
    public Hazard(int localID, String cloudID, String location, String notes, String picture, int status, int cloudSynced){
        this.localID_ = localID;
        this.cloudID_ = cloudID;
        this.location_ = convertLatLngString(location);
        this.notes_ = notes;
        this.picture_ = convertBitmapString(picture);
        this.status_ = status;
        this.cloudSynced_ = cloudSynced;
    }

    public Hazard(String cloudID, double latitude, double longitude, String notes, String picture, int status){
        this.localID_ = 0;
        this.cloudID_ = cloudID;
        this.location_ = new LatLng(latitude,longitude);
        this.notes_ = notes;
        this.picture_ = convertBitmapString(picture);
        this.status_ = status;
        this.cloudSynced_ = 1;
    }

    public static Hazard HazardFromCloud(ArrayList<Field_Value> myFields, String cloudID){

        double latitude = -1;
        double longitude = -1;
        String notes = null;
        String picture = null;
        int status = -1;

        String field_name;

        for(int i = 0 ; i < myFields.size() ; ++i){

            field_name = myFields.get(i).getName();

            if(field_name.equals(HAZARD_LATITUDE)){
                latitude = Double.parseDouble(myFields.get(i).getValue());
            }else if(field_name.equals(HAZARD_LONGITUDE)){
                longitude = Double.parseDouble(myFields.get(i).getValue());
            }else if(field_name.equals(HAZARD_NOTES)){
                notes = myFields.get(i).getValue();
            }else if(field_name.equals(HAZARD_PICTURE)){
                picture = myFields.get(i).getValue();
            }else if(field_name.equals(HAZARD_STATUS)){
                status = (int) Math.round(Double.parseDouble(myFields.get(i).getValue()));
            }
        }

        if(latitude == -1){
            return null;
        }else if(longitude == -1){
            return null;
        }else if(notes == null){
            return null;
        }else if(picture == null){
            return null;
        }else if(status == -1){
            return null;
        }

        return new Hazard(cloudID,latitude,longitude,notes,picture,status);
    }

    // Getter
    public int getLocalID(){
        return localID_;
    }

    public String getCloudID(){
        return cloudID_;
    }

    public LatLng getLocation(){
        return location_;
    }

    public String getNotes() {
        return notes_;
    }

    public Bitmap getPicture() {
        return picture_;
    }

    public int getStatus() {
        return status_;
    }

    // setter
    public void setStatus(int myStatus){
        status_ = myStatus;
        cloudSynced_ = 0;
    }

    public void setNotes(String notes){
        notes_ = notes;
        cloudSynced_ = 0;
    }

    public void setCloudSynced(int cloudSynced){ cloudSynced_ = cloudSynced;}

    public void setPosition(LatLng myLatLng){
        this.location_ = myLatLng;
        cloudSynced_ = 0;
    }

    // Custom methods
    public int getCloudSynced(){return cloudSynced_;}

    public String getLocationString(){
        return getLocation().getLatLngString();
    }


    public String getPictureString(){

        return convertBitmapToString(getPicture());

    }

    private String convertBitmapToString(Bitmap bitmap) {

        if(bitmap == null){
            return "";
        }

        // We create an empty Byte Array Output Stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // We convert the bitmap to a byte array output stream
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);

        // Format the byte array output stream to a byte array
        byte[] bitmapByteArray = outputStream.toByteArray();

        // Encode the byte array to a string using the Base64 algorithm
        String base64Image = Base64.encodeToString(bitmapByteArray, Base64.DEFAULT);

        return base64Image;
    }

    private LatLng convertLatLngString(String myLatLngString){

        if(myLatLngString == null){
            return null;
        }

        return new LatLng(myLatLngString);
    }

    private Bitmap convertBitmapString(String myBitmapString){

        Bitmap myBitmap;

        // Define bitmap decoding options
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inMutable = true;

        // Decode Base64 string into its byte array
        byte[] data = Base64.decode(myBitmapString, Base64.DEFAULT);

        // Decode byte array into its Bitmap
        myBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, opt);

        return myBitmap;
    }


}
