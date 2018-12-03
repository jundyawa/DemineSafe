package Methods;

import android.content.Context;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input_Methods {

    // --- Attributes ---
    private Context myContext_;


    public Input_Methods(Context myContext){
        this.myContext_ = myContext;
    }

    public boolean isValid(TextInputLayout[] myInputLayout, TextInputEditText[] myEditTexts){

        if(myInputLayout == null || myEditTexts == null){
            return false;
        }

        if(myInputLayout.length != myEditTexts.length){
            return false;
        }

        for(int i = 0 ; i < myEditTexts.length ; i++){

            // Check if empty
            if(isTextInputEditTextEmpty(myInputLayout[i], myEditTexts[i])){
                return false;
            }

            // Check if input type is respected
            if(!isInputTypeRespected(myEditTexts[i])){
                return false;
            }
        }

        return true;
    }

    public boolean isValid(TextInputLayout myInputLayout, TextInputEditText myEditTexts){

        if(myInputLayout == null || myEditTexts == null){
            return false;
        }

        // Check if empty
        if(isTextInputEditTextEmpty(myInputLayout, myEditTexts)){
            return false;
        }

        // Check if input type is respected
        if(!isInputTypeRespected(myEditTexts)){
            return false;
        }

        return true;
    }

    public boolean isAddInfoValid(EditText myEditText){

        if(myEditText == null){
            return false;
        }

        // Check if empty
        if(isEditTextEmpty(myEditText)){
            return false;
        }

        // Check if input type is respected
        String myInput_str = myEditText.getText().toString().trim();
        myInput_str = myInput_str.replace("'", "");
        myEditText.setText(myInput_str);

        return true;
    }

    private boolean isEditTextEmpty(EditText myEditText){

        if(myEditText.getText().toString().trim().isEmpty()){
            toastMessage("Please provide some additional information");
            return true;
        }
        return false;
    }

    private boolean isTextInputEditTextEmpty(TextInputLayout myInputLayout, TextInputEditText myEditText){

        if(myEditText.getText().toString().trim().isEmpty()){
            toastMessage("Enter " + myInputLayout.getHint());
            return true;
        }
        return false;
    }

    private boolean isInputTypeRespected(TextInputEditText myEditText){

        String myInput_str = myEditText.getText().toString().trim();

        if(myEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS){

            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                    "A-Z]{2,7}$";
            Pattern pat = Pattern.compile(emailRegex);

            if(pat.matcher(myInput_str).matches()){
                return true;
            }else{
                toastMessage("Wrong email format");
                return false;
            }

        }else if(myEditText.getInputType() == (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)){

            double myInput = Double.parseDouble(myInput_str);

            if(myInput <= 0){

                return false;
            }

            myEditText.setText(String.valueOf(Math.round(myInput*100.0)/100.0));

            return true;

        }else if(myEditText.getInputType() == InputType.TYPE_CLASS_TEXT){

            // Remove the ' char
            myInput_str = myInput_str.replace("'", "");
            myEditText.setText(myInput_str);

            return true;

        }else if(myEditText.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS)){

            // Remove the ' char
            myInput_str = myInput_str.replace("'", "");

            // Capitalize every word
            StringBuffer sb = new StringBuffer(myInput_str);
            for (int i = 0; i < sb.length(); i++){
                if (i == 0 || sb.charAt(i - 1) == ' ') {
                    sb.setCharAt(i, Character.toUpperCase(sb.charAt(i)));
                }
            }

            myEditText.setText(sb.toString());

            return true;

        }else if(myEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_PERSON_NAME){

            if(!myInput_str.matches("[A-Za-z0-9]*")){
                toastMessage("Username can only contain alphanumerical characters");
                return false;
            }else{

                return true;
            }

        }else if(myEditText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD){

            if(myInput_str.length() < 6){
                toastMessage("Password must be at least 6 characters long");
                return false;
            }else if(!myInput_str.matches("[A-Za-z0-9]*")){
                toastMessage("Password can only contain alphanumerical characters");
                return false;
            }else{
                return true;
            }

        }else if(myEditText.getInputType() == InputType.TYPE_CLASS_NUMBER || myEditText.getInputType() ==
                (InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL)) {

            double data;
            try {
                data = Double.parseDouble(myInput_str);
            }catch(NumberFormatException nfe){
                toastMessage("Wrong Numerical Format");
                return false;
            }
        }

        return true;
    }

    // Show message to user through a short pop up
    private void toastMessage(String message){
        Toast.makeText(myContext_,message,Toast.LENGTH_SHORT).show();
    }
}
