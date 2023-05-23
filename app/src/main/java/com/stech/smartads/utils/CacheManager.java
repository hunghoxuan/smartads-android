package com.stech.smartads.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import com.stech.smartads.components.DownloadService;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.core.AppData;
import com.stech.smartads.models.AppVersionObj;
import com.stech.smartads.models.Schedule;
import com.stech.smartads.models.SchedulesObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CacheManager extends BaseUtil {
    protected String TAG = "CacheManager";

    private static final String SMART_CACHE_PREFERENCES = "SMART_CACHE_PREFERENCES";


    public static String getCacheFolder(){
        return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + AppConfigs.APP_NAME;
    }

    public static void cacheSchedulesJson(Context context, String json){
        CommonUtil.error("CacheManager", "cacheSchedulesJson: " + json);

        storeCache(context, "SCHEDULE_JSON", json);
        storeCache(context, "LAST_UPDATE_SCHEDULES", DateTimeUtil.showCurrentDateTime());

//        CommonUtil.message(context,   "Parse Json done !!" + getCacheSchedulesJson(context));
//        CommonUtil.message(context,   getCacheSchedulesJson(context).trim().toLowerCase().equals(json.trim().toLowerCase()) ? "TRUE" : "FALSE");
    }

    public static void clearSchedulesJson(Context context){
        CommonUtil.error("CacheManager", "clearSchedulesJson");
        storeCache(context, "SCHEDULE_JSON", "");
        storeCache(context, "LAST_UPDATE_SCHEDULES", DateTimeUtil.showCurrentDateTime());
    }


    public static String getCacheSchedulesJson(Context context) {
        CommonUtil.error("CacheManager", "Get Cached GetSchedules");
        String json = getCache(context, "SCHEDULE_JSON");
        if (json == null)
            json = "";
        CommonUtil.error("CacheManager", "getCacheSchedulesJson: " + json);

        return json;
    }

    public static ArrayList<Schedule> getCacheSchedules(Context context) {
        String json = getCacheSchedulesJson(context);
        ArrayList<SchedulesObject> scheduleObjs = (ArrayList<SchedulesObject>) ParseUtility.parseSchedule(json);
        ArrayList<Schedule> schedules = new ArrayList<Schedule>();
        if (scheduleObjs.size() > 0) {
            SchedulesObject scheduleObj;
            for (int i = 0; i < scheduleObjs.size(); i++) {
                scheduleObj = scheduleObjs.get(i);
                schedules = scheduleObj.getArrScheduleItems();
                return schedules;
            }
        }
        return schedules;
    }

    public static String getLastUpdateSchedulesTimeDisplay(Context context) {
        ArrayList<Schedule> arrSchedules;
        arrSchedules = getCacheSchedules(context);
        if (arrSchedules == null || arrSchedules.size() == 0) {
            return "";
        }
        Schedule schedule;
        String result = "";
        for (int i = 0; i < arrSchedules.size(); i++) {
            schedule = arrSchedules.get(i);
            result += DateTimeUtil.convertTimeStampToDate(schedule.getStartTime(), AppConfigs.FORMAT_SCHEDULE_TIME) + " - ";
        }
        result += " 24:00";
        return result;
    }

    public static List<File> getCacheListFile() {
        File cacheFolder = new File(getCacheFolder());

        if (cacheFolder.exists() && cacheFolder.isDirectory() && cacheFolder.listFiles() != null) {
            return Arrays.asList(cacheFolder.listFiles());
        } else {
            return null;
        }
    }

    public static boolean isAbleDownloadFile(Context context, String url){

        if(url == null || url.isEmpty()) return false;

        String localUrl = getCacheFileUrl(context,url);
        if(localUrl.isEmpty()){
            return true;
        } else { //check full file is downloaded

           long totalFileSize = 0;

            try {
                File file = new File(localUrl);

                if(!file.exists()) return true; //if local file is not existed

                //if file is existed
                URL urlFile = new URL(url);

                HttpURLConnection connection = (HttpURLConnection) urlFile.openConnection();
                connection.setRequestProperty("Accept-Encoding", "identity");
                totalFileSize = connection.getContentLength();

                return file.length() < totalFileSize;

            } catch (Exception e) {
                CommonUtil.error(e);
                return true;
            }

        }
    }

    public static void downloadFile(Context context, String url, int requestTime){
        if(!DownloadService.isDownloading && url != null && !url.isEmpty())
            new DownloadService(context, getCacheFolder(),requestTime).execute(url);
    }

    public static void downloadListFile(Context context,List<String> urls){

        for (String url:urls) {
            if(!url.isEmpty()) {
                String localUrl = CacheManager.getCacheFileUrl(context, url);
                if (localUrl.isEmpty()) {
                    new DownloadService(context, getCacheFolder(),1).execute(url);
                } else {
                    File file = new File(localUrl);
                    if (!file.exists()) {
                        new DownloadService(context, getCacheFolder(),1).execute(url);
                    } else {
                        CommonUtil.log("CacheManager", "Download success (file is existed): " + localUrl);
                    }
                }
            }
        }

    }

    public static void clearCacheFiles(Context context){
        //Delete all files in cache folder
        File f = new File(getCacheFolder());
        deleteRecursive(f);

        //Delete data in SharedPreference
        removeAllCacheInfo(context);
    }

    public static void deleteCacheFile(Context context, String key){

        String url = getCacheFileUrl(context,key);
        //Delete all files in cache folder
        File f = new File(url);
        deleteRecursive(f);
        CommonUtil.log("CacheManager","Deleted Cached file: " + url);

    }

    //Delete all file in a folder
   private static void deleteRecursive(File fileOrDirectory) {

        if (fileOrDirectory.isDirectory()) {
            for (File child : fileOrDirectory.listFiles()) {
                deleteRecursive(child);
            }
        }

        fileOrDirectory.deleteOnExit();
    }

    //=========== cache doctor name & room title
    public static void cacheDoctorName(Context context,String doctorName){
        AppData.getInstance(context).putStringValue("HIS_DOCTOR_NAME",doctorName);
    }

    public static String getCacheDoctorName(Context context){
        return AppData.getInstance(context).getStringValue("HIS_DOCTOR_NAME");
    }

    public static void cacheRoomTitle(Context context,String roomTitle){
        AppData.getInstance(context).putStringValue("HIS_ROOM_TITLE",roomTitle);
    }

    public static String getCacheRoomTitle(Context context){
        return AppData.getInstance(context).getStringValue("HIS_ROOM_TITLE");
    }

    //========== Using SharedPreference ============================
    // Save cache info
    public static void storeCacheFile(Context context,String key,String value){
        SharedPreferences pref = context.getSharedPreferences(
                SMART_CACHE_PREFERENCES, 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getCacheFileUrl(Context context,String key) {
        // SmartLog.log(TAG, "Get long integer value");
        SharedPreferences pref = context.getSharedPreferences(
                SMART_CACHE_PREFERENCES, 0);
        return pref.getString(key, "");
    }

    private static void removeAllCacheInfo(Context context){
        SharedPreferences preferences = context.getSharedPreferences(SMART_CACHE_PREFERENCES, 0);
        preferences.edit().clear().apply();
    }

    public static void storeCache(Context context, String key, String value) {
        AppData.getInstance(context).putStringValue(key.toUpperCase(), value);
    }

    public static String getCache(Context context, String key, String defaultValue) {
        String value =  AppData.getInstance(context).getStringValue(key.toUpperCase());
        if (value == null)
            value = defaultValue;
        return value;
    }

    public static String getCache(Context context, String key) {
        return getCache(context, key, "");
    }

    public static void storeHomePage(Context context,String ip){
        storeCache(context, "HOMEPAGE", ip);
    }

    public static String getHomePage(Context context) {
        String homepage = getCache(context, "HOMEPAGE");
        return homepage;
    }

    public static void storeAppMode(Context context,String ip){
        storeCache(context, "APP_MODE", ip);
    }

    public static String getAppMode(Context context){
        String mode = getCache(context, "APP_MODE");

        if (mode == null || mode.isEmpty())
            mode = Constants.APP_MODE_API;
        return mode;
    }


    // Cache Ip Address
    public static void storeAddressServerIP(Context context,String ip){
        storeCache(context, "ADDRESS_IP", ip);
    }


    public static String getAddressServerIP(Context context){
        return getCache(context, "ADDRESS_IP");
    }

    // Cache server port
    public static void storeServerPort(Context context,String port){
        storeCache(context, "SERVER_PORT",port);
    }

    public static String getServerPort(Context context){
        return getCache(context,"SERVER_PORT");
    }

    public static String getLastUpdateApp(Context context){
        return getCache(context,"LAST_UPDATE_APP");
    }

    public static String getAppVersionInfo(AppVersionObj versionObj) {
        if (versionObj != null)
            return "Version Id: " + versionObj.getId() + "-"  + versionObj.getVersionCode() + ". Name: " + versionObj.getName() +  ". Description: " + versionObj.getDescription() + ". Updated: " + versionObj.getLastUpdate();
        return "";
    }

    public static String getAppInfo(Context context) {
        return
        "[Device: " + PacketUtility.getDeviceName() +  "] [IME: " + PacketUtility.getDeviceUniqueID(context)  + "] [License: " + CacheManager.getLicenseKey(context) + "] [App: " + PacketUtility.getVersionName(context) + "]";
    }

    public static void saveLastUpdateApp(Context context, AppVersionObj versionObj) {
        try {
            if (versionObj != null) {
                storeCache(context, "LAST_UPDATE_APP",
                        getAppVersionInfo(versionObj) + ". Downloaded: " + DateTimeUtil.showCurrentDateTime() + "");
            } else {
//                storeCache(context, "LAST_UPDATE_APP",
//                        "" + DateTimeUtil.showCurrentDateTime());
            }
        } catch (Exception ex) {
            /*ignore*/
        }
    }

    public static String getLastUpdateSchedules(Context context){
        return  getCache(context, "LAST_UPDATE_SCHEDULES");
    }

    public static String getFullAddressIP(Context context){

        String ip = getAddressServerIP(context);
        String port = getServerPort(context);

        String fullAddress = "";
        if(!port.isEmpty()) {
            fullAddress = ip+":"+port;
        } else {
            fullAddress = ip;
        }

        return  fullAddress;
    }

    // Device Name
    public static void storeSettingDeviceName(Context context,String deviceName){
        storeCache(context, "DEVICE_NAME", deviceName);
    }

    public static String getSettingDeviceName(Context context){
        String deviceName = getCache(context, "DEVICE_NAME");
        if(deviceName.isEmpty()) {
            //get device name
            deviceName = PacketUtility.getDeviceName();
        }
        return  deviceName;
    }

    public static String getLicenseKey(Context context) {
        String license = getCache(context, "LICENSE_KEY");
        if(license.isEmpty()) {
            //get device name
            license = AppConfigs.APP_NAME;
        }
        return license;
    }

    public static void storeLicenseKey(Context context, String license) {
        String license1 = getCache(context, "LICENSE_KEY");
        if (!license.isEmpty() || (license.isEmpty() && license1.isEmpty()))
            storeCache(context,"LICENSE_KEY", license);
    }

    // Device description
    public static void storeSettingDeviceDescription(Context context,String deviceDescription){
        storeCache(context, "DEVICE_DESCRIPTION", deviceDescription);

    }

    public static String getSettingDeviceDescription(Context context) {
        String deviceDescription = getCache(context, "DEVICE_DESCRIPTION");

        if(deviceDescription.isEmpty()) {
            //get device name
            deviceDescription = PacketUtility.getDeviceName();
        }

        return deviceDescription;
    }


    //download resource time
    // Cache Ip Address
    public static void saveDownloadResourceTime(Context context,String time){
        storeCache(context, "DOWNLOAD_TIME",time);
    }

    public static String getDownloadResourceTime(Context context){
        return  getCache(context, "DOWNLOAD_TIME");
    }



}
