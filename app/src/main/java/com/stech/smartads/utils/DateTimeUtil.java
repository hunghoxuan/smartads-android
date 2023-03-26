package com.stech.smartads.utils;

import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.models.Schedule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by NaPro on 06/22/2016.
 */
public class DateTimeUtil extends BaseUtil {

    private static final String TAG = DateTimeUtil.class.getSimpleName();

    public static final String SECOND = "second";
    public static final String MILLISECOND = "millisecond";

    /**
     * @param timeStamp    As millisecond
     * @param outputFormat Expected output format
     * @return String of date
     */
    public static String convertTimeStampToDate(String timeStamp, String outputFormat) {
        SimpleDateFormat formater = new SimpleDateFormat(outputFormat, Locale.getDefault());
        try {
            Date date = new Date(Long.parseLong(timeStamp));
            return formater.format(date);
        } catch (NumberFormatException ex) {
            return formater.format(new Date());
        }
    }

    public static String convertTimeStampToDate(String timeStamp) {
        return convertTimeStampToDate(timeStamp, AppConfigs.FORMAT_SCHEDULE_TIME);
    }


    /**
     * @param timeStamp    As millisecond
     * @param outputFormat Expected output format
     * @return String of date
     */
    public static String convertTimeStampToDate(long timeStamp, String outputFormat) {
        SimpleDateFormat formater = new SimpleDateFormat(outputFormat, Locale.getDefault());
        try {
            Date date = new Date(timeStamp);
            return formater.format(date);
        } catch (NumberFormatException ex) {
            return formater.format(new Date());
        }
    }

    public static String convertTimeStampToDate(long timeStamp) {
        return convertTimeStampToDate(timeStamp, AppConfigs.FORMAT_SCHEDULE_TIME);
    }

    /**
     * @param timeStamp    as millisecond
     * @param outputFormat
     * @param timeZone
     * @return
     */
    public static String convertTimeStampToDateByTimezone(long timeStamp, String outputFormat, String timeZone) {
        Date date = new Date(timeStamp);

        SimpleDateFormat destFormat = new SimpleDateFormat(outputFormat, Locale.getDefault());
        if (timeZone != null && !timeZone.equals("")) {
            TimeZone tz = TimeZone.getTimeZone(timeZone);
            destFormat.setTimeZone(tz);
        }

        return destFormat.format(date);
    }

    /**
     * @param date
     * @return TimeStamp of the given date as millisecond
     */
    public static long convertDateToTimeStamp(Date date) {
        long result;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        try {
            result = cal.getTimeInMillis();
        } catch (Exception ex) {
            result = 0;
        }
        return result;
    }

    /**
     * @param curTimeStamp  As second
     * @param specTimeStamp As second
     * @return Date difference
     */
    public static int getDateDiff(long curTimeStamp, long specTimeStamp) {
        try {
            long diff = specTimeStamp - curTimeStamp;
            long day = diff / 24 / 60 / 60;

            String strDay = day + "";
            if (strDay.contains(".")) {
                strDay = strDay.substring(0, strDay.indexOf("."));
            }

            return Integer.parseInt(strDay);
        } catch (Exception ex) {
            CommonUtil.error(ex);
            return 0;
        }
    }

    /**
     * @param strDate
     * @param inputFormat  Original format
     * @param outputFormat Expected output format
     * @return New date string with outputFormat
     */
    public static String changeDateFormat(String strDate, String inputFormat, String outputFormat) {
        SimpleDateFormat dateFormaterInput, dateFormaterOutput;
        dateFormaterInput = new SimpleDateFormat(inputFormat, Locale.getDefault());
        dateFormaterOutput = new SimpleDateFormat(outputFormat, Locale.getDefault());
        Date dob;
        try {
            dob = dateFormaterInput.parse(strDate);
            return dateFormaterOutput.format(dob);
        } catch (ParseException e) {
            return strDate;
        }
    }

    /**
     * @param date
     * @param outputFormat Expected output format
     * @return String from date
     */
    public static String convertDateToString(Date date, String outputFormat) {
        SimpleDateFormat formater = new SimpleDateFormat(outputFormat, Locale.getDefault());
        return formater.format(date);
    }

    /**
     * @param strDate
     * @param format  Format of strDate
     * @return Date from string
     */
    public static Date convertStringToDate(String strDate, String format) {
        DateFormat formatter = new SimpleDateFormat(format, Locale.getDefault());
        try {
            return formatter.parse(strDate);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param timeStamp As millisecond
     * @return timestamp of the date(00:00:00) from timeStamp
     */
    public static long convertTimeStampToStartOfDate(long timeStamp, String timeZone, String unit) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal.setTimeInMillis(timeStamp);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (unit.equalsIgnoreCase(SECOND)) {
            String result = String.valueOf(cal.getTimeInMillis());
            return Long.parseLong(result.substring(0, result.length() - 3));
        } else {
            return cal.getTimeInMillis();
        }
    }

    /**
     * @param timeStamp As millisecond
     * @return timestamp of the date(23:59:59) from timeStamp
     */
    public static long convertTimeStampToEndOfDate(long timeStamp, String timeZone, String unit) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal.setTimeInMillis(timeStamp);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        if (unit.equalsIgnoreCase(SECOND)) {
            String result = String.valueOf(cal.getTimeInMillis());
            return Long.parseLong(result.substring(0, result.length() - 3));
        } else {
            return cal.getTimeInMillis();
        }
    }

