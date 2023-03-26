package com.stech.smartads.utils;

import android.content.Context;

import com.stech.smartads.core.AppData;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.models.AppVersionObj;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.HisDataWrapper;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.models.Schedule;
import com.stech.smartads.models.QuestionObj;
import com.stech.smartads.models.SchedulesObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ParseUtility extends BaseUtil {

    public static List<SchedulesObject> parseSchedule(String response)
    {
        List<SchedulesObject> list = new ArrayList<>();
        response = response.trim();
        if (response.isEmpty())
            return list;

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if (jsonWrapper.getString("status").equalsIgnoreCase("success") && !jsonWrapper.isNull("data")) {

                JSONArray jsonArray = jsonWrapper.getJSONArray("data");
                for (int i = 0; i < jsonArray.length(); i++) {
                    list.add(new SchedulesObject((JSONObject) jsonArray.get(i)));
                }
            }

        } catch (JSONException ex) {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
            //CommonUtil.error(ex);
        }


        return list;
    }

    public static Schedule parseDefaultSchedule(String response)
    {
        Schedule theme = null;
        response = response.trim();
        if (response.isEmpty())
            return theme;
        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if (jsonWrapper.getString("status").equalsIgnoreCase("success")
                    && jsonWrapper.has("default_schedule")
                    && !jsonWrapper.isNull("default_schedule"))
            {
                JSONObject jsonArray = jsonWrapper.getJSONObject("default_schedule");
                theme = new Schedule(jsonArray);
                theme.setDefault(true);
            }

        } catch (JSONException ex) {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
        }

        return theme;
    }


    public static boolean isValidJson(String response) {
        return isSuccess(response);
    }

    public static List<String> parseResourceFiles(String response)
    {
        List<String> list = new ArrayList<>();
        response = response.trim();
        if (response.isEmpty())
            return list;

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if(jsonWrapper.getString("status").equalsIgnoreCase("success"))
            {
                //parse json Array Files and download time
                String downloadFileTime = jsonWrapper.isNull("download_time")? "":jsonWrapper.getString("download_time");

                //cache download time
                if(downloadFileTime != null && !downloadFileTime.isEmpty()){
                    long downloadTime= 0;
                    try {
                        downloadTime = Long.parseLong(downloadFileTime) * 1000;
                    } catch (Exception ex)
                    {
                        downloadTime = Calendar.getInstance().getTimeInMillis();
                    }

                    AppData.getInstance().setDownloadResourceTime(downloadTime);
                } else {
                    AppData.getInstance().setDownloadResourceTime(Calendar.getInstance().getTimeInMillis());
                }

                //parse list files
                JSONArray filesJson = jsonWrapper.getJSONArray("download_files");

                for (int i = 0; i < filesJson.length();i++) {
                    JSONObject json =  filesJson.getJSONObject(i);
                    String url  = json.isNull("url")? "":json.getString("url");
                    if(url != null && !url.trim().isEmpty()){
                        list.add(url);
                    }
                }
            }

        } catch (JSONException ex)
        {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
        }

        return list;
    }

    public static Schedule getDefaultSchedule(Context context) {
        Schedule theme = AppData.getInstance(context).getDefaultSchedule();

        if (theme == null) {
            AppData(context).getOfflineDataAndSwitchToOfflineMode();
            theme = AppData(context).getDefaultSchedule();
            if (theme == null) {
                theme = getDefaultScheduleFromFile(context);
            }
        }
        return theme;
    }

    public static Schedule getDefaultScheduleFromFile(Context context) {
        String layoutFile = "";
        if (AppData.getInstance(context).isAPIMode()) {
            layoutFile = AppConfigs.DEFAULT_SCHEDULE_FILE_API;
        } else {
            layoutFile = AppConfigs.DEFAULT_SCHEDULE_FILE_HOMEPAGE;
        }

        Schedule newSchedule = null;
        //get data from asset
        try {
            String data = StringUtil.getStringContentFromAssetFile(context, layoutFile, AppData.getInstance(context).getCurrentLayoutFrame());
            newSchedule = new Schedule(new JSONObject(data));
            newSchedule.setDefault(true);
        } catch (Exception e) {
            CommonUtil.error(e);
        }
        return newSchedule;
    }

    public static HisDataWrapper parseHISData(String response)
    {
        HisDataWrapper hisDataWrapper=null;
        response = response.trim();
        if (response.isEmpty())
            return hisDataWrapper;

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if(jsonWrapper.getString("status").equalsIgnoreCase("success"))
            {
                hisDataWrapper = new HisDataWrapper(jsonWrapper.getJSONObject("data"));
            }

        }catch (JSONException ex) {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
            hisDataWrapper = new HisDataWrapper();
        }

        return hisDataWrapper;
    }

    public static List<QuestionObj> parseQuestions(String response)
    {
        List<QuestionObj> list = new ArrayList<>();
        response = response.trim();
        if (response.isEmpty())
            return list;

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if(jsonWrapper.getString("status").equalsIgnoreCase("success"))
            {
                QuestionObj questionObj;
                JSONArray jsonArray = jsonWrapper.getJSONArray("data");
                for(int i=0;i<jsonArray.length();i++){
                    questionObj = new QuestionObj((JSONObject)jsonArray.get(i));

                    list.add(questionObj);
                }
            }

        } catch (JSONException ex)
        {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
        }

        return list;
    }

    public static AppVersionObj parseAppVersion(String response)
    {
        AppVersionObj versionObj=null;
        response = response.trim();
        if (response.isEmpty())
            return versionObj;

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if(jsonWrapper.getString("status").equalsIgnoreCase("success"))
            {
                versionObj = new AppVersionObj(jsonWrapper.getJSONObject("data"));
            }

        } catch (JSONException ex) {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
            versionObj = new AppVersionObj();
        }

        return versionObj;
    }

    public static String parseJsonFromObject(Object obj) {
        String json;
        if (obj == null) {
            json = "";
        } else if (obj instanceof String) {
            json = (String) obj;
        } else {
            json = obj.toString();
        }

//        if (!isSuccess(json)) {
//            json = "";
//        }
        return json;
    }

    public static boolean isSuccess(String response)
    {
        if (response == null)
            return false;

        response = response.trim();
        if (response.isEmpty())
            return false;

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            return (jsonWrapper.has("status") && jsonWrapper.getString("status").equalsIgnoreCase("success"))
                    || (jsonWrapper.has("success") && jsonWrapper.getString("success").equalsIgnoreCase("true"));
        } catch (JSONException ex) {
            return false;
        }
    }


    public static String getErrorMessage(String response)
    {
        if (response == null)
            return ""; //"Không có dữ liệu trả về (empty response)";

        response = response.trim();
        if (response.isEmpty())
            return "";

        try {
            JSONObject jsonWrapper = new JSONObject(response);
            if ((jsonWrapper.has("status") && jsonWrapper.getString("status").equalsIgnoreCase("success"))
                    || (jsonWrapper.has("success") && jsonWrapper.getString("success").equalsIgnoreCase("true")))
                return "";
            return jsonWrapper.has("message") ?  jsonWrapper.getString("message") : "";
        } catch (JSONException ex) {
            return "Dữ liệu trả về không đúng định dạng json (invalid json)";
        }
    }

    public static long parseServerTimestamp(String response)
    {
        response = response.trim();
        if (response.isEmpty())
            return Calendar.getInstance().getTimeInMillis();

        long timestamp = 0;
        try {
            JSONObject jsonWrapper = new JSONObject(response);

            if(!jsonWrapper.isNull("current_time")){

                timestamp = jsonWrapper.getLong("current_time")*1000;

                //add more 5 seconds for processing after register.
                timestamp+= 5*1000;

               // CommonUtil.log("parseUtility", "server_time :"+timestamp);
            } else {
                timestamp = Calendar.getInstance().getTimeInMillis();

            }

        }catch (JSONException ex) {
            timestamp = Calendar.getInstance().getTimeInMillis();
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
        }

        return timestamp;

    }

    public static ServerSetting parseServerSetting(String response)
    {
        response = response.trim();
        if (response.isEmpty())
            return new ServerSetting();

        ServerSetting serverSetting = null;
        try {
            JSONObject jsonWrapper = new JSONObject(response);

            if (!jsonWrapper.isNull("settings")) {
                //CommonUtil.log("PARSE_HIS", "HIS SETTING IS READY");
                serverSetting = new ServerSetting(jsonWrapper.getJSONObject("settings"));
            } else {
                //CommonUtil.log("PARSE_HIS", "HIS SETTING IS DEFAULT");
                serverSetting = new ServerSetting();

            }

        } catch (JSONException ex) {
            //CommonUtil.log("PARSE_HIS", "HIS SETTING IS ERROR. GET DEFAULT");
            serverSetting = new ServerSetting();
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
        }


        return serverSetting;

    }

    public static boolean parseNeedRefreshSchedules (String response){
        if (response == null)
            return  false;

        response = response.trim();
        if (response.isEmpty())
            return false;

        try {
            JSONObject jsonWrapper = new JSONObject(response);

            if(!jsonWrapper.isNull("need_refresh_schedules")){

                return jsonWrapper.getBoolean("need_refresh_schedules");

            } else {
                return  false;

            }

        }catch (JSONException ex) {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
            return  false;
        }
    }

    public static boolean isNullOrEmpty(String value)  {
        if (value == null)
            return true;
        return value.trim().isEmpty();
    }

    public static String parseJsonValue (String response, String key){
        response = response.trim();
        if (response.isEmpty())
            return "";

        try {
            JSONObject jsonWrapper = new JSONObject(response);

            if(!jsonWrapper.isNull(key)){
                return jsonWrapper.getString(key);
            } else {
                return "";

            }

        } catch (JSONException ex) {
            CommonUtil.error(new JSONException(ex.getMessage() + " Json: " + response));
            return "";
        }
    }

    public static String parseLicense (String response) {
        return parseJsonValue(response, "license");
    }

    public static String parseDeviceName (String response){
        return parseJsonValue(response, "name");
    }

    public static String parseDeviceDescription (String response){
        return parseJsonValue(response, "description");
    }


    public static boolean isJsonDifferent(String json1, String json2, String comparedField) {
        if (json1 == null || json2 == null)
            return true;
        json1 = json1.trim();
        if (json1.isEmpty())
            return true;

        json2 = json2.trim();
        if (json2.isEmpty())
            return true;

        try {
            JSONObject jsonWrapper1 = new JSONObject(json1);
            JSONObject jsonWrapper2 = new JSONObject(json2);
            String jsonData1 = jsonWrapper1.getString(comparedField);
            String jsonData2 = jsonWrapper2.getString(comparedField);
            if (jsonData1.equals(jsonData2))
                return false;
            return true;
        } catch (JSONException ex) {
            return true;
        }

    }

    public static boolean isJsonDifferent(String json1, String json2) {
        return isJsonDifferent(json1, json2, "data");
    }

    public static boolean isSameJsonData(String json1, String json2) {
        return !isJsonDifferent(json1, json2, "data");
    }

    public static JSONArray getDataJsonArray(List<DataContentObj> list) {
        JSONArray arr = new JSONArray();
        for (int i = 0; i < list.size(); i ++) {
            JSONObject obj = list.get(i).getJSONObject();
            if (obj != null)
                arr.put(list.get(i).getJSONObject());
        }
        return arr;
    }

    public static JSONObject getDataJsonObject(List<DataContentObj> list) {
        try {
            JSONObject child = new JSONObject();
            child.put("status", "SUCCESS");
            child.put("data", getDataJsonArray(list));
            return child;
        } catch (JSONException e) {
            return new JSONObject();
        }
    }
}
