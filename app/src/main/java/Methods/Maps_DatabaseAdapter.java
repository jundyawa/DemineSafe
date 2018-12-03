package Methods;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.util.ArrayList;

import Classes.Hazard;
import Classes.LatLng;
import Classes.Map;
import Classes.PathPoint;
import Classes.TimeStamp;
import REST_API.FirestoreRequest;
import REST_API.OSMRequest;

public class Maps_DatabaseAdapter {

    // -------------------------------------------------------------------------
    // ------------------------------ TABLE ------------------------------------
    // -------------------------------------------------------------------------
    // | Local Id | Cloud Id | Map Name | Rectangular Bounds | All Outer Bounds | Total Area | Hazards Table | Path Table

    // Database Strings
    private String DB_NAME;
    private static final String DB_HEADER = "DemineSafe";
    private static final int DB_VERSION = 1;

    // Table Names
    private static final String TABLE_HAZARDS = "hazards";
    private static final String TABLE_PATH = "path";
    private static final String TABLE_MAP = "map";

    // Common column names
    private static final String LOCAL_ID = "local_id";
    private static final String CLOUD_ID = "cloud_id";
    private static final String CREATED_TIME = "createdTime";
    private static final String UPDATED_TIME = "updatedTime";
    private static final String CLOUD_SYNCED = "cloudSynced";

    // MAP Table column names
    private static final String MAP_NAME = "name";
    private static final String MAP_RECT_BOUNDS = "rect_bounds";
    private static final String MAP_ALL_BOUNDS = "all_bounds";
    private static final String MAP_AREA = "area";
    private static final String MAP_PROGRESS = "progress";
    private static final String MAP_NBROFHAZARDS = "nbrOfHazards";

    // Hazard Table column names
    private static final String HAZARD_MAP_CLOUD_FK = "map_fk";
    private static final String HAZARD_LOCATION = "location";
    private static final String HAZARD_NOTES = "notes";
    private static final String HAZARD_PICTURE = "picture";
    private static final String HAZARD_STATUS = "status";

    // Hazard Table column names
    private static final String PATH_MAP_CLOUD_FK = "map_fk";
    private static final String PATH_LOCATION = "location";

    // --- Table Create Statements ---
    // Map Table
    private static final String CREATE_TABLE_MAP = "CREATE TABLE "
            + TABLE_MAP + "(" + LOCAL_ID + " INTEGER PRIMARY KEY," + CLOUD_ID
            + " TEXT," + CREATED_TIME + " TEXT," + UPDATED_TIME
            + " TEXT," + MAP_NAME + " TEXT," + MAP_RECT_BOUNDS + " TEXT," +
            MAP_ALL_BOUNDS + " TEXT," + MAP_AREA + " REAL," + MAP_PROGRESS + " REAL,"
            + MAP_NBROFHAZARDS + " INTEGER," + CLOUD_SYNCED + " INTEGER" + ")";

    // Hazard Table
    private static final String CREATE_TABLE_HAZARD = "CREATE TABLE "
            + TABLE_HAZARDS + "(" + LOCAL_ID + " INTEGER PRIMARY KEY," + CLOUD_ID
            + " TEXT," + CREATED_TIME + " TEXT," + UPDATED_TIME
            + " TEXT," + HAZARD_MAP_CLOUD_FK + " TEXT," + HAZARD_LOCATION + " TEXT," + HAZARD_NOTES + " TEXT," +
            HAZARD_PICTURE + " TEXT," + HAZARD_STATUS + " INTEGER," + CLOUD_SYNCED + " INTEGER" + ")";

    // Path Table
    private static final String CREATE_TABLE_PATH = "CREATE TABLE "
            + TABLE_PATH + "(" + LOCAL_ID + " INTEGER PRIMARY KEY," + CLOUD_ID
            + " TEXT," + PATH_MAP_CLOUD_FK
            + " TEXT,"+ PATH_LOCATION + " TEXT," + CLOUD_SYNCED + " INTEGER" + ")";


    // Creates Database
    private static class DatabaseHelper extends SQLiteOpenHelper {

        private static DatabaseHelper mInstance = null;

        private String myUsername_;


        static DatabaseHelper getInstance(Context context) {

            // Use the application context, which will ensure that you
            // don't accidentally leak an Activity's context.
            // See this article for more information: http://bit.ly/6LRzfx
            if (mInstance == null) {
                mInstance = new DatabaseHelper(context.getApplicationContext());
            }
            return mInstance;
        }

        public static void refreshInstance() {
            mInstance = null;
        }


