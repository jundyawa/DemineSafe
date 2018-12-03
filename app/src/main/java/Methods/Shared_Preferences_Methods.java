package Methods;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import java.util.ArrayList;

import Classes.Isotope;
import Classes.LatLng;

public class Shared_Preferences_Methods {

    // --- Attributes ---
    private Context myContext_;
    private SharedPreferences mySharedPref_;
    private SharedPreferences.Editor myEditor_;

    private final String endPoint = "DemineSafe_SharedPrefs";

    // Constructor
    public Shared_Preferences_Methods(Context myContext){
        this.myContext_ = myContext;
        mySharedPref_ = myContext_.getSharedPreferences(endPoint, Context.MODE_PRIVATE);
        myEditor_ = mySharedPref_.edit();
    }

    public void saveUsername(String username){

        myEditor_.putString("last_username", username);
        myEditor_.apply();
    }

    public String getUsername(){

        return mySharedPref_.getString("last_username",null);
    }

    public void saveIdToken(String idToken){

        myEditor_.putString("idToken", idToken);
        myEditor_.apply();
    }

    public String getIdToken(){

        return mySharedPref_.getString("idToken",null);
    }

    public void saveAccount(String username, String password){

        myEditor_.putString(username, encrypt(password));
        myEditor_.apply();
    }

    public String getPassword(String username){

        return decrypt(mySharedPref_.getString(username,null));
    }

    public boolean accountCheck(String username, String password){

        // Read
        String saved_password = getPassword(username);

        if(saved_password != null){

            return saved_password.equals(password);

        }else{
            return false;
        }
    }

    public static String encrypt(String input) {
        return Base64.encodeToString(input.getBytes(), Base64.DEFAULT);
    }

    @NonNull
    public static String decrypt(String input) {

        if(input != null) {
            return new String(Base64.decode(input, Base64.DEFAULT));
        }else{
            return null;
        }
    }


    public LatLng getSavedLocation(){

        Double LAT = Double.longBitsToDouble(mySharedPref_.getLong("latitude",0));
        Double LNG = Double.longBitsToDouble(mySharedPref_.getLong("longitude",0));

        return new LatLng(LAT,LNG);
    }

    public double getSavedZoom(){

        Double ZOOM = Double.longBitsToDouble(mySharedPref_.getLong("zoom",0));

        return ZOOM;
    }

    public void saveLocationZoom(double latitude, double longitude, double zoomLevelDouble) {

        myEditor_.putLong("latitude",Double.doubleToRawLongBits(latitude));
        myEditor_.putLong("longitude",Double.doubleToRawLongBits(longitude));
        myEditor_.putLong("zoom", Double.doubleToRawLongBits(zoomLevelDouble));
        myEditor_.apply();
    }

    public void saveNoiseLevel(float myNoiseLevel){

        myEditor_.putFloat("noise_level",myNoiseLevel);
        myEditor_.apply();
    }

    public float getNoiseThreshold(){

        return mySharedPref_.getFloat("noise_level",0);
    }

    public void saveMinFrontPoints(int myMinFrontPoints){

        myEditor_.putInt("min_front_point",myMinFrontPoints);
        myEditor_.apply();
    }

    public void saveEnergyWidth(double energy_delta, double min_energy) {

        // energy(width) = energy_delta*width + min_energy

        myEditor_.putLong("min_energy",Double.doubleToLongBits(min_energy));
        myEditor_.apply();

        myEditor_.putLong("energy_delta",Double.doubleToLongBits(energy_delta));
        myEditor_.apply();
    }

    public double[] getEnergyWidth() {

        // energy(width) = energy_delta*width + min_energy

        double min_energy = Double.longBitsToDouble(mySharedPref_.getLong("min_energy",0));
        double energy_delta = Double.longBitsToDouble(mySharedPref_.getLong("energy_delta",0));

        double[] myInfo = new double[2];
        myInfo[0] = min_energy;
        myInfo[1] = energy_delta;

        return myInfo;
    }



    public int getMinFrontPoints() {

        return mySharedPref_.getInt("min_front_point",3);
    }

    public void saveBackgroundNoise(short[] myData, int timeInSeconds) {


        StringBuilder myDataStr = new StringBuilder();

        for(int i = 0 ; i < myData.length ; ++i){

            float my_val = myData[i]/(1.0f*timeInSeconds);

            myDataStr.append(my_val);

            if(i < myData.length - 1){
                myDataStr.append(",");
            }
        }

        myEditor_.putString("background_data", myDataStr.toString());
        myEditor_.apply();
    }

