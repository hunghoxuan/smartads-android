package com.stech.smartads.models;

import android.content.Context;

import com.stech.smartads.config.AppConfigs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class SchedulesObject {
    public final static String FORMAT_SCHEDULE_DATE = AppConfigs.FORMAT_SCHEDULE_DATE;
    public final static String FORMAT_SCHEDULE_TIME = AppConfigs.FORMAT_SCHEDULE_TIME;
    public final static String FORMAT_SCHEDULE_DATE_TIME = AppConfigs.FORMAT_SCHEDULE_DATE_TIME;

    private  int id;
    private String deviceID;
    private String date; //  yyyy/MM/dd
    private int duration = 0; // duration of a schedule - minutes
    private JSONObject jsonObject;
    private ArrayList<Schedule> arrScheduleItems;
    private String device_name;
    private String device_description;

    public JSONObject getJsonObject() {
        return jsonObject;
    }


    public SchedulesObject(JSONObject jsonObj) {
        try {
            this.jsonObject = jsonObj;
            this.id = jsonObj.isNull("id")? 0:jsonObj.getInt("id");
            this.deviceID = jsonObj.isNull("device_id")? "":jsonObj.getString("device_id");
            this.date = jsonObj.isNull("date")? "":jsonObj.getString("date");

            this.device_name = jsonObj.isNull("name") ? "" :jsonObj.getString("name");
            this.device_description = jsonObj.isNull("description") ? "" :jsonObj.getString("description");

            //parse scheduleItem list
            arrScheduleItems = new ArrayList<>();

            if(!jsonObj.isNull("schedules")) {
                JSONArray arrJsonScheduleItems = jsonObj.getJSONArray("schedules");
                Schedule scheduleItem = null;
                JSONObject jsonItem = null;

                for (int i = 0; i < arrJsonScheduleItems.length(); i ++) {
                    jsonItem = arrJsonScheduleItems.getJSONObject(i);
                    scheduleItem = new Schedule(jsonItem, date);
                    arrScheduleItems.add(scheduleItem);
                }
            }

        } catch (Exception e) {
            this.id = 0;
            this.deviceID = "";
            this.date = "";
            this.duration = 0;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public ArrayList<Schedule> getArrScheduleItems() {
        return arrScheduleItems;
    }

    public void setArrScheduleItems(ArrayList<Schedule> arrScheduleItems) {
        this.arrScheduleItems = arrScheduleItems;
    }

    public boolean isTodaySchedules(Context context){
        return true;
        /*Calendar calendar = ((MainApplication)context.getApplicationContext()).getCurrentCalendar();
        String strToday = DateTimeUtil.convertDateToString(calendar.getTime(),FORMAT_SCHEDULE_DATE);

        CommonUtil.log("SchedulesObjectect", "schedule date :"+this.date);
        CommonUtil.log("SchedulesObjectect", "today :"+strToday);

        return (this.date.equalsIgnoreCase("") || strToday.equals(this.date));*/

    }
}
