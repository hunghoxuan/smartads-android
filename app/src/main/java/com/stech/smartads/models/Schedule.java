package com.stech.smartads.models;

import com.stech.smartads.config.Constants;
import com.stech.smartads.core.AppData;
import com.stech.smartads.utils.DateTimeUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mac on 10/27/17.
 */

public class Schedule {
    private int id = 0;
    private String name = "" ;
    private String background = "#ffffff";
    private String fontColor = "#000000";
    private int duration = 0;
    private String startTime = "";
    private long finishTime = 0;
    private String downloadFileTime = "";
    private boolean isAds = false;
    private boolean isDefault = false;
    private String json;
    private JSONObject jsonObject;
    private JSONArray dataJson;

    private List<LayoutFrameObj> arrFrameLayout;
    private List<String> arrFileUrls;


    public Schedule(){
        super();
    }

    public String AsString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[")
                .append(DateTimeUtil.getScheduleTimeDisplay(this)).append("] ").append("#").append(id);
        for (int i = 0; i < arrFrameLayout.size(); i++) {
            builder.append(" | Frame ").append(arrFrameLayout.get(i).getName() + " : " + arrFrameLayout.get(i).getDataContentType())
                    .append(" (").append(arrFrameLayout.get(i).getData().size()).append(")");
        }
        return  builder.toString();
    }

    public boolean equals(Schedule schedule) {
        if (schedule == null)
            return false;
        return getJson().equalsIgnoreCase(schedule.getJson());
    }

    public String getJson() {
        return this.json;
    }

    public String getDataJson() {
        return this.dataJson.toString();
    }

    public JSONObject getJsonObject() {
        return this.jsonObject;
    }

    public JSONArray getDataJsonArray() {
        return this.dataJson;
    }

    public Schedule(JSONObject jsonObj)
    {
        try {
            this.jsonObject = jsonObj;
            this.json = jsonObj.getString("data");
            //parse json Array
            this.dataJson = jsonObj.getJSONArray("data");


            this.id = jsonObj.isNull("id")? 0:jsonObj.getInt("id");
            this.name = jsonObj.isNull("name")? "":jsonObj.getString("name");
            this.background = jsonObj.isNull("background")? background:jsonObj.getString("background");
            this.fontColor = jsonObj.isNull("fontColor")? fontColor:jsonObj.getString("fontColor");
//            this.duration = jsonObj.isNull("duration")? 0 : jsonObj.getInt("duration");
            this.duration = jsonObj.isNull("duration")? 0 : (int)(jsonObj.getDouble("duration") * 60);
            this.isDefault = !jsonObj.isNull("is_default") &&  jsonObj.getInt("duration") == 1;

            //process start time
            if(!jsonObj.isNull("start_time")){
                try {
                    long timestamp = jsonObj.getLong("start_time");
                    this.startTime = String.valueOf(timestamp);
                } catch (Exception ex){
                    this.startTime = jsonObj.getString("start_time");
                }
            } else {
                this.startTime = "";
            }

            //Hung: if no data -> could be blank
            if (this.dataJson.length() == 0) {
                this.arrFrameLayout = AppData.getInstance().getDefaultSchedule().getFrameLayouts();
            } else {
                this.arrFrameLayout = new ArrayList<>();
                for (int i = 0; i < this.dataJson.length(); i++) {
                    this.arrFrameLayout.add(new LayoutFrameObj((JSONObject) this.dataJson.get(i)));
                }
            }

        } catch (Exception e) {
            this.id = 0;
            this.name = "";
            this.arrFrameLayout = new ArrayList<>();
        }
    }

    public Schedule(JSONObject jsonObj, String date)
    {
        try {
            this.jsonObject = jsonObj;
            this.json = jsonObj.isNull("data") ? "" : jsonObj.getString("data");
            //parse json Array
            this.dataJson = jsonObj.getJSONArray("data");

            this.id = jsonObj.isNull("id") ? 0 : jsonObj.getInt("id");
            this.name = jsonObj.isNull("name") ? "" : jsonObj.getString("name");
            this.background = jsonObj.isNull("background") ? background:jsonObj.getString("background");
            this.fontColor = jsonObj.isNull("fontColor") ? fontColor:jsonObj.getString("fontColor");
            this.duration = jsonObj.isNull("duration") ? 0 : jsonObj.getInt("duration");

            //process start time
            if (!jsonObj.isNull("start_time")) {

                String time = jsonObj.getString("start_time");
                startTime = date + " "+time;

            } else {
                this.startTime = "";
            }

            //parse json Array
            if (dataJson.length() == 0) {
                this.arrFrameLayout = AppData.getInstance().getDefaultSchedule().getFrameLayouts();
            } else {
                this.arrFrameLayout = new ArrayList<>();
                for (int i = 0; i < dataJson.length(); i++) {
                    arrFrameLayout.add(new LayoutFrameObj((JSONObject) dataJson.get(i)));
                }
            }

        } catch (Exception e) {
            this.id = 0;
            this.name = "";
            this.arrFrameLayout = new ArrayList<>();
        }
    }

    public boolean isAds() {
        return isAds;
    }

    public void setAds(boolean ads) {
        isAds = ads;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getBackground() {
        return background;
    }

    public void setBackground(String background) {
        this.background = background;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public List<LayoutFrameObj> getFrameLayouts() {
        return arrFrameLayout;
    }

    public void setArrFrameLayout(List<LayoutFrameObj> arrFrameLayout) {
        this.arrFrameLayout = arrFrameLayout;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getStartTime() {

        long startTime = 0;
        try {
            startTime = Long.getLong(this.startTime)*1000;
        } catch (Exception e){
            try {
                String localDate = DateTimeUtil.showCurrentDate();
                String time = localDate + " " + this.startTime.substring(this.startTime.indexOf(" ") + 1, this.startTime.length());

                Date date = DateTimeUtil.convertStringToDate(time, SchedulesObject.FORMAT_SCHEDULE_DATE_TIME);
                startTime = DateTimeUtil.convertDateToTimeStamp(date);
            } catch (Exception ex){
                startTime = 0;
            }
        }

        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return getStartTime() + this.duration*60*1000;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public String getDownloadFileTime() {
        return downloadFileTime;
    }

    public void setDownloadFileTime(String downloadFileTime) {
        this.downloadFileTime = downloadFileTime;
    }

    public List<String> getArrFileUrls() {
        return arrFileUrls;
    }

    public void setArrFileUrls(List<String> arrFileUrls) {
        this.arrFileUrls = arrFileUrls;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isExistedHIS(){

        if(arrFrameLayout == null || arrFrameLayout.isEmpty()) return false;

        for (LayoutFrameObj frameObj:arrFrameLayout) {
            if (frameObj.getDataContentType().equals(Constants.TYPE_HIS_VIMES)) {
                    return true;
            }

        }

        return false;
    }
}