    public float[] getBackgroundNoise() {

        String allData = mySharedPref_.getString("background_data",null);

        if(allData == null){
            return null;
        }

        String[] allDataArray = allData.split(",");

        float[] myArray = new float[allDataArray.length];

        for(int i = 0 ; i < allDataArray.length ; ++i){
            myArray[i] = Float.parseFloat(allDataArray[i]);
        }

        return myArray;
    }

    public ArrayList<Isotope> getIsotopes() {

        ArrayList<Isotope> myIsotopes = new ArrayList<>();

        String isotopesName = mySharedPref_.getString("isotopes_name","");
        String isotopesEnergy = mySharedPref_.getString("isotopes_energy","");
        String isotopesX_pos = mySharedPref_.getString("isotopes_x_pos","");

        if(!isotopesName.isEmpty() && !isotopesEnergy.isEmpty() && !isotopesX_pos.isEmpty()){
            String[] names = isotopesName.split(",");
            String[] energies = isotopesEnergy.split(",");
            String[] x_poss = isotopesX_pos.split(",");

            if(names.length != energies.length || names.length != x_poss.length){
                return null;
            }

            if(names.length < 1){
                return null;
            }

            for(int i = 0 ; i < names.length ; ++i){
                myIsotopes.add(new Isotope(names[i],Double.parseDouble(energies[i]), Float.parseFloat(x_poss[i])));
            }

            return myIsotopes;

        }else{
            return null;
        }
    }

    public void addIsotope(Isotope myIsotope){

        ArrayList<Isotope> myIsotopes = getIsotopes();

        if(myIsotopes == null){
            myIsotopes = new ArrayList<>();
        }

        myIsotopes.add(myIsotope);
        saveIsotopes(myIsotopes);
    }

    public void removeIsotope(Isotope myIsotope){

        ArrayList<Isotope> myIsotopes = getIsotopes();

        if(myIsotopes == null){
            return;
        }

        for(int i = 0 ; i < myIsotopes.size() ; ++i){

            if(myIsotopes.get(i).getName().equals(myIsotope.getName())){
                myIsotopes.remove(i);
                break;
            }
        }

        saveIsotopes(myIsotopes);
    }



    public void updateIsotope(Isotope myIsotope){

        ArrayList<Isotope> myIsotopes = getIsotopes();

        if(myIsotopes == null){
            return;
        }

        for(int i = 0 ; i < myIsotopes.size() ; ++i){

            if(myIsotopes.get(i).getName().equals(myIsotope.getName())){
                myIsotopes.get(i).setX_pos(myIsotope.getX_pos());
                break;
            }
        }

        saveIsotopes(myIsotopes);
    }

    public void saveIsotopes(ArrayList<Isotope> myIsotopes){

        if(myIsotopes == null){
            myEditor_.putString("isotopes_name","");
            myEditor_.apply();

            myEditor_.putString("isotopes_energy","");
            myEditor_.apply();

            myEditor_.putString("isotopes_x_pos","");
            myEditor_.apply();

            return;
        }

        if(myIsotopes.size() < 1){
            myEditor_.putString("isotopes_name","");
            myEditor_.apply();

            myEditor_.putString("isotopes_energy","");
            myEditor_.apply();

            myEditor_.putString("isotopes_x_pos","");
            myEditor_.apply();

            return;
        }


        StringBuilder names = new StringBuilder("");
        StringBuilder energies = new StringBuilder("");
        StringBuilder x_pos = new StringBuilder("");

        for(int i = 0 ; i < myIsotopes.size() ; ++i){

            names.append(myIsotopes.get(i).getName());
            energies.append(String.valueOf(myIsotopes.get(i).getEnergy()));
            x_pos.append(String.valueOf(myIsotopes.get(i).getX_pos()));

            if(i < myIsotopes.size() - 1) {
                names.append(",");
                energies.append(",");
                x_pos.append(",");
            }
        }
        myEditor_.putString("isotopes_name",names.toString());
        myEditor_.apply();

        myEditor_.putString("isotopes_energy",energies.toString());
        myEditor_.apply();

        myEditor_.putString("isotopes_x_pos",x_pos.toString());
        myEditor_.apply();
    }

    public void saveChangeSetId(String id) {
        myEditor_.putString("changeset_id",id);
        myEditor_.apply();
    }

    public String getChangeSetId() {
        return mySharedPref_.getString("changeset_id",null);
    }
}
