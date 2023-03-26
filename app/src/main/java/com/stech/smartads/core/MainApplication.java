package com.stech.smartads.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.config.Constants;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;
import com.stech.smartads.utils.MusicService;

import java.util.Calendar;


public class MainApplication extends Application {
    private Activity mainActivity = null;
    private MusicService musicService = null;
    private static Gson gson;
    private Calendar currentCalendar;
    private Handler handler;

    public static int counterCallingHisData = 10;

    private int resetAppCounter = 0;
    private int currentCheckConnectionCount = 0;
    private int currentCheckLicenseCount = 0;
    private int currentCheckGetSchedules = 0;

    private Runnable timeRunnable = new Runnable() {
        @Override
        public void run() {
        try {
            int mainTimer = AppData.getInstance().getServerSetting().getMainTimer();
            long refreshScheduleTimer = AppData.getInstance().getServerSetting().getRefreshDataTime();

            //Check network
            NetworkUtility.getInstance(getApplicationContext()).checkNetwork();

            //Internal Calendar & Timer
            if (currentCalendar != null) {
                currentCalendar.add(Calendar.SECOND, mainTimer);
            }
            // show Time in Main activity
            LocalBroadCastUtil.sendBroadcastListener(MainApplication.this, LocalBroadCastUtil.ACTION_SHOW_CURRENT_TIME);


            //refresh App
            resetAppCounter += mainTimer;
            int refreshAppTime = AppData.getInstance().getServerSetting().getRefreshAppTime();

            if (refreshAppTime > 0 && resetAppCounter >= refreshAppTime) {
                //  if(!isVideoPlaying) {
                LocalBroadCastUtil.sendBroadcastListener(MainApplication.this, LocalBroadCastUtil.ACTION_RELOAD_APP);
                resetAppCounter = 0;
                //  }
            }

            // check license
            if (currentCheckLicenseCount >= AppConfigs.CHECK_LICENSE_TIMER) {
                if (!AppData.getInstance().getIsLicense()) {
                    LocalBroadCastUtil.sendBroadcastListener(MainApplication.this, LocalBroadCastUtil.ACTION_LOAD_SETTING_SCREEN);
                }
                currentCheckLicenseCount = 0;
            } else {
                currentCheckLicenseCount += mainTimer;
            }

            //call api check connect each of 3 minutes
            if (currentCheckConnectionCount >= AppConfigs.API_CHECK_STATUS_DEVICE_TIMER) {
                AppData().checkConnection(null);
                currentCheckConnectionCount = 0;
            } else {
                currentCheckConnectionCount += mainTimer;
            }

            //call check memory used
            if (counterTimeForCheckingMemory >= AppConfigs.CHECK_MEMORY_DEVICE_TIME) {
                ActivityManager.MemoryInfo memoryInfo = getAvailableMemory();
                if (memoryInfo != null) {
                    totalMemory = memoryInfo.totalMem / (1024 * 1024);
                    usedMemInMB = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024);
                    if (memoryInfo.lowMemory) {
                        CommonUtil.message(null, "LOW MEMORY: " + usedMemInMB + " / " + totalMemory + " used. " + Constants.TEXT_RESTART_APP);
                        //if low memory -> reload app
                        LocalBroadCastUtil.sendBroadcastListener(MainApplication.this, LocalBroadCastUtil.ACTION_RELOAD_APP);
                    }
                }

                counterTimeForCheckingMemory = 0;
            } else {
                counterTimeForCheckingMemory += mainTimer;
            }

            //check counter request his data
            if (counterCallingHisData > 100) {
                counterCallingHisData = 0;
            } else {
                counterCallingHisData += mainTimer;
            }

            //check call refresh schedule at the first second of the minute
            if (currentCheckGetSchedules >= refreshScheduleTimer) {
                LocalBroadCastUtil.sendBroadcastListener(MainApplication.this, LocalBroadCastUtil.ACTION_CALL_REFRESH_SCHEDULE);
                currentCheckGetSchedules = 0;
            } else {
                currentCheckGetSchedules += mainTimer;
            }

            //check handler
            if (handler == null) {
                handler = new Handler(Looper.myLooper());
            }
            handler.postDelayed(this, 1000 * mainTimer); //timer 1 second

        } catch (Exception ex) {
            //CommonUtil.message(getApplicationContext(), ex.getMessage());
            CommonUtil.error(getApplicationContext(), ex);
        }
        }
    };

    private int counterTimeForCheckingMemory = 0;

    private long usedMemInMB = 0;
    private long totalMemory = 0;


    protected ServerSetting Setting() {
        return AppData.getInstance(getApplicationContext()).getServerSetting();
    }
    protected AppData AppData() { return AppData.getInstance(getApplicationContext()); }

    @Override
    public void onCreate() {
        super.onCreate();
        gson = new Gson();

        currentCalendar = Calendar.getInstance();

        handler = new Handler(Looper.myLooper());

        //start time
        handler.post(timeRunnable);
    }

    public Handler getAppHandler() {
        if(handler == null){
            handler = new Handler(Looper.myLooper());
        }
        return handler;
    }

    public static Gson getGson() {
        return gson;
    }

    //manage main activity
    public void setMainActivity(Activity mainActivity )
    {
        this.mainActivity = mainActivity;
    }
    public Activity getMainActivity()
    {
        return mainActivity;
    }

    //manage audio service
    public void setMusicService(MusicService service )
    {
        this.musicService = service;
    }
    public MusicService getMusicService()
    {
        return musicService;
    }
    public boolean isMusicServiceAvailable(){
        return musicService !=null;
    }

    public Calendar getCurrentCalendar() {
        if (currentCalendar == null)
            currentCalendar = Calendar.getInstance();

        return currentCalendar;
    }

    public void setCurrentTime(long currentTime){
        currentCalendar.setTimeInMillis(currentTime);
    }

    public long getUsedMemInMB() {
        return usedMemInMB;
    }

    public long getTotalMemory() {
        return totalMemory;
    }

    // Get a MemoryInfo object for the device's current memory status.
    private ActivityManager.MemoryInfo getAvailableMemory() {
        try {
            ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            return memoryInfo;
        } catch (Exception ex) {
            CommonUtil.error(getApplicationContext(), ex);
            return null;
        }
    }


    ///===================================================================================
    // Video checking
    private boolean isVideoPlaying = false;

    public void setVideoPlaying(boolean videoPlaying) {
        isVideoPlaying = videoPlaying;
    }
}
