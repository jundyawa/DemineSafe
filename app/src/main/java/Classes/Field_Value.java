package Classes;

import java.util.ArrayList;

public class Field_Value {

    // Attributes
    private String name_;
    private String type_;
    private String valueSTR_;


    // Constructors
    public Field_Value(String name, String value){
        this.name_ = name;
        this.type_ = "stringValue";
        this.valueSTR_ = value.trim();
    }

    public Field_Value(String name, double value){
        this.name_ = name;
        this.type_ = "doubleValue";
        this.valueSTR_ = Double.toString(value);
    }

    public Field_Value(String name, boolean value){
        this.name_ = name;
        this.type_ = "booleanValue";
        this.valueSTR_ = Boolean.toString(value);
    }

    public Field_Value(String name){
        this.name_ = name;
        this.type_ = "nullValue";
        this.valueSTR_ = null;
    }


    // Get methods
    public String getName(){
        return name_;
    }
    public String getType(){
        return type_;
    }
    public String getValue(){

        return valueSTR_;
    }

    // Custom Methods
    public boolean isStringType(){

        return getType().equals("stringValue");
    }

    public boolean isFieldName(String myFieldName){

        return getName().equals(myFieldName);
    }

    public static String getFieldValue(ArrayList<Field_Value> myFields, String fieldName){

        for(int i = 0 ; i < myFields.size() ; i++){
            if(myFields.get(i).isFieldName(fieldName)){
                return myFields.get(i).getValue();
            }
        }

        return null;
    }

}
