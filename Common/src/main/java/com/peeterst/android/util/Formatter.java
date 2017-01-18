package com.peeterst.android.util;

import android.app.Activity;
import android.content.Context;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas
 * Date: 24/03/12
 * Time: 18:10
 * Static util class
 */
public class Formatter {

    public static String formatDate(int year, int month, int day, Context source){

        int base = 1900;
        int offset = year - base;
        Date date = new Date(offset,month,day);
        System.out.println(date.toString());
        Calendar calendar = DateFormat.getDateFormat(source).getCalendar();
        calendar.setTime(date);

        String longForm = DateFormat.getLongDateFormat(source).format(date);
        String weekDay = DateUtils.formatDateTime(source,calendar.getTimeInMillis(),DateUtils.FORMAT_SHOW_WEEKDAY)+", ";


        return weekDay + longForm;
    }

    public static boolean isNumeric(String str)
    {
        try
        {
            double d = Double.parseDouble(str);
        }
        catch(NumberFormatException nfe)
        {
            return false;
        }
        return true;
    }

}
