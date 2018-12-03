package Classes;

import org.osmdroid.util.GeoPoint;

public class LatLng {

    // Attributes
    private GeoPoint geoPoint_;

    // Constructors
    public LatLng(double latitude, double longitude){

        this.geoPoint_ = new GeoPoint(latitude,longitude);
    }

    public LatLng(String latitude, String longitude){
        this.geoPoint_ = new GeoPoint(Double.parseDouble(latitude), Double.parseDouble(longitude));
    }

    public LatLng(GeoPoint myGeoPoint){
        this.geoPoint_ = myGeoPoint;
    }

    public LatLng(String myLatLngString){

        if(myLatLngString == null){
            return;
        }

        String[] myLatLng = myLatLngString.split(",");
        this.geoPoint_ = new GeoPoint(Double.parseDouble(myLatLng[0]), Double.parseDouble(myLatLng[1]));
    }

    // getter
    public double getMyLatitude(){
        return geoPoint_.getLatitude();
    }

    public double getMyLongitude(){
        return geoPoint_.getLongitude();
    }

    public String getLatLngString(){
        return getMyLatitude() + "," + getMyLongitude();
    }

    public GeoPoint getGeoPoint(){
        return geoPoint_;
    }
}
