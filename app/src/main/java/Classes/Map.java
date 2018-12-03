package Classes;

import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.PolygonArea;
import net.sf.geographiclib.PolygonResult;

import java.util.ArrayList;
import java.util.UUID;

public class Map {

    // Attributes
    private int localID_;
    private String cloudID_;
    private String mapName_;
    private LatLngBounds rectBounds_;
    private ArrayList<LatLng> allBounds_;
    private double totalArea_;
    private double progress_;
    private int nbrOfHazards_;
    private ArrayList<Hazard> hazards_;
    private ArrayList<PathPoint> path_;
    private int cloudSynced_;

    // Constructor
    public Map(String mapName, ArrayList<LatLng> allBounds){
        this.localID_ = 0;
        this.cloudID_ = UUID.randomUUID().toString();
        this.mapName_ = mapName;
        this.rectBounds_ = computeRectBounds(allBounds);
        this.allBounds_ = allBounds;
        this.totalArea_ = computeTotalArea(allBounds);
        this.progress_ = 0;
        this.nbrOfHazards_ = 0;
        this.hazards_ = null;
        this.path_ = null;
        this.cloudSynced_ = 0;
    }

    // Full pull from SQLite
    public Map(int localID, String cloudID, String mapName, String rectBounds, String allBounds,
               double totalArea, double progress, int nbrOfHazards, int cloudSynced){
        this.localID_ = localID;
        this.cloudID_ = cloudID;
        this.mapName_ = mapName;
        this.rectBounds_ = convertLatLngBoundsString(rectBounds);
        this.allBounds_ = convertAllBoundsString(allBounds);
        this.totalArea_ = totalArea;
        this.progress_ = progress;
        this.nbrOfHazards_ = nbrOfHazards;
        this.hazards_ = null;
        this.path_ = null;
        this.cloudSynced_ = cloudSynced;
    }

    public Map(int localID, String cloudID, String mapName, LatLngBounds rectBounds, ArrayList<LatLng> allBounds,
               double totalArea, double progress, int nbrOfHazards, int cloudSynced){
        this.localID_ = localID;
        this.cloudID_ = cloudID;
        this.mapName_ = mapName;
        this.rectBounds_ = rectBounds;
        this.allBounds_ = allBounds;
        this.totalArea_ = totalArea;
        this.progress_ = progress;
        this.nbrOfHazards_ = nbrOfHazards;
        this.hazards_ = null;
        this.path_ = null;
        this.cloudSynced_ = cloudSynced;
    }

    public static Map MapFromCloud(ArrayList<Field_Value> myFields, String cloudID){

        String Map_Name = null;
        double Area = -1;
        double Progress = -1;
        int Nbr_Of_Hazard = -1;
        ArrayList<LatLng> All_Bounds = null;

        String field_name;

        for(int i = 0 ; i < myFields.size() ; ++i){

            field_name = myFields.get(i).getName();

            if(field_name.equals("Map_Name")){
                Map_Name = myFields.get(i).getValue();
            }else if(field_name.equals("Area")){
                Area = Double.parseDouble(myFields.get(i).getValue());
            }else if(field_name.equals("Progress")){
                Progress = Double.parseDouble(myFields.get(i).getValue());
            }else if(field_name.equals("Nbr_Of_Hazards")){
                Nbr_Of_Hazard = (int) Math.round(Double.parseDouble(myFields.get(i).getValue()));
            }else if(field_name.equals("Bounds")){
                All_Bounds = convertAllBoundsString(myFields.get(i).getValue());
            }
        }

        if(Map_Name == null){
            return null;

        }else if(Area == -1){
            return null;

        }else if(Progress == -1){
            return null;

        }else if(Nbr_Of_Hazard == -1){
            return null;

        }else if(All_Bounds == null){
            return null;
        }

        return new Map(0,cloudID,Map_Name,computeRectBounds(All_Bounds),All_Bounds,Area,Progress,Nbr_Of_Hazard,1);
    }

    // --- Getter Methods ---
    public int getLocalID(){
        return localID_;
    }

    public String getCloudID(){
        return cloudID_;
    }

    public String getMapName(){
        return mapName_;
    }

    public LatLngBounds getRectBounds() {
        return rectBounds_;
    }

    public ArrayList<LatLng> getAllBounds() {
        return allBounds_;
    }

    public double getTotalArea() {
        return totalArea_;
    }

    public double getProgress() {
        return progress_;
    }

    public int getNbrOfHazards() {
        return nbrOfHazards_;
    }

