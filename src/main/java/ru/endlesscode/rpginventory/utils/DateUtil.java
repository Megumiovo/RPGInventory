package ru.endlesscode.rpginventory.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {
    private final static SimpleDateFormat YMD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static Date getNowDate(){
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        return date;
    }
    public static String getDateToStr(Date date){
        return YMD_DATE_FORMAT.format(date);
    }

    public static Date getStrToDate(String date){
        try {
            return YMD_DATE_FORMAT.parse(date);
        }
        catch (ParseException e){
            e.printStackTrace();
            return null;
        }
    }

    public static String getDate() {
        return getDateToStr(getNowDate());
    }
}