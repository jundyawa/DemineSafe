package Methods;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import Classes.LatLng;

public class GPS_Methods {

    // Attributes
    private Context myContext_;

    private LocationManager locationManager;
    private LocationListener locationListener;

    private RESPONSE myRESPONSE_;

    // Interface for callbacks
    public interface RESPONSE{
        void RESPONSE(LatLng myLatLng);
    }

    // Constructor
    public GPS_Methods(Context myContext, RESPONSE myRESPONSE) {
        this.myContext_ = myContext;
        this.myRESPONSE_ = myRESPONSE;
    }

    public void startGPSListener(int minTime, int minDist, final int minAccuracy) {

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) myContext_.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {


                if (location.getAccuracy() < minAccuracy) {
                    // Called when a new location is found by the network location provider.
                    myRESPONSE_.RESPONSE(new LatLng(location.getLatitude(),location.getLongitude()));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {

                // Stop receiving updates
                //stopGPSListener();

                toastMessage("Activate your GPS");

                // Return null callback
                myRESPONSE_.RESPONSE(null);
            }
        };

        if (ActivityCompat.checkSelfPermission(myContext_, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Stop receiving updates
            stopGPSListener();

            toastMessage("Grant access to location in order to use GPS");

            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", myContext_.getPackageName(), null);
            i.setData(uri);
            myContext_.startActivity(i);

            // Return null callback
            myRESPONSE_.RESPONSE(null);

            return;
        }

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDist, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDist, locationListener);
    }

    // When the service is destroyed, we must ensure the listener is not active
    public void stopGPSListener() {

        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    // Toast Message function
    public void toastMessage(String message){
        Toast.makeText(myContext_,message,Toast.LENGTH_LONG).show();
    }
}
