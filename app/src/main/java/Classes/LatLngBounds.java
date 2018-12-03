package Classes;

public class LatLngBounds {

    // Attributes
    private LatLng minLatLng_;
    private LatLng maxLatLng_;

    // Constructors
    public LatLngBounds(LatLng minLatLng, LatLng maxLatLng){
        this.minLatLng_ = minLatLng;
        this.maxLatLng_ = maxLatLng;
    }

    public LatLngBounds(String minLatLngString, String maxLatLngString){

        this.minLatLng_ = new LatLng(minLatLngString);
        this.maxLatLng_ = new LatLng(maxLatLngString);
    }

    // getter
    public LatLng getMinLatLng(){
        return minLatLng_;
    }

    public LatLng getMaxLatLng(){
        return maxLatLng_;
    }

    public String getRectBoundsString(){

        return getMinLatLng().getMyLatitude() + "," + getMinLatLng().getMyLongitude() +
                ";" + getMaxLatLng().getMyLatitude() + "," + getMaxLatLng().getMyLongitude();
    }
}