    /**
     * @param timeStamp as millisecond
     * @param date      how many days you want to plus/minus
     * @param hour
     * @param minute
     * @param second
     * @param timeZone
     * @param unit      {@link #SECOND} or {@link #MILLISECOND}
     * @return timestamp as second if @unit is second else return millisecond
     */
    public static long convertTimeStampToNewOne(long timeStamp, int date, int hour, int minute, int second, String timeZone, String unit) {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone(timeZone));
        cal.setTimeInMillis(timeStamp);
        cal.add(Calendar.DATE, date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        if (unit.equalsIgnoreCase(SECOND)) {
            String result = String.valueOf(cal.getTimeInMillis());
            return Long.parseLong(result.substring(0, result.length() - 3));
        } else {
            return cal.getTimeInMillis();
        }
    }

    /**
     * @return Current time in timestamp as millisecond
     * @param unit      {@link #SECOND} or {@link #MILLISECOND}
     */
    public static long getCurrentTime(String unit) {
        if (unit.equalsIgnoreCase(SECOND)) {
            String result = String.valueOf(Calendar.getInstance().getTimeInMillis());
            return Long.parseLong(result.substring(0, result.length() - 3));
        } else {
            return Calendar.getInstance().getTimeInMillis();
        }
    }

    /**
     * @return Current time in timestamp as millisecond
     */
    public static long getCurrentTime(Calendar calendar, String unit) {
        if (unit.equalsIgnoreCase(SECOND)) {
            String result = String.valueOf(calendar.getTimeInMillis());
            return Long.parseLong(result.substring(0, result.length() - 3));
        } else {
            return calendar.getTimeInMillis();
        }
    }

    public static String showCurrentTime() {
        return DateTimeUtil.convertDateToString(Calendar.getInstance().getTime(), AppConfigs.FORMAT_SCHEDULE_TIME);
    }

    public static String showCurrentDateTime() {
        return DateTimeUtil.convertDateToString(Calendar.getInstance().getTime(), AppConfigs.FORMAT_SCHEDULE_DATE_TIME);
    }

    public static String showCurrentDate() {
        return DateTimeUtil.convertDateToString(Calendar.getInstance().getTime(), AppConfigs.FORMAT_SCHEDULE_DATE);
    }

    /**
     * This method is just for temporary, need to be improved
     *
     * @param curTimeStamp  As second
     * @param specTimeStamp As second
     * @return Array contains  rest of day, hour, minute, and second
     */
    public static ArrayList<String> countDown(long curTimeStamp, long specTimeStamp) {
        ArrayList<String> arr = new ArrayList<>();

        long diff = specTimeStamp - curTimeStamp;

        long sec = diff % 60;
        long min = (diff / 60) % 60;
        long hour = (diff / 60 / 60) % 24;
        long day = diff / 24 / 60 / 60;

        String strSec = sec + "";
        if (sec < 10) {
            strSec = "0" + sec;
        }
        if (strSec.contains(".")) {
            strSec = strSec.substring(0, strSec.indexOf("."));
        }

        String strMin = min + "";
        if (min < 10) {
            strMin = "0" + min;
        }
        if (strMin.contains(".")) {
            strMin = strMin.substring(0, strMin.indexOf("."));
        }

        String strHour = hour + "";
        if (hour < 10) {
            strHour = "0" + hour;
        }
        if (strHour.contains(".")) {
            strHour = strHour.substring(0, strHour.indexOf("."));
        }

        String strDay = day + "";
        if (day < 10) {
            strDay = "0" + day;
        }
        if (strDay.contains(".")) {
            strDay = strDay.substring(0, strDay.indexOf("."));
        }

        arr.add(strDay);
        arr.add(strHour);
        arr.add(strMin);
        arr.add(strSec);

        return arr;
    }

    public static String getScheduleTimeDisplay(Schedule schedule) {
        if (schedule != null) {
            String start = DateTimeUtil.convertTimeStampToDate(schedule.getStartTime());
            String end = DateTimeUtil.convertTimeStampToDate(schedule.getFinishTime());
            if (end.equalsIgnoreCase("00:00"))
                end = "24:00";
            return start + " - " + end;
        }

        return "00:00-24:00";
    }
}
