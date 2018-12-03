package Methods;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import Classes.Field_Value;

public class JSON_Methods {

    public static ArrayList<Field_Value> convertFirestoreJSON(JsonReader myReader) throws IOException {

        // Create Empty Array
        ArrayList<Field_Value> myFieldsValue = new ArrayList<>();

        // Field Name
        String nextName = "";
        String myFieldName = "";

        // Json Token
        JsonToken nextType;

        while(true){

            nextType = myReader.peek();

            if(nextType == JsonToken.BEGIN_OBJECT) {        // {
                myReader.beginObject();

            }else if(nextType == JsonToken.BEGIN_ARRAY){    // [
                myReader.beginArray();

            }else if(nextType == JsonToken.END_ARRAY){      // ]
                myReader.endArray();

            }else if(nextType == JsonToken.END_OBJECT){     // }
                myReader.endObject();

            }else if(nextType == JsonToken.END_DOCUMENT){   // nothing
                break;

            }else if(nextType == JsonToken.NAME){           // Field Name

                nextName = myReader.nextName();

                if(!isFirestoreValueType(nextName)){
                    myFieldName = nextName;
                }

            }else if(nextType == JsonToken.NUMBER){         // int, double, long
                myFieldsValue.add(new Field_Value(myFieldName,myReader.nextDouble()));

            }else if(nextType == JsonToken.BOOLEAN){        // boolean
                myFieldsValue.add(new Field_Value(myFieldName,myReader.nextBoolean()));

            }else if(nextType == JsonToken.STRING){         // string
                myFieldsValue.add(new Field_Value(myFieldName,myReader.nextString()));

            }else if(nextType == JsonToken.NULL){           // null
                myFieldsValue.add(new Field_Value(myFieldName));

            }else{
                myReader.skipValue();
            }
        }

        // We close the JsonReader
        myReader.close();

        return myFieldsValue;
    }

    private static boolean isFirestoreValueType(String nextName){

        if(nextName.equals("stringValue") || nextName.equals("doubleValue") || nextName.equals("booleanValue")
                || nextName.equals("doubleValue") || nextName.equals("integerValue") || nextName.equals("timestampValue") || nextName.equals("documents") || nextName.equals("fields")){
            return true;
        }else{
            return false;
        }
    }

    public static JSONObject buildJSON(ArrayList<Field_Value> myPayload) throws JSONException {

        // If empty
        if(myPayload.size() == 0){
            return null;
        }

        // Start creating the JSON string
        StringBuilder myJSONstring = new StringBuilder("{");

        Field_Value myField;

        for(int i = 0 ; i < myPayload.size() ; i++){

            myField = myPayload.get(i);

            myJSONstring.append("\"").append(myField.getName()).append("\":");

            if(myField.isStringType()){
                myJSONstring.append("\"").append(myField.getValue()).append("\"");
            }else{
                myJSONstring.append(myField.getValue());
            }

            if(i < myPayload.size() - 1){
                myJSONstring.append(",");
            }
        }

        myJSONstring.append("}");

        return new JSONObject(myJSONstring.toString());

    }

    public static JSONObject buildFirestoreJSON(ArrayList<Field_Value> myPayload) throws JSONException {

        // If empty
        if(myPayload.size() == 0){
            return null;
        }

        Field_Value myNextFieldValue;

        // Start creating the JSON string
        StringBuilder myJSONstring = new StringBuilder("{\"fields\":{");

        for(int i = 0 ; i < myPayload.size() ; i++){

            myNextFieldValue = myPayload.get(i);

            myJSONstring.append("\"").append(myNextFieldValue.getName()).append("\":{\"").append(myNextFieldValue.getType()).append("\":");

            if(myNextFieldValue.isStringType()){
                myJSONstring.append("\"").append(myNextFieldValue.getValue()).append("\"");
            }else{
                myJSONstring.append(myNextFieldValue.getValue());
            }

            myJSONstring.append("}");

            if(i < myPayload.size() - 1){
                myJSONstring.append(",");
            }
        }

        myJSONstring.append("}}");

        return new JSONObject(myJSONstring.toString());
    }
}
