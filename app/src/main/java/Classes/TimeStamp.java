package Classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeStamp {

    private int day_;
    private int month_;
    private int year_;
    private int hour_;
    private int minute_;
    private int second_;

    // Constructors
    TimeStamp(int year, int month, int day, int hour, int minute, int second){

        this.year_ = year;
        this.month_ = month;
        this.day_ = day;
        this.hour_ = hour;
        this.minute_ = minute;
        this.second_ = second;
    }

    // Getter
    int getDay(){
        return day_;
    }

    int getMonth(){
        return month_;
    }

    int getYear(){
        return year_;
    }

    int getHour(){
        return hour_;
    }

    int getMinute(){
        return minute_;
    }

    int getSecond() {
        return second_;
    }


    // Custom methods
    static TimeStamp getTimeStamp(){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss", Locale.US);

        String[] timestamp_array_str = fmt.format(date).split("/");

        return new TimeStamp(Integer.parseInt(timestamp_array_str[0]), Integer.parseInt(timestamp_array_str[1]),
                Integer.parseInt(timestamp_array_str[2]), Integer.parseInt(timestamp_array_str[3]),
                Integer.parseInt(timestamp_array_str[4]), Integer.parseInt(timestamp_array_str[5]));
    }

    public static String getTimeStamp_String(){

        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd/HH/mm/ss", Locale.US);
        return fmt.format(date);
    }

    public static String getTimeStamp_String_Clean(){
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.US);

        return fmt.format(date);
    }


    private String getMonthName(TimeStamp myTimeStamp){

        int Month = myTimeStamp.getMonth();

        String monthStr = "";

        if(Month == 1){
            monthStr = "Jan ";
        }else if(Month == 2){
            monthStr = "Feb ";
        }else if(Month == 3){
            monthStr = "Mar ";
        }else if(Month == 4){
            monthStr = "Apr ";
        }else if(Month == 5){
            monthStr = "May ";
        }else if(Month == 6){
            monthStr = "Jun ";
        }else if(Month == 7){
            monthStr = "Jul ";
        }else if(Month == 8){
            monthStr = "Aug ";
        }else if(Month == 9){
            monthStr = "Sep ";
        }else if(Month == 10){
            monthStr = "Oct ";
        }else if(Month == 11){
            monthStr = "Nov ";
        }else if(Month == 12){
            monthStr = "Dec ";
        }

        return monthStr;
    }

}
