package com.stech.smartads.core;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.android.volley.VolleyError;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.stech.smartads.R;
import com.stech.smartads.activities.BaseActivity;
import com.stech.smartads.config.APIConfigs;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.config.SocketConfigs;
import com.stech.smartads.fragments.BaseFragment;
import com.stech.smartads.interfaces.IModelListener;
import com.stech.smartads.interfaces.IResponse;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.components.network.VolleyGet;
import com.stech.smartads.models.AppVersionObj;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.models.SchedulesObject;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.models.Schedule;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.DateTimeUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;

import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.FileUtility;
import com.stech.smartads.utils.LocalBroadCastUtil;
import com.stech.smartads.utils.PacketUtility;
import com.stech.smartads.utils.ParseUtility;
import com.stech.smartads.utils.StringUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppData  {
    private static final String TAG = AppData.class.getSimpleName();
    private static final String FRUITY_DROID_PREFERENCES = "SMART_ADS_PREFERENCES";
    public static boolean isUserSetting = false;

    private Schedule defaultSchedule;
    private Schedule currentSchedule;
    private Schedule nextSchedule;

    private int currentSchedulePosition = 0;
    private String license = "";
    private boolean isLicensed = false;

    //main schedule
    private List<Schedule> arrSchedules;
    private List<String> arrResourceFilePaths;

    //his screen config
    private ServerSetting serverSetting;

    private long downloadResourceTime = 0;

    private static AppData instance;

    private int screenHeight=0;
    private int screenWidth=0;
    private final Object mLock = new Object();
    private String mJsCallbackCode;

    private String json;
    private int timeout = -1; // -1: init, 0: forever, > 0: timeout (s)
    public Context context;
    private WebView webView;
    private Map<String, String> data = new ConcurrentHashMap<>();
    private BaseActivity currentActivity;
    private BaseFragment currentFragment;

    public AppData() {

    }

    public static AppData getInstance()
    {
        if (instance == null) {
            synchronized (AppData.class) {
                if (instance == null) {
                    instance = new AppData();
                }
            }
        }

        return instance;
    }

    public static AppData getInstance(Context ctx)
    {
        instance = getInstance();
        instance.setContext(ctx);
        return instance;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context ctx) {
        context = ctx;
        if (currentActivity == null && ctx instanceof  BaseActivity)
            currentActivity = (BaseActivity) ctx;
    }

    @JavascriptInterface
    public BaseActivity getCurrentActivity() {
        return currentActivity;
    }

    public void setCurrentActivity(BaseActivity ctx) {
        currentActivity = ctx;
    }

    public BaseFragment getCurrentFragment() {
        return currentFragment;
    }

    public void setCurrentFragment(BaseFragment ctx) {
        currentFragment = ctx;
    }


    public void setCurrentWebView(WebView wv) {
        webView = wv;
    }

    public WebView getWebView() {
        return webView;
    }

    public boolean getIsLicense() {
        return isLicensed;
    }

    public void setIsLicense(boolean value) {
        isLicensed = value;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String value) {
        license = value;
    }

    //screen height
    public void setScreenWidth(int screenWidth){
        this.screenWidth = screenWidth;
    }

    @JavascriptInterface
    public int getScreenWidth(){
        return this.screenWidth;
    }

    public void setScreenHeight(int screenHeight){
        this.screenHeight = screenHeight;
    }

    @JavascriptInterface
    public int getScreenHeight(){
        return this.screenHeight;
    }

    @JavascriptInterface
    public String getJson() {
        return json;
    }

    public void setJson(String value)  {
        this.json = value;
    }

    public boolean isJsonDifferent(String json1) {
        return ParseUtility.isJsonDifferent(getJson(), json1);
    }

    public boolean isNeedRefreshJson(String json1) {
        return ParseUtility.isValidJson(json1) && isJsonDifferent(json1);
    }

    public boolean isNeedRefreshLayout(Context context) {
        Schedule currentSchedule = getCurrentSchedule(); //store current schedule and then compare to new current schedule !!
        Schedule currentScheduleNew = getCurrentSchedule(context);
        if (currentSchedule == null || currentScheduleNew == null)
            return true;
        return currentSchedule.equals(currentScheduleNew);
    }

    //manage list Schedule
    public void setSchedules(List<Schedule> arrSchedules){
        this.arrSchedules = arrSchedules;
    }

    public List<Schedule> getSchedules() {
        if (this.arrSchedules == null){
            this.arrSchedules = new ArrayList<>();
        }
       return this.arrSchedules;
    }

    public Schedule getNextSchedule() {
        return nextSchedule;
    }

    public List<String> getArrResourceFilePaths() {
        if(arrResourceFilePaths == null){
            arrResourceFilePaths = new ArrayList<>();
        }
        return arrResourceFilePaths;
    }

    public void setArrResourceFilePaths(Context context,List<String> arrResourceFilePaths) {

        if(this.arrResourceFilePaths == null || this.arrResourceFilePaths.size() == 0){
            this.arrResourceFilePaths = arrResourceFilePaths;
            return;
        }

        /// Process to remove old resource is not needed
        //new list resources
        int newResourceListSize = arrResourceFilePaths.size();

        //old list resources
        int oldResourceListSize = this.arrResourceFilePaths.size();
        boolean isExisted;
        String oldResourceLink, newResourceLink;

        for(int i = 0; i < oldResourceListSize; i++ ){ // for of old list
            isExisted = false;
            oldResourceLink = this.arrResourceFilePaths.get(i);
            for (int j=0;j< newResourceListSize;j++){ // for of new list to check

                newResourceLink = arrResourceFilePaths.get(j);

                if(oldResourceLink.equals(newResourceLink)){
                    isExisted = true;
                    break;
                }
            }

            //process to remove if not match
            if(!isExisted){
                CacheManager.deleteCacheFile(context,oldResourceLink);
            }
        }

        //re-add resource list to global again
        this.arrResourceFilePaths = arrResourceFilePaths;
    }
    
    public void appendResourceFilePaths(List<String> arrResourceFilePaths) {

        if(this.arrResourceFilePaths == null){
            this.arrResourceFilePaths = arrResourceFilePaths;
            return;
        }

        int newResourceListSize = arrResourceFilePaths.size();
        int oldResourceListSize = this.arrResourceFilePaths.size();
        boolean isExisted;
        for(int i = 0; i < newResourceListSize; i++ ){
            isExisted = false;
            for (int j=0;j< oldResourceListSize;j++){
                if(arrResourceFilePaths.get(i).equals(this.arrResourceFilePaths.get(j))){
                    isExisted = true;
                    break;
                }
            }

            //add item is not existed in old list
            if(!isExisted){
                this.arrResourceFilePaths.add(arrResourceFilePaths.get(i));
            }
        }
    }

    //get next schedule
    public Schedule getNextSchedule(Context context) {
        nextSchedule=null;

        if (arrSchedules == null || arrSchedules.size() == 0) {
            return null;
        }

        //get next schedule by current time
        long scheduleTime=-1;
        long currentTime = -1;
        long nextScheduleTime = -1;
        Schedule schedule;

        for (int i = 0; i <arrSchedules.size(); i++) {

            schedule = arrSchedules.get(i);

            scheduleTime = schedule.getStartTime();
            currentTime =  DateTimeUtil.getCurrentTime(((MainApplication)context.getApplicationContext()).getCurrentCalendar(),DateTimeUtil.MILLISECOND);
            if (scheduleTime >= currentTime) {
                if (nextSchedule == null) {
                    nextSchedule = schedule;
                } else {
                    nextScheduleTime = nextSchedule.getStartTime();
                    if (scheduleTime < nextScheduleTime)
                        nextSchedule = schedule;
                }
            }

        }

        return nextSchedule;
    }

    public Schedule getDefaultSchedule() {
        return defaultSchedule;
    }

    public void setDefaultSchedule(Schedule defaultSchedule) {
        this.defaultSchedule = defaultSchedule;
    }

    //manage theme(list layout)
    public void setCurrentSchedule(Schedule currentSchedule){
        this.currentSchedule = currentSchedule;
    }

    @JavascriptInterface
    public Schedule getCurrentSchedule() {
        if (this.currentSchedule == null && this.context != null)
            this.currentSchedule = getCurrentSchedule(context);
        return this.currentSchedule;
    }

    public Schedule getCurrentSchedule(Context context) {
        Schedule schedule = null; //?? Hung

        //get next schedule by current time
        long scheduleTime1 =-1;
        long scheduleEndTime1 =-1;
        long currentTime = -1;

        List<Schedule> arrSchedules = this.getSchedules(); // getSchedules();

        for (int i = 0; i < arrSchedules.size(); i++) {

            currentTime =  DateTimeUtil.getCurrentTime(((MainApplication)context.getApplicationContext()).getCurrentCalendar(), DateTimeUtil.MILLISECOND);
            //CommonUtil.log("Schedules", "Total Schedules: " + arrSchedules.size());

            scheduleTime1 = arrSchedules.get(i).getStartTime();
            scheduleEndTime1 = arrSchedules.get(i).getFinishTime();

            if (scheduleTime1 <= currentTime  && currentTime < scheduleEndTime1) {
                currentSchedulePosition = i;
                if (currentSchedule != null) {
                    CommonUtil.error("Schedules", "Total Schedules: " + arrSchedules.size() + ". Current Schedule #" + currentSchedule.getId() + " :" + DateTimeUtil.convertTimeStampToDate(currentSchedule.getStartTime(), AppConfigs.FORMAT_SCHEDULE_TIME) + " - " + DateTimeUtil.convertTimeStampToDate(currentSchedule.getFinishTime(), AppConfigs.FORMAT_SCHEDULE_TIME));
                }

                if (AppConfigs.allowOverlapSchedules) {
                    if (schedule == null)
                        schedule = arrSchedules.get(i);
                    else
                        schedule.addFrameLayouts(arrSchedules.get(i));
                } else {
                    schedule = arrSchedules.get(i);
                    break;
                }
            }
        }

        if (schedule != null)
            currentSchedule = schedule;
        if (AppConfigs.allowOverlapSchedules && currentSchedule != null) {
            Schedule defaultSchedule = this.getDefaultSchedule(); // test
            currentSchedule.addFrameLayouts(defaultSchedule, 0); // also play default Schedule at background
        }
        return currentSchedule;
    }

    public String getAppMode() {
        String appMode = getServerSetting().getAppMode();
        if (appMode.isEmpty())
            appMode = CacheManager.getAppMode(context);
        if (appMode.isEmpty())
            appMode = Constants.APP_MODE_API;
        return appMode;
    }

    public String getHomepage() {
        String homepage = getServerSetting().getHomepage();
        if (homepage.isEmpty())
            homepage = CacheManager.getHomePage(context);
        if (homepage.isEmpty())
            homepage = Constants.APP_MODE_HOMEPAGE;
        return homepage;
    }

    public boolean isAPIMode() {
        String appMode = CacheManager.getAppMode(context);
        if (appMode.isEmpty())
            return true;

        String url = getCurrentUrl();
        return appMode.equalsIgnoreCase(Constants.APP_MODE_API) && url.isEmpty();
    }

    public boolean isAPIMode(String appMode) {
        if (appMode.isEmpty())
            return true;
        String url = getCurrentUrl();
        return appMode.equalsIgnoreCase(Constants.APP_MODE_API) && url.isEmpty();
    }

    public boolean isHomePageMode() {
        String appMode = CacheManager.getAppMode(context);
        String url = getCurrentUrl();
        return appMode.equalsIgnoreCase(Constants.APP_MODE_HOMEPAGE) && url.isEmpty();
    }

    public boolean isHomePageMode(String appMode) {
        String url = getCurrentUrl();
        return appMode.equalsIgnoreCase(Constants.APP_MODE_HOMEPAGE) && url.isEmpty();
    }

    public boolean isWebsiteMode() {
        String url = getCurrentUrl();
        return !url.isEmpty();
    }

    public boolean isNextScheduleAvailable(){
        return arrSchedules != null && arrSchedules.size()-1 > currentSchedulePosition;
    }

    public long getDownloadResourceTime() {
        return downloadResourceTime;
    }

    public void setDownloadResourceTime(long downloadResourceTime) {
        this.downloadResourceTime = downloadResourceTime;
    }

    public void cleanup()
    {
        this.currentSchedule = null;
        this.arrSchedules = null;
        this.currentSchedulePosition = 0;
        this.json = null;
    }

    //layoutFrame of fullscreen website
    private DataContentObj currentContent;

    @JavascriptInterface
    public DataContentObj getCurrentContent() {
        return currentContent;
    }

    public void setCurrentContent(DataContentObj obj) {
        currentContent = obj;
    }

    //layoutFrame of fullscreen website
    private LayoutFrameObj currentLayoutFrame;
    public LayoutFrameObj getCurrentLayoutFrame() {
        return currentLayoutFrame;
    }

    public void setCurrentLayoutFrame(LayoutFrameObj obj) {
        currentLayoutFrame = obj;
    }

    //temporary url of fullscreen website (triggered by singleClick of SlideFragment) !!
    private String currentUrl = "";
    public String getCurrentUrl() {
        return currentUrl;
    }

    public void setCurrentUrl(String url) {
        if (url.contains("youtube.com/watch")) {
            url = url.replace("https://", "").replace("www.", "").replace(".com/watch", "");
        } else if (url.contains("youtube.com/embed")) {
            url = url.replace("https://", "").replace("www.", "").replace("?", "&").replace(".com/embed/", "?v=");
        } else if (url.contains("youtu.be")) {
            url = url.replace("https://", "").replace("www.", "").replace("?", "&").replace("youtu.be/", "youtube?v=");
        }

        currentUrl = url;
    }

    public ServerSetting getServerSetting() {

        if(serverSetting == null){
            serverSetting = new ServerSetting();
        }
        return serverSetting;
    }

    public void setServerSetting(ServerSetting serverSetting) {
        this.serverSetting = serverSetting;
    }

    public void parseJson(Context ctx, String json) {
        if (json == null || !ParseUtility.isSuccess(json))
            return;

        setJson(json);

        setServerSetting(ParseUtility.parseServerSetting(json));

        //parse default theme from server
        Schedule defaultSchedule = ParseUtility.parseDefaultSchedule(json);
        if (defaultSchedule != null && defaultSchedule.getFrameLayouts().size() > 0) {
            defaultSchedule.setDefault(true);
            setDefaultSchedule(defaultSchedule);
        }

        //parse schedule
        ArrayList<SchedulesObject> scheduleObjs = (ArrayList<SchedulesObject>) ParseUtility.parseSchedule(json);

        if (scheduleObjs.size() > 0) {

            SchedulesObject scheduleObj;
            for (int i = 0; i < scheduleObjs.size(); i++) {
                scheduleObj = scheduleObjs.get(i);
                if (scheduleObj.isTodaySchedules(ctx)) {
                    setSchedules(scheduleObj.getArrScheduleItems());
                    break;
                }
            }
        } else {
            getSchedules().clear();
        }

        //handler to download files
        setArrResourceFilePaths(ctx, ParseUtility.parseResourceFiles(json));
    }

    public void getOfflineDataAndSwitchToOfflineMode() {
        //NetworkUtility.getInstance(context).setOffline(true); // can not do it
        String cachedJson = CacheManager.getCacheSchedulesJson(context);
        parseJson(context, cachedJson);
    }

    public void getOfflineDataAndSwitchToOfflineMode(Context context) {
        //NetworkUtility.getInstance(context).setOffline(true); can not do it
        String cachedJson = CacheManager.getCacheSchedulesJson(context);
        parseJson(context, cachedJson);
    }

    /**
     * Save an string to MySharedPreferences
     *
     * @param key
     * @param s
     */
    public void putStringValue(String key, String s) {
        // SmartLog.log(TAG, "Set string value");
        android.content.SharedPreferences pref = context.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0);
        android.content.SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, s);
        editor.commit();
    }

    /**
     * Read an string to MySharedPreferences
     *
     * @param key
     * @return
     */
    public String getStringValue(String key) {
        // SmartLog.log(TAG, "Get string value");
        android.content.SharedPreferences pref = context.getSharedPreferences(
                FRUITY_DROID_PREFERENCES, 0);
        String value = pref.getString(key, "");
        if (value == null)
            value = "";
        return value;
    }

    public Uri.Builder getAPIBuilder(String url) {
        if (!url.toLowerCase().startsWith("http"))
            url = getAPIUrl(url);

        Uri.Builder builder = Uri.parse(url).buildUpon();
        if (AppConfigs.AUTO_SEND_HASHKEY_TO_API)
            builder.appendQueryParameter("hash", CommonUtil.generateHashKey(context));
        builder.appendQueryParameter("ime", PacketUtility.getDeviceUniqueID(context));

        return builder;
    }

    public String getConfiguredAddressIp() {

        String url = CacheManager.getFullAddressIP(context);
        if (StringUtil.isFullUrl(url))
            return url;
        return Constants.PROTOCOL_HTTP + url;

    }

    public String getSocketAddressIp(){
        return Constants.PROTOCOL_HTTP + APIConfigs.SERVER_URL + ":" + SocketConfigs.SOCKET_SERVER_PORT;
    }

    public void requestServerTime(final IModelListener listener) {
        final Context ctx = context;

        if (!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            if (AppConfigs.OFFLINE_ENABLED)  {
                listener.onSuccess(null);
                return;
            } else {
                listener.onError(null);
            }
            return;
        }


        Uri.Builder builder = getInstance().getAPIBuilder(APIConfigs.URL_REQUEST_SERVER_TIME);

        //call api
        new VolleyGet(ctx, false, false).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {
                String json = ParseUtility.parseJsonFromObject(response);
                String errorMessage = ParseUtility.getErrorMessage(json);
                boolean isSuccess = ParseUtility.isSuccess(json);
                CommonUtil.error("requestServerTime - ONLINE Response JSON", response.toString());

                if (isSuccess) {
                    listener.onSuccess(response);

                } else {
                    CommonUtil.message(ctx, Constants.TEXT_SYNC_SERVER_TIME + " " + Constants.TEXT_ERROR + ": " + errorMessage);
                    listener.onError(null);
                }
            }

            @Override
            public void onError(VolleyError error) {
                CommonUtil.error(ctx, error);
                CommonUtil.message(ctx, Constants.TEXT_SYNC_SERVER_TIME + " " + Constants.TEXT_ERROR + ": " + CommonUtil.getErrorMessage(error));
                listener.onError(error);
            }
        });
    }

    public void registerDevice(final IModelListener listener) {
        final Context ctx = getContext();

        if (!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            //Hung: Can skip register
            if (AppConfigs.OFFLINE_ENABLED)  {
                listener.onSuccess(null);
                return;
            } else {
                listener.onError(null);
            }
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_REGISTER_DEVICE);
        builder.appendQueryParameter("device_name", CacheManager.getSettingDeviceName(ctx));
        builder.appendQueryParameter("device_description", CacheManager.getSettingDeviceDescription(ctx));
        builder.appendQueryParameter("device_name", CacheManager.getSettingDeviceName(ctx));
        builder.appendQueryParameter("mac_address", PacketUtility.getMacAddress());
        builder.appendQueryParameter("ScreenName", PacketUtility.getScreenName());
        builder.appendQueryParameter("last_activity", CacheManager.getLastUpdateApp(ctx));

        //call api
        new VolleyGet(ctx, true, false).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {
                String json = ParseUtility.parseJsonFromObject(response);
                String errorMessage = ParseUtility.getErrorMessage(json);
                boolean isSuccess = ParseUtility.isSuccess(json);
                CommonUtil.error("Register Device - ONLINE Response JSON", response.toString());

                if (isSuccess) {
                    NetworkUtility.getInstance(ctx).setOffline(false);

                    //parse setting
                    setServerSetting(ParseUtility.parseServerSetting(response.toString()));
                    setDefaultSchedule(ParseUtility.parseDefaultSchedule(response.toString()));
                    setJson(response.toString());

                    listener.onSuccess(response);

                } else {
                    CommonUtil.message(ctx, Constants.TEXT_REGISTER_DEVICE + " " + Constants.TEXT_ERROR + ": " + errorMessage);
                    listener.onError(null);
                }
            }

            @Override
            public void onError(VolleyError error) {
                CommonUtil.message(ctx, Constants.TEXT_REGISTER_DEVICE + " " + Constants.TEXT_ERROR + ": " + CommonUtil.getErrorMessage(error));
                NetworkUtility.getInstance(ctx).setOffline(true);

                listener.onError(error);
            }
        });
    }

    public void getSchedulesForModeHomePage(final IModelListener listener) {
        listener.onSuccess(null);
    }

    //hung:
    public String getWebsiteUrl() {
        String url = getCurrentUrl();

        if (url == null || url.isEmpty()) {
            if (isHomePageMode())
                url = getHomepage();
            else
                url = CacheManager.getHomePage(context);
        }

        //CommonUtil.message(context, StringUtil.getFullUrl(url));
        return StringUtil.getFullUrl(url);
    }

    @JavascriptInterface
    public String getAPIUrl(String url) {
        if (!url.startsWith("http")) {
            if (!url.startsWith(APIConfigs.BASE_URL))
                url = APIConfigs.BASE_URL + "/" + url;

            url = getConfiguredAddressIp() + url;
        }
        return url;
    }


    public void getSchedules(final IModelListener listener) {
        AppData app = this;
        final Context ctx = getContext();

        if (!app.isAPIMode()) {
            getSchedulesForModeHomePage(listener);
            return;
        }

        if (!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            if (AppConfigs.OFFLINE_ENABLED) {
                getOfflineDataAndSwitchToOfflineMode(ctx);
                listener.onSuccess(null);
            } else {
                listener.onError(null);
            }

            return;  //HUNGHX:OFFLINE - Tiếp tục xử lý
        }

        Uri.Builder builder = app.getAPIBuilder(APIConfigs.URL_GET_SCHEDULES);

//        if(scheduleID!=null && !scheduleID.isEmpty()) {
//            builder.appendQueryParameter("schedule_id", scheduleID);
//        } else if(getCurrentSchedule()!=null) {
//            builder.appendQueryParameter("schedule_id", getCurrentSchedule().getId()+"");
//        }
//
//        if(channelID!=null && !channelID.isEmpty()) {
//            builder.appendQueryParameter("channel_id", channelID);
//        }
//
//        if (action!=null && !action.isEmpty()) {
//            builder.appendQueryParameter("action", action);
//        }

        //CommonUtil.log(TAG, "Call API GetSchedules" + " Url: " + builder.toString());
        //CommonUtil.message(ctx, "Call API GetSchedules" + " Url: " + builder.toString());

        //call api
        new VolleyGet(ctx, false, false).getStringRequest(builder, new IResponse() {
                    @Override
                    public void onResponse(Object response) {
                        String json = ParseUtility.parseJsonFromObject(response);
                        String cachedJson = CacheManager.getCacheSchedulesJson(ctx);
                        String errorMessage = ParseUtility.getErrorMessage(json);
                        boolean isSuccess = ParseUtility.isSuccess(json);

                        CommonUtil.error("Response JSON: ", response.toString());

                        if ((!isSuccess || response == null || json.isEmpty() || !errorMessage.isEmpty()) && AppConfigs.OFFLINE_ENABLED) {
                            CommonUtil.message(ctx,   Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_SCHEDULE + " " + Constants.TEXT_FAIL + ": "  + errorMessage);
                            NetworkUtility.getInstance(ctx).setOffline(true);

                            if (isJsonDifferent(cachedJson)) {
                                json = cachedJson;
                                if (ParseUtility.isJsonDifferent(getJson(), json)) {
                                    CacheManager.cacheSchedulesJson(ctx, json);
                                    parseJson(ctx, json);
                                }
                            }
                        } else { //load online successfully

                            String deviceName = ParseUtility.parseDeviceName(json);
                            if (!deviceName.equals(CacheManager.getSettingDeviceName(ctx))) {
                                CacheManager.storeSettingDeviceName(ctx, deviceName);
                            }

                            String deviceDescription = ParseUtility.parseDeviceDescription(json);
                            if (!deviceDescription.equals(CacheManager.getSettingDeviceDescription(ctx))) {
                                CacheManager.storeSettingDeviceDescription(ctx, deviceDescription);
                            }
                            //CommonUtil.message(ctx, deviceName + ":" + deviceDescription);

                            String license = ParseUtility.parseLicense(json);
                            boolean isLicensed = PacketUtility.checkLicense(ctx);
                            setIsLicense(isLicensed);
                            if (license != null && !license.isEmpty()) {
                                CacheManager.storeLicenseKey(ctx, license);
                            }

                            NetworkUtility.getInstance(ctx).setOffline(false);

                            //boolean need_refresh_schedules = ParseUtility.parseNeedRefreshSchedules(json) || isNeedRefreshJson(json);

                            //if (need_refresh_schedules) {
                            //cache online data
                            CacheManager.cacheSchedulesJson(ctx, json);
                            parseJson(ctx, json);
                            //}
                        }

                        listener.onSuccess(json);
                    }

                    @Override
                    public void onError(VolleyError error) {
                        CommonUtil.error(ctx, error);
                        CommonUtil.message(ctx, Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_SCHEDULE + " " + Constants.TEXT_FAIL + ": " + CommonUtil.getErrorMessage(error));
                        NetworkUtility.getInstance(ctx).setOffline(true);
                        listener.onError(error);
                    }
                }
        );
    }


    public void checkConnection(final IModelListener listener) {
        final Context ctx = getContext();

        if (AppConfigs.API_CHECK_STATUS_DEVICE_TIMER <=  0)
            return;

        if (!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            if(listener!=null){
                listener.onError(null);
            }
            return;
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_CHECK_CONNECTION);

        if(getCurrentSchedule() != null) {
            builder.appendQueryParameter("current_schedule", getCurrentSchedule().getId()+"");
        }
        if (AppConfigs.AUTO_SEND_HASHKEY_TO_API)
            builder.appendQueryParameter("hash", CommonUtil.generateHashKey(ctx));
        //builder.appendQueryParameter("free_disk_size", CommonUtil.getAvailableDiskSize()+"");

        CommonUtil.log(TAG,  "Call API GetStatusDevice (checkConnection) "  + " Url: " + builder.toString());
        new VolleyGet(ctx, false, true).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {

                if(listener!= null)
                    listener.onSuccess(response);

                LocalBroadCastUtil.sendBroadcastListener(ctx, LocalBroadCastUtil.ACTION_CONNECT_SERVER_SUCCESSFUL);
            }

            @Override
            public void onError(VolleyError error) {
                if(error != null){
                    CommonUtil.error(ctx, error);
                } else {
                    CommonUtil.error(TAG, Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_FAIL);
                }

                if(listener!= null){
                    listener.onError(error);
                }
            }
        });
    }

    public void sendLogToServer(final IModelListener listener, String log) {
        final Context ctx = getContext();

        if (!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            if(listener!=null){
                listener.onError(null);
            }
            return;
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_CHECK_CONNECTION);

        builder.appendQueryParameter("log", log + "");

        CommonUtil.message(ctx,  "Add LOG: " + log);
        new VolleyGet(ctx, false, true).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {

                if(listener!= null)
                    listener.onSuccess(response);

                LocalBroadCastUtil.sendBroadcastListener(ctx, LocalBroadCastUtil.ACTION_CONNECT_SERVER_SUCCESSFUL);
            }

            @Override
            public void onError(VolleyError error) {

                if(error != null) {
                    CommonUtil.error(ctx, error);
                } else {
                    CommonUtil.error(TAG, Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_FAIL);
                }

                if(listener!= null){
                    listener.onError(error);
                }
            }
        });
    }

    public void downloadFile(String urlFile, boolean isCompleted, final IModelListener listener) {
        final Context ctx = getContext();

        if(!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            if(listener!=null) {
                listener.onError(null);
            }
            return;
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_REQUEST_DOWNLOAD_FILE);
        builder.appendQueryParameter("file", urlFile);

        if(isCompleted){
            builder.appendQueryParameter("status", "done");
        }

        new VolleyGet(ctx, false, true).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {

                if(listener != null)
                    listener.onSuccess(response);
            }

            @Override
            public void onError(VolleyError error) {
                if (error != null){
                    CommonUtil.error(ctx, error);
                    //if(error.networkResponse.statusCode == )

                } else {
                    CommonUtil.error(TAG, Constants.TEXT_UPDATE_DEVICE + " " + Constants.TEXT_FAIL);
                }

                if(listener != null)
                    listener.onError(error);
            }
        });
    }

    public String getAppVersion() {
        return PacketUtility.getVersionName(context);
    }

    public String getAppName() {
        return AppConfigs.APP_NAME;
    }

    public void requestNewAppVersion(final IModelListener listener) {
        final Context ctx = getContext();

        if (!NetworkUtility.getInstance(ctx).isNetworkAvailable()) {
            if(listener !=null) {
                CommonUtil.message(ctx, Constants.TEXT_CHECK_NETWORK);
            }
            return;
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_REQUEST_APP_LATEST_VERSION);
        builder.appendQueryParameter("versionName", getAppVersion());
        //builder.appendQueryParameter("versionCode", PacketUtility.getVersionCode(ctx) + "");
        builder.appendQueryParameter("packageName", getAppName());

        //CommonUtil.message(context, builder.toString());

        new VolleyGet(ctx, false, false).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {
                if(listener != null)
                    listener.onSuccess(response);
            }

            @Override
            public void onError(VolleyError error) {
                if(listener != null)
                    listener.onError(error);
            }
        });
    }

    public String getApkName() {
        return context.getString(R.string.app_name).replace(" ","_")+ ".apk";
    }

    public boolean downloadApp(final Activity activity, final AppVersionObj versionObj) {
        return downloadApp(activity, versionObj.getName(), versionObj.getFileUrl());
    }

    public boolean downloadApp(final Activity activity, final String apkurl) {
        return downloadApp(activity, "", apkurl);
    }

    public boolean downloadApp(final Activity activity, String apkFolder, final String apkurl) {
        final boolean override = AppConfigs.OVERRIDE_DOWNLOAD_APP; // delete before or after download successfully !!
        String folder = CacheManager.getCacheFolder() + File.separator;
        if (!apkFolder.isEmpty())
            folder = folder + apkFolder + File.separator;
        String extension = apkurl.substring(apkurl.lastIndexOf(".") + 1);
        String fileName = "";
        if (fileName.isEmpty()) {
            fileName = apkurl.substring(apkurl.lastIndexOf("/") + 1);
            fileName = fileName.replace(" ", "-");
        }
        if (!fileName.contains(".") && !extension.isEmpty())
            fileName = fileName + "." + extension;

        String fileNameNew = override ? fileName : fileName.replace(".", "_" + DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND) + ".");

        final String finalDestination = folder + fileName;
        final String finalDestinationNew = folder + fileNameNew;

        //Delete update file if exists
        final File file = FileUtility.getFile(finalDestination);
        final File fileNew = FileUtility.getFile(finalDestinationNew);
        final File folderNew = FileUtility.getFolder(folder);

        boolean result = true;

        if (override && file.exists() && file.delete()) {
            CommonUtil.message(activity,  Constants.TEXT_DELETE_APP + " " + Constants.TEXT_SUCCESSFUL);
        }

        CommonUtil.message(activity,  Constants.TEXT_DOWNLOAD_AND_OVERRIDE);

        final Uri uri = Uri.fromFile(new File( finalDestinationNew));
        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkurl));
        request.setDescription(apkurl);
        request.setTitle(fileName);

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    CommonUtil.message(activity, Constants.TEXT_DOWNLOAD + " " + Constants.TEXT_SUCCESSFUL);

                    if (!override && file.exists() && file.delete()) {
                        CommonUtil.message(activity, Constants.TEXT_DELETE_APP + " " + Constants.TEXT_SUCCESSFUL);
                    }

                    boolean ok = !override ? fileNew.renameTo(file) : true;
                    if (!ok)  {
                        CommonUtil.message(activity, Constants.TEXT_INSTALL_APP + " " + Constants.TEXT_ERROR + ". Failed to rename file: " + fileNew.getAbsolutePath() + " -> " + file.getAbsolutePath());
                    } else {

                        try {
                            if (apkurl.toLowerCase().endsWith(".zip")) {
                                CommonUtil.message(activity, "Unzip file: " + fileNew.getAbsolutePath() + " to folder: " + folderNew.getAbsolutePath());
                                FileUtility.unzip(fileNew, folderNew);
                            }
                        } catch (IOException ex) {
                            CommonUtil.error(ex);
                        }

                        CommonUtil.message(activity, Constants.TEXT_INSTALL_APP + " " + Constants.TEXT_SUCCESSFUL);
                    }
                }
            };
            activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        } catch (SecurityException ex) {
            CommonUtil.error(ex);
        }
        return result;
    }

    public boolean updateApk(final Activity activity, final AppVersionObj versionObj) {
        String apkurl = versionObj.getFileUrl();

        final boolean override = AppConfigs.OVERRIDE_DOWNLOAD_APP; // delete before or after download successfully !!
        String folder = CacheManager.getCacheFolder() + File.separator;
        String fileName = getApkName(); // activity.getString(R.string.app_name).replace(" ","_")+ ".apk";
        //CommonUtil.message(activity, "app name: " + fileName);

        String fileNameNew = override ? fileName : fileName.replace(".", DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND) + ".");

        final String finalDestination = folder + fileName;
        final String finalDestinationNew = folder + fileNameNew;

        //Delete update file if exists
        final File file = new File(finalDestination);
        final File fileNew = new File(finalDestinationNew);
        boolean result = true;
        //CommonUtil.message(activity,  finalDestination + (file.exists() ? " EXISTED " : "NOT EXISTED"));

        if (override) {
            if (file.exists() && file.delete()) {
                CommonUtil.message(activity,  Constants.TEXT_DELETE_APP + " " + Constants.TEXT_SUCCESSFUL);
            }
        } else {

        }

        CommonUtil.message(activity,  Constants.TEXT_DOWNLOAD_AND_OVERRIDE + folder + fileName);

        final Uri uri = Uri.fromFile(new File( folder + fileNameNew));
        //set download manager
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkurl));
        request.setDescription("Update new APK");
        request.setTitle(activity.getString(R.string.app_name));

        //set destination
        request.setDestinationUri(uri);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        try {
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {

                    CommonUtil.message(activity, Constants.TEXT_DOWNLOAD + " " + Constants.TEXT_SUCCESSFUL);

                    if (!override && file.exists() && file.delete()) {
                        CommonUtil.message(activity, Constants.TEXT_DELETE_APP + " " + Constants.TEXT_SUCCESSFUL);
                    }

                    boolean ok = !override ? fileNew.renameTo(file) : true;
                    if (!ok)  {
                        CommonUtil.message(activity, Constants.TEXT_INSTALL_APP + " " + Constants.TEXT_ERROR);
                    } else {

                        CommonUtil.message(activity, Constants.TEXT_INSTALL_APP + " " + Constants.TEXT_SUCCESSFUL);

                        Intent install;

                        //CommonUtil.message(activity, activity.getPackageName());

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Uri contentUri = FileProvider.getUriForFile(
                                    activity,
                                    activity.getPackageName() + ".provider",
                                    new File(finalDestination)
                            );

                            install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                            install.setDataAndType(contentUri, manager.getMimeTypeForDownloadedFile(downloadId));
                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
                        } else {
                            Uri contentUri = Uri.fromFile(new File(finalDestination));

                            install = new Intent(Intent.ACTION_VIEW);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                            install.setDataAndType(contentUri, manager.getMimeTypeForDownloadedFile(downloadId));
                        }

                        activity.startActivity(install);
                        activity.unregisterReceiver(this);
                        activity.finish();

                        CacheManager.saveLastUpdateApp(activity, versionObj);
                    }
                }
            };
            activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            //register receiver for when .apk download is compete

        } catch (SecurityException ex) {
            CommonUtil.error(ex);
        }

        return result;
    }

    public void requestHisData(final IModelListener listener) {
        final Context ctx = getContext();

        if(!NetworkUtility.getInstance(ctx).isNetworkAvailable()){
            if(listener!=null){
                listener.onError(null);
            }
            return;
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_REQUEST_HIS_DATA);
        builder.appendQueryParameter("current_time", DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND)+"");

        new VolleyGet(ctx, false, false).getStringRequest(builder, new IResponse() {
            @Override
            public void onResponse(Object response) {
                if(listener != null)
                    listener.onSuccess(response);
            }

            @Override
            public void onError(VolleyError error) {
                if(error != null){
                    CommonUtil.error(ctx, error);

                } else {
                    CommonUtil.error(TAG, "Có lỗi xảy ra trong quá trình tải dữ liệu HIS !!");
                }

                if(listener != null)
                    listener.onError(error);
            }
        });
    }

    public void requestPolling(final IModelListener listener) {
        final Context ctx = getContext();
        if(!NetworkUtility.getInstance(ctx).isNetworkAvailable()){
            if(listener!=null){
                listener.onError(null);
            }
            return;
        }

        Uri.Builder builder = getAPIBuilder(APIConfigs.URL_REQUEST_POLLING);

        //get data from asset
        AssetManager assetManager = ctx.getAssets();
        try {
            InputStream istr = assetManager.open("json/surveys.json");
            String data = FileUtility.readStringFromIS(istr);
            listener.onSuccess(data);
        } catch (IOException e) {
            //CommonUtil.error(e);
        }


//        new VolleyGet(ctx, false, true).getStringRequest(builder, new IResponse() {
//            @Override
//            public void onResponse(Object response) {
//
//                if(listener != null)
//                    listener.onSuccess(response);
//            }
//
//            @Override
//            public void onError(VolleyError error) {
//                if(error != null){
//                    CommonUtil.error(TAG, "Error : "+  error.getMessage());
//
//                } else {
//                    CommonUtil.error(TAG, "Error : Request download File is failed ");
//                }
//
//                if(listener != null)
//                    listener.onError();
//            }
//        });
    }

    @JavascriptInterface
    public void executeJSFunc(String jsCallbackCode) {
        synchronized (mLock) {
            mJsCallbackCode = jsCallbackCode;
        }
        // Start some business logic asynchronously, and return back here immediately.
        return;
    }

    public void onBusinessLogicCompleted(WebView mWebView, boolean success) {
        String jsCallbackCode;
        synchronized (mLock) {
            jsCallbackCode = mJsCallbackCode;
        }
        mWebView.loadUrl("javascript:" + jsCallbackCode + "(" + success + ");void(0);");
    }

    @JavascriptInterface
    public String callAPI(final String url, final IModelListener listener) {
        Uri.Builder builder = getAPIBuilder(url);

        data.remove(url);
        //CommonUtil.message(context, builder.toString());

        //call api
        new VolleyGet(context, false, false).getStringRequest(builder, new IResponse() {
                    @Override
                    public void onResponse(Object response) {
                        final String json = ParseUtility.parseJsonFromObject(response);
                        data.put(url, json);
                        if (listener != null) {
                            listener.onSuccess(json);
                        }
                    }

                    @Override
                    public void onError(VolleyError error) {
                        CommonUtil.error(context, error);
                        if (listener != null)
                            listener.onError(error);
                    }
                }
        );

        int i = 0;
        while (true || i == 30) {
            try {
                Thread.sleep(100); //important: wait after get response from api
                i += 1;
            } catch (InterruptedException ex) {

            }
            if (data.containsKey(url))
                break;
        }

        return data.containsKey(url) ? data.get(url) : null;
    }

    @JavascriptInterface
    public List<DataContentObj> getCurrentDataList() {
        if (currentLayoutFrame == null || currentLayoutFrame.getData().size() == 0)
            return new LinkedList<DataContentObj>();
        return currentLayoutFrame.getData();
    }

    @JavascriptInterface
    public String getCurrentDataJson() {
        if (currentLayoutFrame == null || currentLayoutFrame.getData().size() == 0)
            return new JsonObject().toString();

        return currentLayoutFrame.getJson();
    }

    @JavascriptInterface
    public LayoutFrameObj getCurrentLayout() {
        return currentLayoutFrame;
    }

    @JavascriptInterface
    public void alert(String text) {
        CommonUtil.message(context, text);
    }

    @JavascriptInterface
    public void runAsync(final String rand, final String funcName, final String jsonParams) {
        final AppData app = this;
        new Thread() {
            // runs the java function in a new thread
            @Override public void run() {
                try {
                    final JSONObject params = new JSONObject(jsonParams);
                    String result = (String) app.getClass().getMethod(funcName, JSONObject.class).invoke(app, params);
                    app.jsResolve(rand, true, result);
                } catch (InvocationTargetException ite) { // exceptions inside the funcName function
                    app.jsResolve(rand, false, ite.getCause().toString());
                } catch (Exception e) {
                    app.jsResolve(rand, false, e.toString());
                }
            }
        }.start();
    }

    private void jsResolve(String rand, boolean isSuccess, String result) { // notify that result is ready
        data.put(rand, result);
        final String url = "javascript:" + rand + ".callback(" + isSuccess + ")";
        //CommonUtil.log("LOG_TAG", "calling js method with url " + url);
        if (webView != null) {
            webView.post(new Runnable() {
                @Override
                public void run() {
                    webView.loadUrl(url);
                }
            });
        }
    }

    @JavascriptInterface
    public String runAsyncResult(String rand) { // returns the result from runAsync to JS
        String result = data.get(rand);
        data.remove(rand);
        return result;
    }

    @JavascriptInterface
    public void showMessage(String msg) {
        CommonUtil.message(context, msg);
    }

    @JavascriptInterface
    public String getCachedSchedulesJson() {
        String cachedJson = CacheManager.getCacheSchedulesJson(context);
        return cachedJson;
    }

    @JavascriptInterface
    public String getDebugInfo() {
        String info = CacheManager.getAppInfo(context) + " "
                //+ "[ IME: " + PacketUtility.getDeviceUniqueID(this) + "]"
                //+ "[License: " + CacheManager.getLicenseKey(context) + "] "
                + "[" + CacheManager.getLastUpdateApp(context) + "] "
                + "[Screen: " + new StringBuilder().append(PacketUtility.getScreenWidth()).append("x").append(PacketUtility.getScreenHeight()).toString() + "] "
                + "[Schedule: " + CacheManager.getLastUpdateSchedulesTimeDisplay(context) + "] "
                + "[Data Cached: " + CacheManager.getLastUpdateSchedules(context) + "] "
                + "[Mode: " + CacheManager.getAppMode(context) + "] "
                + "[Network: " + (NetworkUtility.getInstance(context).isOffline() ? "Offline" : "Online") + "]";
        return info;
    }

    @JavascriptInterface
    public String getSchedulesText() {
        List<Schedule> schedules = getSchedules();
        StringBuilder builder = new StringBuilder();
        if (getCurrentSchedule() != null)
            builder.append( getCurrentSchedule().AsString()).append("\n---\n");
        for (int i = 0; i < schedules.size(); i ++) {
            builder.append( schedules.get(i).AsString()).append("\n");
        }

        return builder.toString();
    }

    @JavascriptInterface
    public int getOrientation() {
        return getCurrentActivity().getResources().getConfiguration().orientation;
    }

    @JavascriptInterface
    public boolean isPortrait() {
        return getCurrentActivity().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    @JavascriptInterface
    public int getTimeOut() {
        if (timeout > Constants.TIMEOUT_INIT_VALUE)
            return timeout;
        return getServerSetting().getIdleTimeOut() * 1000;
    }

    @JavascriptInterface
    public void setTimeOut(int _timeout) {
        timeout = _timeout;
    }

    @JavascriptInterface
    public void clearTimeOut() {
        setTimeOut(Constants.TIMEOUT_INIT_VALUE);
    }

    @JavascriptInterface
    public String getContentJson(String contentId) {
        DataContentObj obj = getCurrentContent();
        if (obj != null && (contentId == null || contentId.isEmpty() || contentId.equalsIgnoreCase(new StringBuilder().append(obj.getId()).toString())))
            return getCurrentDataJson();
        return callAPI("content?" + Constants.PARAM_CONTENT_ID + "=" + contentId, null);
    }

    @JavascriptInterface
    public String getContentJson() {
        return getContentJson(null);
    }

    @JavascriptInterface
    public void switchAppMode(String mode) {
        if (currentActivity != null)
            currentActivity.switchAppMode(mode);
    }

    @JavascriptInterface
    public void gotoWebScreen(String url) {
        if (currentActivity != null)
            currentActivity.gotoWebScreen(url);
    }

    @JavascriptInterface
    public void gotoWebScreen(String url, int timeout) {
        if (currentActivity != null)
            currentActivity.gotoWebScreen(url, timeout);
    }

    @JavascriptInterface
    public String getDeviceImagesJson() {
        List<DataContentObj> list = FileUtility.getImagesFromDevice(getCurrentActivity());
        return ParseUtility.getDataJsonObject(list).toString();
    }

    @JavascriptInterface
    public String getLocalAppsJson() {
        List<File> files  = FileUtility.getSubFiles(CacheManager.getCacheFolder() + File.separator + AppConfigs.APPS_FOLDER, Constants.TYPE_FOLDER);
        List<DataContentObj> list = new LinkedList<DataContentObj>();
        for (File file : files) {
            if (file.getName().startsWith("__"))
                continue;
            list.add(new DataContentObj(file.getName(), AppConfigs.APPS_FOLDER + File.separator + file.getName(), Constants.TYPE_HTML));
        }
        return ParseUtility.getDataJsonObject(list).toString();
    }

    @JavascriptInterface
    public String getDownloadableAppsJson() {
       return callAPI("app-version?platform=apps&id=*", null);
    }

    @JavascriptInterface
    public String getUrlForClient(String url) {
        String tmp = url.startsWith("@") ? url.replace("@", "") : url;
        if (tmp.equalsIgnoreCase(Constants.APP_MODE_API) || tmp.equalsIgnoreCase(Constants.APP_MODE_HOMEPAGE) || tmp.equalsIgnoreCase(Constants.APP_MODE_SETTINGS)) {
            return url;
        }

        if (!url.toLowerCase().contains(Constants.PARAM_TIMEOUT + "=")) {
            url = url + (url.contains("?") ? "&" : "?") + Constants.PARAM_TIMEOUT + "=0";
        }

        //CommonUtil.message(context, url);
        return url;
    }

    @JavascriptInterface
    public String getFullUrl(String url) {
        if (url == null)
            return "";

        url = url.trim();

        if (StringUtil.isFullUrl(url))
            return url;

        String postFix = "";
        if (url.contains("?")) {
            postFix = url.substring(url.lastIndexOf("?"));
            url = url.substring(0, url.lastIndexOf("?"));
        } else if (url.contains("#")) {
            postFix = url.substring(url.lastIndexOf("#"));
            url = url.substring(0, url.lastIndexOf("#"));
        }

        url = url.toLowerCase(); //tolowercase must be executed after get postFix to keep params case-sensitive.

        String separator = url.startsWith(File.separator) ? "" : File.separator;

        if (url.startsWith("json/") || url.startsWith("/json/")) {
            url = AppConfigs.ASSET_ROOT_FOLDER + separator + url;
            if (!url.endsWith(".json"))
                url = url + ".json";
        } else if (url.startsWith("galleries/") || url.startsWith("/galleries/")) {
            url = AppConfigs.ASSET_ROOT_FOLDER + separator + url;
        } else if (url.startsWith(AppConfigs.APPS_FOLDER + File.separator) || url.startsWith(File.separator + AppConfigs.APPS_FOLDER + File.separator)) {
            url = FileUtility.getFilePath(url);
        } else if (url.startsWith("/games/") || url.startsWith("games/")
                || url.startsWith("/layout/") || url.startsWith("layout/")
                || url.startsWith("/menu/") || url.startsWith("menu/")
        ) { //images
            url = FileUtility.getFilePath(AppConfigs.APPS_FOLDER  + File.separator + url);
        } else if (url.startsWith("downloads/") || url.startsWith("/downloads/")
                || url.startsWith("cache/") || url.startsWith("/cache/")
                || url.startsWith(".") || url.startsWith("./")) {
            url = Constants.PROTOCOL_FILE + File.separator + CacheManager.getCacheFolder() + separator + url.replace("downloads", "");
        } else if (url.startsWith("images/") || url.startsWith("/images/")
                || url.startsWith("/videos/") || url.startsWith("videos/")
                || url.startsWith("/pdfs/") || url.startsWith("pdfs/")
                || url.startsWith("/htmls/") || url.startsWith("htmls/")
        ) { //images
            url = FileUtility.getFilePath(File.separator + url);
        } else if (url.startsWith("/storage/") || url.startsWith("storage/")) {
            url = Constants.PROTOCOL_FILE + separator + url;
        } else if (!url.contains(File.separator) && !url.contains(".")) {
            url = FileUtility.getFilePath(AppConfigs.APPS_FOLDER  + File.separator + url);
        } else
            url = Constants.PROTOCOL_HTTPS + url;

        if (!url.contains(".") && !url.startsWith(Constants.PROTOCOL_HTTPS))
            url = url + File.separator + AppConfigs.WEB_INDEX;

        return url + postFix;
    }
}