        private DatabaseHelper(Context context) {
            super(context, DB_HEADER + "_" + new Shared_Preferences_Methods(context).getUsername().split("@")[0], null, DB_VERSION);
            myUsername_ = new Shared_Preferences_Methods(context).getUsername().split("@")[0];
        }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {

            sqLiteDatabase.execSQL(CREATE_TABLE_MAP);
            sqLiteDatabase.execSQL(CREATE_TABLE_HAZARD);
            sqLiteDatabase.execSQL(CREATE_TABLE_PATH);

        }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            // on upgrade drop older tables
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MAP);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_HAZARDS);
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_PATH);

            // create new tables
            onCreate(sqLiteDatabase);
        }
    }

    // Adapter to establish a connection with the database
    private final Context myContext;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase sqLiteDatabase;

    public Maps_DatabaseAdapter(Context context) {

        this.myContext = context;
        DB_NAME = DB_HEADER + "_" + new Shared_Preferences_Methods(context).getUsername().split("@")[0];
    }

    public void open() throws SQLException {

        dbHelper = DatabaseHelper.getInstance(myContext);
        sqLiteDatabase = dbHelper.getWritableDatabase();
    }


    public long createMap(Map myMap){

        ContentValues contentValues = new ContentValues();
        contentValues.put(CLOUD_ID, myMap.getCloudID());
        contentValues.put(CREATED_TIME, TimeStamp.getTimeStamp_String());
        contentValues.put(UPDATED_TIME, TimeStamp.getTimeStamp_String());
        contentValues.put(MAP_NAME, myMap.getMapName());
        contentValues.put(MAP_RECT_BOUNDS, myMap.getRectBoundsString());
        contentValues.put(MAP_ALL_BOUNDS, myMap.getAllBoundsString());
        contentValues.put(MAP_AREA, myMap.getTotalArea());
        contentValues.put(MAP_PROGRESS, myMap.getProgress());
        contentValues.put(MAP_NBROFHAZARDS, myMap.getNbrOfHazards());
        contentValues.put(CLOUD_SYNCED, myMap.getCloudSynced());

        long map_id = sqLiteDatabase.insert(TABLE_MAP, null, contentValues);

        return map_id;
    }

    public long createHazard(String mapCloudID, Hazard myHazard){

        ContentValues contentValues = new ContentValues();
        contentValues.put(CLOUD_ID, myHazard.getCloudID());
        contentValues.put(CREATED_TIME, TimeStamp.getTimeStamp_String());
        contentValues.put(UPDATED_TIME, TimeStamp.getTimeStamp_String());
        contentValues.put(HAZARD_MAP_CLOUD_FK, mapCloudID);
        contentValues.put(HAZARD_LOCATION, myHazard.getLocationString());
        contentValues.put(HAZARD_NOTES, myHazard.getNotes());
        contentValues.put(HAZARD_PICTURE, myHazard.getPictureString());
        contentValues.put(HAZARD_STATUS, myHazard.getStatus());
        contentValues.put(CLOUD_SYNCED, myHazard.getCloudSynced());

        long hazard_id = sqLiteDatabase.insert(TABLE_HAZARDS, null, contentValues);

        return hazard_id;
    }

    public long createPathPoint(String mapCloudID, PathPoint myPathPoint){

        ContentValues contentValues = new ContentValues();
        contentValues.put(CLOUD_ID, myPathPoint.getCloudID());
        contentValues.put(PATH_MAP_CLOUD_FK, mapCloudID);
        contentValues.put(PATH_LOCATION, myPathPoint.getLatlng().getLatLngString());
        contentValues.put(CLOUD_SYNCED, myPathPoint.getCloudSynced());

        long path_id = sqLiteDatabase.insert(TABLE_PATH, null, contentValues);

        return path_id;
    }

    public ArrayList<Map> getAllMaps(){

        String query = "SELECT * FROM " + TABLE_MAP + " ORDER BY " + MAP_AREA + " ASC";
        Cursor mapCursor = sqLiteDatabase.rawQuery(query, null);
        mapCursor.moveToFirst();

        if (mapCursor == null) {
            return null;
        }

        ArrayList<Map> myMaps = new ArrayList<>();

        while(!mapCursor.isAfterLast()) {

            Map myMap = new Map(mapCursor.getInt(mapCursor.getColumnIndex(LOCAL_ID)), mapCursor.getString(mapCursor.getColumnIndex(CLOUD_ID)),
                    mapCursor.getString(mapCursor.getColumnIndex(MAP_NAME)), mapCursor.getString(mapCursor.getColumnIndex(MAP_RECT_BOUNDS)),
                    mapCursor.getString(mapCursor.getColumnIndex(MAP_ALL_BOUNDS)), mapCursor.getDouble(mapCursor.getColumnIndex(MAP_AREA)),
                    mapCursor.getDouble(mapCursor.getColumnIndex(MAP_PROGRESS)),mapCursor.getInt(mapCursor.getColumnIndex(MAP_NBROFHAZARDS)),
                    mapCursor.getInt(mapCursor.getColumnIndex(CLOUD_SYNCED)));

            myMaps.add(myMap);

            mapCursor.moveToNext();
        }

        mapCursor.close();

        return myMaps;
    }

    public ArrayList<Hazard> getAllHazards(){

        String query = "SELECT * FROM " + TABLE_HAZARDS;
        Cursor hazardsCursor = sqLiteDatabase.rawQuery(query, null);
        hazardsCursor.moveToFirst();

        if (hazardsCursor == null) {
            return null;
        }

        ArrayList<Hazard> myHazards = new ArrayList<>();
        String mapCloudID = "";

        while(!hazardsCursor.isAfterLast()) {

            mapCloudID = hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_MAP_CLOUD_FK));

            Hazard newHazard = new Hazard(hazardsCursor.getInt(hazardsCursor.getColumnIndex(LOCAL_ID)),
                    hazardsCursor.getString(hazardsCursor.getColumnIndex(CLOUD_ID)),
                    hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_LOCATION)),
                    hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_NOTES)),
                    hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_PICTURE)),
                    hazardsCursor.getInt(hazardsCursor.getColumnIndex(HAZARD_STATUS)),
                    hazardsCursor.getInt(hazardsCursor.getColumnIndex(CLOUD_SYNCED)));

            myHazards.add(newHazard);

            if(newHazard.getCloudSynced() == 0){

                sendHazardToOSM(newHazard);

                FirestoreRequest myRequest = new FirestoreRequest(myContext, new FirestoreRequest.RESPONSE() {
                    @Override
                    public void RESPONSE(int nbr_of_objects) {}
                });
                myRequest.sendHazard(mapCloudID,newHazard);
            }

            hazardsCursor.moveToNext();
        }

        hazardsCursor.close();

        return myHazards;
    }

    private void sendHazardToOSM(Hazard myHazard){
        OSMRequest myRequest = new OSMRequest(myContext);
        myRequest.sendHazardtoOSM(myHazard);
    }

    public Map getMapByCloudId(String myCloudId){

        // Map select query string
        String MapSelectQuery = "SELECT * FROM " + TABLE_MAP + " WHERE "
                + CLOUD_ID + " = '" + myCloudId + "'";

        Cursor mapCursor = sqLiteDatabase.rawQuery(MapSelectQuery, null);

        if (mapCursor == null) {
           return null;
        }

        mapCursor.moveToFirst();

        Map myMap = new Map(mapCursor.getInt(mapCursor.getColumnIndex(LOCAL_ID)), mapCursor.getString(mapCursor.getColumnIndex(CLOUD_ID)),
                mapCursor.getString(mapCursor.getColumnIndex(MAP_NAME)), mapCursor.getString(mapCursor.getColumnIndex(MAP_RECT_BOUNDS)),
                mapCursor.getString(mapCursor.getColumnIndex(MAP_ALL_BOUNDS)), mapCursor.getDouble(mapCursor.getColumnIndex(MAP_AREA)),
                mapCursor.getDouble(mapCursor.getColumnIndex(MAP_PROGRESS)),mapCursor.getInt(mapCursor.getColumnIndex(MAP_NBROFHAZARDS)),
                mapCursor.getInt(mapCursor.getColumnIndex(CLOUD_SYNCED)));

        mapCursor.close();

        // Hazards select query string
        String HazardsSelectQuery = "SELECT * FROM " + TABLE_HAZARDS + " WHERE "
                + HAZARD_MAP_CLOUD_FK + " = '" + myMap.getCloudID() + "'";

        Cursor hazardsCursor = sqLiteDatabase.rawQuery(HazardsSelectQuery, null);

        if(hazardsCursor != null){

            ArrayList<Hazard> myHazards = new ArrayList<>();

            hazardsCursor.moveToFirst();

            while(!hazardsCursor.isAfterLast()){

                myHazards.add(new Hazard(hazardsCursor.getInt(hazardsCursor.getColumnIndex(LOCAL_ID)),
                        hazardsCursor.getString(hazardsCursor.getColumnIndex(CLOUD_ID)),
                        hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_LOCATION)),
                        hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_NOTES)),
                        hazardsCursor.getString(hazardsCursor.getColumnIndex(HAZARD_PICTURE)),
                        hazardsCursor.getInt(hazardsCursor.getColumnIndex(HAZARD_STATUS)),
                        hazardsCursor.getInt(hazardsCursor.getColumnIndex(CLOUD_SYNCED))));

                hazardsCursor.moveToNext();
            }

            myMap.setHazards(myHazards);

            hazardsCursor.close();
        }

        // Path select query string
        String PathSelectQuery = "SELECT * FROM " + TABLE_PATH + " WHERE "
                + PATH_MAP_CLOUD_FK + " = '" + myMap.getCloudID() + "'";

        Cursor pathCursor = sqLiteDatabase.rawQuery(PathSelectQuery, null);

        if(pathCursor != null){

            ArrayList<PathPoint> myPathPoints = new ArrayList<>();

            pathCursor.moveToFirst();

            while(!pathCursor.isAfterLast()){

                myPathPoints.add(new PathPoint(pathCursor.getString(pathCursor.getColumnIndex(CLOUD_ID)),
                        pathCursor.getString(pathCursor.getColumnIndex(PATH_LOCATION)),
                        pathCursor.getInt(pathCursor.getColumnIndex(CLOUD_SYNCED))));

                pathCursor.moveToNext();
            }

            myMap.setPath(myPathPoints);

            pathCursor.close();
        }

        return myMap;
    }

    public int updateHazard(Hazard myHazard){

        ContentValues contentValues = new ContentValues();
        contentValues.put(UPDATED_TIME, TimeStamp.getTimeStamp_String());
        contentValues.put(HAZARD_NOTES, myHazard.getNotes());
        contentValues.put(HAZARD_LOCATION, myHazard.getLocationString());
        contentValues.put(HAZARD_STATUS, myHazard.getStatus());
        contentValues.put(CLOUD_SYNCED, myHazard.getCloudSynced());

        // updating row
        return sqLiteDatabase.update(TABLE_HAZARDS, contentValues, CLOUD_ID + " = ?", new String[] { String.valueOf(myHazard.getCloudID()) });
    }

    public int updateHazardCloudSynced(Hazard myHazard){

        ContentValues contentValues = new ContentValues();
        contentValues.put(CLOUD_SYNCED, myHazard.getCloudSynced());

        // updating row
        return sqLiteDatabase.update(TABLE_HAZARDS, contentValues, CLOUD_ID + " = ?", new String[] { String.valueOf(myHazard.getCloudID()) });
    }

    public int updateMap(Map myMap){

        ContentValues contentValues = new ContentValues();
        contentValues.put(UPDATED_TIME, TimeStamp.getTimeStamp_String());
        contentValues.put(MAP_PROGRESS, myMap.getProgress());
        contentValues.put(MAP_NBROFHAZARDS, myMap.getNbrOfHazards());
        contentValues.put(CLOUD_SYNCED, myMap.getCloudSynced());

        // updating row
        return sqLiteDatabase.update(TABLE_MAP, contentValues, CLOUD_ID + " = ?", new String[] { String.valueOf(myMap.getCloudID()) });
    }

    public int updateMapCloudSynced(Map myMap){

        ContentValues contentValues = new ContentValues();
        contentValues.put(CLOUD_SYNCED, myMap.getCloudSynced());

        // updating row
        return sqLiteDatabase.update(TABLE_MAP, contentValues, CLOUD_ID + " = ?", new String[] { String.valueOf(myMap.getCloudID()) });
    }

    public int updatePathPointsCloudSynced(PathPoint myPathPoint){

        ContentValues contentValues = new ContentValues();
        contentValues.put(CLOUD_SYNCED, myPathPoint.getCloudSynced());

        // updating row
        return sqLiteDatabase.update(TABLE_PATH, contentValues, CLOUD_ID + " = ?", new String[] { String.valueOf(myPathPoint.getCloudID()) });
    }

    public void deleteHazard(Hazard myHazard){
        sqLiteDatabase.delete(TABLE_HAZARDS, CLOUD_ID + " = ?", new String[] { String.valueOf(myHazard.getCloudID()) });
    }

    public void deleteMap(Map myMap){
        sqLiteDatabase.delete(TABLE_HAZARDS, HAZARD_MAP_CLOUD_FK + " = ?", new String[] { String.valueOf(myMap.getCloudID()) });
        sqLiteDatabase.delete(TABLE_PATH, PATH_MAP_CLOUD_FK + " = ?", new String[] { String.valueOf(myMap.getCloudID()) });
        sqLiteDatabase.delete(TABLE_MAP, CLOUD_ID + " = ?", new String[] { String.valueOf(myMap.getCloudID()) });
    }

    public void closeDB() {
        if (sqLiteDatabase != null && sqLiteDatabase.isOpen()) {
            sqLiteDatabase.close();
        }
    }

    public void forceRefresh(){

        DatabaseHelper.refreshInstance();
    }

    public boolean doesDatabaseExist() {
        File dbFile = myContext.getDatabasePath(DB_NAME);
        return dbFile.exists();
    }
}