    public ArrayList<Hazard> getHazards() {
        return hazards_;
    }

    public ArrayList<PathPoint> getPath() {
        return path_;
    }

    public int getCloudSynced() {
        return cloudSynced_;
    }

    // adder
    public void addHazard(Hazard myHazard){
        hazards_.add(myHazard);
        nbrOfHazards_ = nbrOfHazards_ + 1;
        cloudSynced_ = 0;
    }

    public void addToPath(PathPoint myPathPoint){
        path_.add(myPathPoint);
        cloudSynced_ = 0;
    }


    // Setter
    public void setHazards(ArrayList<Hazard> hazards){
        hazards_ = hazards;
    }

    public void setPath(ArrayList<PathPoint> path){
        path_ = path;
    }

    public void setCloudSynced(int cloudSynced) {
        this.cloudSynced_ = cloudSynced;
    }

    public void setProgress(double progress) {
        this.progress_ = progress;
        cloudSynced_ = 0;
    }

    // Custom methods
    private double computeTotalArea(ArrayList<LatLng> myBounds){

        if(myBounds == null){
            return 0.0;
        }

        if(myBounds.size() < 3){
            return 0.0;
        }

        // Define the World Geodetic System
        Geodesic geod = Geodesic.WGS84;

        // Init a polygon
        PolygonArea p = new PolygonArea(geod, false);

        // Add lat lng to the polygon
        for(int i = 0 ; i < myBounds.size() ; ++i){
            p.AddPoint(myBounds.get(i).getMyLatitude(),myBounds.get(i).getMyLongitude());
        }

        // Compute
        PolygonResult r = p.Compute();

        double area = Math.abs(r.area);

        double area_rounded = Math.round(area*100.0)/100.0;

        // Return area
        return area_rounded;
    }

    private static LatLngBounds computeRectBounds(ArrayList<LatLng> myBounds){

        if(myBounds == null){
            return null;
        }

        if(myBounds.size() < 3){
            return null;
        }

        double minLat = myBounds.get(0).getMyLatitude();
        double minLng = myBounds.get(0).getMyLongitude();
        double maxLat = myBounds.get(0).getMyLatitude();
        double maxLng = myBounds.get(0).getMyLongitude();

        for(int i = 1 ; i < myBounds.size() ; ++i){

            if(myBounds.get(i).getMyLatitude() < minLat){
                minLat = myBounds.get(i).getMyLatitude();
            }

            if(myBounds.get(i).getMyLatitude() > maxLat){
                maxLat = myBounds.get(i).getMyLatitude();
            }

            if(myBounds.get(i).getMyLongitude() < minLng){
                minLng = myBounds.get(i).getMyLongitude();
            }

            if(myBounds.get(i).getMyLongitude() > maxLng){
                maxLng = myBounds.get(i).getMyLongitude();
            }
        }

        return new LatLngBounds(new LatLng(minLat,minLng), new LatLng(maxLat,maxLng));
    }

    public String getRectBoundsString(){

        return getRectBounds().getRectBoundsString();
    }

    public String getAllBoundsString(){

        if(getAllBounds().size() < 3){
            return null;
        }

        StringBuilder myStrBuilder = new StringBuilder("");

        LatLng myLoc;

        for(int i = 0 ; i < getAllBounds().size() ; ++i){

            myLoc = getAllBounds().get(i);

            myStrBuilder.append(myLoc.getLatLngString());

            if(i < getAllBounds().size() - 1){
                myStrBuilder.append(";");
            }
        }

        return myStrBuilder.toString();
    }

    private LatLngBounds convertLatLngBoundsString(String myLatLngBoundsString){

        if(myLatLngBoundsString == null){
            return null;
        }

        String[] myLatLngBoundsStrings = myLatLngBoundsString.split(";");

        String minLatLngString = myLatLngBoundsStrings[0];
        String maxLatLngString = myLatLngBoundsStrings[1];

        return new LatLngBounds(minLatLngString,maxLatLngString);
    }

    private static ArrayList<LatLng> convertAllBoundsString(String myAllBoundsString){

        ArrayList<LatLng> myAllBounds = new ArrayList<>();

        if(myAllBoundsString == null){
            return myAllBounds;
        }

        String[] myAllBoundsStrings = myAllBoundsString.split(";");

        for (String myAllBoundsString1 : myAllBoundsStrings) {
            myAllBounds.add(new LatLng(myAllBoundsString1));
        }

        return myAllBounds;
    }

}
