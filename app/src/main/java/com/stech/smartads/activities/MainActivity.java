package com.stech.smartads.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.core.AppData;
import com.stech.smartads.interfaces.IModelListener;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.utils.PacketUtility;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.models.Schedule;
import com.stech.smartads.components.audio.Audio;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.config.SocketConfigs;
import com.stech.smartads.utils.ParseUtility;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.components.socket.SocketListener;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.components.DownloadService;
import com.stech.smartads.utils.LocalBroadCastUtil;
import com.stech.smartads.components.audio.AudioService;
import com.stech.smartads.components.SoftInputAssist;
import com.stech.smartads.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends BaseActivity implements SocketListener {

    private Socket socket;
    AlarmManager alarmManager;
    PendingIntent alarmPendingIntent;

    private boolean isReconnectSocket = false;
    private boolean isCallingScheduleAPI = false;

    //music service
    private Intent musicServiceIntent;

    private TextView labelTime, labelDeviceName;

    List<LayoutFrameObj> layouts;

    protected SoftInputAssist createSoftInputAssist() {
        return null; //return new SoftInputAssist(this);
    }

    //connect to the service
    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            AudioService.MusicBinder binder = (AudioService.MusicBinder) service;
            //get service
            ((MainApplication) getApplication()).setMusicService(binder.getService());
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            ((MainApplication) getApplication()).setMusicService(null);
        }
    };

    //broadcast

    private BroadcastReceiver broadcastReceiverShowTimer = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showCurrentTime();
        }
    };

    private BroadcastReceiver broadcastGotoDefaultScreen = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gotoSettingScreen();
        }
    };

    private BroadcastReceiver broadcastGotoSettingScreen = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            gotoSettingScreen();
        }
    };

    private BroadcastReceiver broadcastReceiverResetApp = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            reloadApp();
        }
    };

    private BroadcastReceiver broadcastReceiverCallRefreshSchedule = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //CommonUtil.log(TAG, "broadcastReceiverCallRefreshSchedule@getSchedules");
            getSchedules();
        }
    };

    private BroadcastReceiver showDefaultScreenBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (!NetworkUtility.getInstance(self).isOnline()) {
                if (!AppConfigs.OFFLINE_ENABLED) {
                    gotoSettingScreen();
                }
            }
        }
    };

    private BroadcastReceiver reloadNewLayoutBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            MainActivity.this.getLayoutSetting();
        }
    };

    private BroadcastReceiver downloadFileBroadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (self == null) return;

                Bundle bundle = intent.getExtras();
                if (bundle != null) {

                    final String fileUrl = bundle.getString(Constants.PARAM_FILEURL);

                    if (fileUrl != null && !fileUrl.isEmpty()) {

                        //check status not done
                        String status = bundle.getString(Constants.PARAM_STATUS);

                        if (status == null) return;

                        if (status.equals(DownloadService.STATUS_ERROR_SERVER)) { // if download get error (server die or stop)
                            //CommonUtil.log(TAG, "-- Download File fail - server no response !");
                            return;
                        } else if (status.equals(DownloadService.STATUS_FILE_EXISTED)) {
                            //CommonUtil.log(TAG, "-- Download File Broadcast File is existed");
                        }

                        int size = AppData.getInstance().getArrResourceFilePaths().size();
                        int currentFilePosition = -1;
                        String resourceFile = "";
                        for (int i = 0; i < size; i++) {

                            resourceFile = AppData.getInstance().getArrResourceFilePaths().get(i);

                            if (resourceFile != null && !resourceFile.isEmpty() && fileUrl.equals(resourceFile)) {
                                currentFilePosition = i;
                                break;
                            }
                        }

                        int nextFilePosition = currentFilePosition + 1;
                        //check
                        if (nextFilePosition < size) {

                            String nextFileUrl = AppData.getInstance().getArrResourceFilePaths().get(nextFilePosition);

                            //handle to download
                            MainActivity.this.callCheckDownloadFile(nextFileUrl);

                            //CommonUtil.log(TAG, "Download File Broadcast File #" + nextFilePosition + ": " + nextFileUrl);
                        }
                    }

                }

            } catch (Exception ex) {
                CommonUtil.error(self, ex);
            }
        }
    };


    private boolean isLoadedInTheFirstTime = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            //set screen on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //restart time to calling his data
            MainApplication.counterCallingHisData = 30;

            final View decorView = getDecorView();

            //hide NavigationBar
            getDecorView().setSystemUiVisibility(uiOptions);
            AppData.getInstance().setScreenHeight(decorView.getHeight());
            AppData.getInstance().setScreenWidth(decorView.getWidth());

            //CommonUtil.log(TAG, "original height: " + decorView.getHeight());
            decorView.setOnSystemUiVisibilityChangeListener(
                    new View.OnSystemUiVisibilityChangeListener() {
                        @Override
                        public void onSystemUiVisibilityChange(int i) {
                            AppData.getInstance().setScreenHeight(decorView.getHeight());
                            AppData.getInstance().setScreenWidth(decorView.getWidth());
                            createLayout(true, "");
                            isLoadedInTheFirstTime = true;
                        }
                    });

            //init Alarm manager
            alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

            //process to download files
            handleDownloadFile();

            //create audio service intent
            initAudioServiceIntent();

// Fix auto-resize keyboard issue ?
//            final FrameLayout mainLayout = getFrameLayout();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                ViewCompat.setOnApplyWindowInsetsListener(mainLayout , new OnApplyWindowInsetsListener() {
//                    @Override
//                    public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
//                        v.setPadding(0, 0, 0, insets.getSystemWindowInsetBottom());
//                        return insets;
//                    }
//                });
//            } else {
//                final View contentView = mainLayout;
//                decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                    @Override
//                    public void onGlobalLayout() {
//                        Rect r = new Rect();
//                        //r will be populated with the coordinates of your view that area still visible.
//                        decorView.getWindowVisibleDisplayFrame(r);
//
//                        //get screen height and calculate the difference with the useable area from the r
//                        int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
//                        int diff = height - r.bottom;
//
//                        //if it could be a keyboard add the padding to the view
//                        if (diff != 0) {
//                            // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
//                            //check if the padding is 0 (if yes set the padding for the keyboard)
//                            if (contentView.getPaddingBottom() != diff) {
//                                //set the padding of the contentView for the keyboard
//                                contentView.setPadding(0, 0, 0, diff);
//                            }
//                        } else {
//                            //check if the padding is != 0 (if yes reset the padding)
//                            if (contentView.getPaddingBottom() != 0) {
//                                //reset the padding of the contentView
//                                contentView.setPadding(0, 0, 0, 0);
//                            }
//                        }
//                    }
//                });
//            }

        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }

    }

    @Override
    protected void inflateLayout() {
        // getLayoutInflater().inflate(R.layout.activity_main, mFrlMain);
    }

    @Override
    void initUI() {

    }

    @Override
    void initControl() {
        AppData.getInstance().setCurrentSchedule(AppData.getInstance().getCurrentSchedule(self)); //
    }


    @Override
    void getExtraValues() {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }


    @Override
    protected void onResume() {
        try {
            super.onResume();

            if (isLoadedInTheFirstTime) {
                createLayout(true, "");
            }
            //connect socket server
            connectSocketIO();

            //set main activity to Application
            ((MainApplication) getApplicationContext()).setMainActivity(this);

            //binding to audio service
            bindToAudioService();

            //register broadcast
            LocalBroadCastUtil.registerBroadCast(this, downloadFileBroadCastReceiver, LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED);
            LocalBroadCastUtil.registerBroadCast(this, reloadNewLayoutBroadCastReceiver, LocalBroadCastUtil.ACTION_REFRESH_LAYOUT);
            LocalBroadCastUtil.registerBroadCast(this, broadcastReceiverShowTimer, LocalBroadCastUtil.ACTION_SHOW_CURRENT_TIME);
            LocalBroadCastUtil.registerBroadCast(this, broadcastGotoSettingScreen, LocalBroadCastUtil.ACTION_LOAD_SETTING_SCREEN);
            LocalBroadCastUtil.registerBroadCast(this, broadcastGotoDefaultScreen, LocalBroadCastUtil.ACTION_LOAD_DEFAULT_SCREEN);

            LocalBroadCastUtil.registerBroadCast(this, broadcastReceiverResetApp, LocalBroadCastUtil.ACTION_RELOAD_APP);
            LocalBroadCastUtil.registerBroadCast(this, broadcastReceiverCallRefreshSchedule, LocalBroadCastUtil.ACTION_CALL_REFRESH_SCHEDULE);
            //register broadcast network
            registerReceiver(showDefaultScreenBroadCastReceiver, new IntentFilter(LocalBroadCastUtil.ACTION_SYSTEM_NETWORK_CHANGED));

        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    protected void onPause() {
        try {
            if (socket != null) {
                socket.close();
            }

            //clear alarm
            if (alarmManager != null && alarmPendingIntent != null) {
                alarmManager.cancel(alarmPendingIntent);
            }

            //unbind to service
            unBindToAudioService();

            //un-register broadcast show default image
            unregisterReceiver(showDefaultScreenBroadCastReceiver);

            super.onPause();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    protected void onUserLeaveHint() {
        //super.onUserLeaveHint();
//        Intent startMain = new Intent(Intent.ACTION_MAIN);
//        startMain.addCategory(Intent.CATEGORY_HOME);
//        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        this.startActivity(startMain);
        //close this task

        this.finish();

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    @Override
    public void finish() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                super.finishAndRemoveTask();
            } else {
                super.finish();
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }


    @Override
    protected void onDestroy() {
        try {
            //remove all global value
            AppData.getInstance().cleanup();

            ((MainApplication) getApplication()).setMainActivity(null);

            //clear socket
            if (socket != null) {
                socket.disconnect();
                socket = null;
            }

            //stop all broad cast
            LocalBroadCastUtil.unRegisterBroadCast(this, broadcastReceiverResetApp);
            LocalBroadCastUtil.unRegisterBroadCast(this, broadcastReceiverShowTimer);
            LocalBroadCastUtil.unRegisterBroadCast(this, downloadFileBroadCastReceiver);
            LocalBroadCastUtil.unRegisterBroadCast(this, reloadNewLayoutBroadCastReceiver);
            LocalBroadCastUtil.unRegisterBroadCast(this, broadcastReceiverCallRefreshSchedule);

            LocalBroadCastUtil.unRegisterBroadCast(this, broadcastGotoDefaultScreen);
            LocalBroadCastUtil.unRegisterBroadCast(this, broadcastGotoSettingScreen);

            //stop service
            if (musicServiceIntent != null) {
                stopService(musicServiceIntent);
            }
            super.onDestroy();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    public void reloadApp() {
        CommonUtil.reloadApp(this);
    }

    //get current layout theme
    public void getLayoutSetting() {
        try {
            Schedule currentSchedule = AppData.getInstance().getCurrentSchedule();
            if (currentSchedule != null && DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.MILLISECOND) > currentSchedule.getFinishTime()) {
                if (AppData.getInstance().isNextScheduleAvailable()) {
                    AppData.getInstance().setCurrentSchedule(AppData.getInstance().getCurrentSchedule(self));
                    createLayout(true, Constants.TEXT_MOVE_TO_NEXT_SCHEDULE);
                } else {
                    CommonUtil.log(TAG, "getLayoutSetting @getSchedules isNextScheduleAvailable");
                    showMessage( Constants.TEXT_UPDATE_NEW_SCHEDULE);
                    getSchedules();
                }
            } else {
                CommonUtil.log(TAG, "getLayoutSetting @getSchedules isNextScheduleAvailable NOT");
                getSchedules();
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }

    }

    private void getSchedules() {
        try {
            if (isCallingScheduleAPI) return;

            isCallingScheduleAPI = true;

            //get schedul
            AppData().getSchedules(new IModelListener() {
                @Override
                public void onSuccess(Object obj) {
                    String json = ParseUtility.parseJsonFromObject(obj);
                    //String cachedJson = CacheManager.getCacheSchedulesJson(self);
                    //showMessage( "Tải kịch bản !! " + json);
                    if (!json.isEmpty()) {
                        Schedule currentSchedule = AppData.getInstance().getCurrentSchedule();
                        createLayout(true, DateTimeUtil.getScheduleTimeDisplay(currentSchedule));
                        handleDownloadFile();
                    }

                    isCallingScheduleAPI = false;

                }

                @Override
                public void onError(Throwable ex) {
                    showMessage(  Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_SCHEDULE + " " + Constants.TEXT_ERROR + ": " + StringUtil.getErrorMessage(ex));
                    onSuccess(null);
                    isCallingScheduleAPI = false;
                }
            });

            isCallingScheduleAPI = false;

        } catch (Exception ex) {
            /*ignore*/
            CommonUtil.error(self, ex);
            isCallingScheduleAPI = false;
        }
    }

    public boolean isNeedRefreshLayout(Schedule currentScheduleNew, Schedule currentSchedule) {
        return true;
//        if (currentSchedule == null || currentScheduleNew == null)
//            return true;
//
//        return !currentSchedule.equals(currentScheduleNew);
    }

    void createLayout(Schedule schedule, String message) {
        removeAllViewsFromMainLayout(); //remove allViews
        createScheduleLayout(schedule); //create schedule layout
        createCommonLayout(); //show logos, times etc --> hence must call after createScheduleLayout
        if (!message.isEmpty())
            showMessage( message.isEmpty() ? Constants.TEXT_INIT_SCREEN : (Constants.TEXT_UPDATE_SCREEN +  ": " + message));

        startAudioService(schedule.getAudios());
        setNextTimeRefresh();
    }

    public void createScheduleLayout(Schedule schedule) {
        FrameLayout frame = null;
        FrameLayout.LayoutParams params = null;

        layouts = getLayouts();

        if (schedule == null) {
            //showMessage( "Không tìm được kịch bản để hiển thị"); // Full screen
            layouts.clear();
            LayoutFrameObj layout = new LayoutFrameObj(AppConfigs.HOMEPAGE);
            layouts.add(layout);
        } else {
            layouts = schedule.getFrameLayouts();
            getFrameLayout().setBackgroundColor(Color.parseColor(schedule.getBackground()));
            AppData().setCurrentSchedule(schedule); //assign current schedule !!
        }

        //add new layout
        for (LayoutFrameObj layout : layouts) {
            addFragment(layout);
        }

        // addFragment(0.1, 0.1, 300, 200, "https://freehtml5games.org/icons/duck-shooter.png", Constants.TYPE_IMAGE);
        // addFragment("main", "https://freehtml5games.org/icons/duck-shooter.png", Constants.TYPE_IMAGE);
    }

    public void addFragment(LayoutFrameObj layout) {
        layout.setActivity(self);

        FrameLayout frame = null;
        FrameLayout.LayoutParams params = null;
        frame = new FrameLayout(self);
        frame.setTag(layout.getName());
        frame.setId(layout.getId());

        //set background
        frame.setBackgroundColor(Color.parseColor(layout.getBackground()));
        //set fragment content
        addFragment(frame, layout.getFragmentContent());

        params = new FrameLayout.LayoutParams(layout.getWidth(self), layout.getHeight(self));//width and height
        params.leftMargin = layout.getLeft(self);
        params.topMargin = layout.getTop(self);

        int screenHeight = (int) (AppData().getScreenHeight() * 0.99);
        int screenWidth = (int) (AppData().getScreenWidth() * 0.99);
        int frameBorder = AppData().getServerSetting().getFrameBorder();
        String frameBorderColor = AppData().getServerSetting().getFrameBorderColor();
        // frame.setBackgroundColor(Color.parseColor(frameBorderColor));

        if (params.leftMargin + params.width < screenWidth) {
            params.width -= frameBorder;
            //showMessage(String.valueOf(params.leftMargin) + " : " + String.valueOf(params.leftMargin + params.width) + ":" + String.valueOf(screenWidth));
        }

        if (params.topMargin + params.height < screenHeight) {
            params.height -= frameBorder;
            //showMessage(String.valueOf(params.topMargin) + " : " + String.valueOf(params.topMargin + params.height) + ":" + String.valueOf(screenHeight));
        }

//        if (params.leftMargin > 0) {
//            params.leftMargin += frameBorder;
//        }

//        if (params.topMargin > 0) {
//            params.topMargin += frameBorder;
//        }

        getFrameLayout().addView(frame, params);
        layout.setFrame(frame);
    }

    void createLayout(String message) {
        //showMessage( "--> create Layout 1: " + message);
        createLayout(true, message);
    }

    // init layout to main frame
    void createLayout(final boolean isRefreshLayer, String message) {
        try {
            if (!isRefreshLayer)
                return;

            getDecorView().setSystemUiVisibility(uiOptions);

            Schedule schedule = null;
            Schedule currentSchedule = AppData().getCurrentSchedule();

            //if showing special view rather than main homepage
            if (!AppData().isAPIMode()) {
                AppData.getInstance().cleanup();
                schedule = ParseUtility.getDefaultScheduleFromFile(self); // always show 1 web homepage
            }

            //1, try to set current schedule from schedules
            if (schedule == null && AppData.getInstance().getSchedules().size() > 0) {
                schedule = AppData.getInstance().getCurrentSchedule(self);
            }

            if (schedule == null) {
                //switch to default layout
                schedule = AppData.getInstance().getDefaultSchedule();
            }

            //2. switch to default layout embeded in device
            if (schedule == null) {
                schedule = ParseUtility.getDefaultSchedule(self);
            }

            //3, if no current schedule -> try to get from cached data (offline)
            if (schedule == null) {
                //switch to offline data
                NetworkUtility.getInstance(self).setOffline(true);
                AppData().getOfflineDataAndSwitchToOfflineMode(self);
                schedule = AppData.getInstance().getCurrentSchedule(self);
            }

            if (schedule != null) { // if next schedule is available
                if (isNeedRefreshLayout(currentSchedule, schedule))
                    createLayout(schedule, message);
            }

        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    public List<LayoutFrameObj> getLayouts() {
        if (layouts == null)
            layouts = new ArrayList<LayoutFrameObj>();
        return layouts;
    }




    public LayoutFrameObj getLayoutByName(String frameName) {
        for (LayoutFrameObj layout : layouts) {
            if (layout.getName().equalsIgnoreCase(frameName))
                return layout;
        }
        return null;
    }

    public void addFragment(String frameId, DataContentObj content) {
        LayoutFrameObj layout = getLayoutByName(frameId);
        if (layout == null)
            return;
        layout.addContent(content);
        addFragment(layout);
    }

    public void addFragment(String frameId, List<DataContentObj> content) {
        LayoutFrameObj layout = getLayoutByName(frameId);
        if (layout == null)
            return;
        layout.addContent(content);
        addFragment(layout);
    }

    public void addFragment(String frameId, String content, String contentType) {
        LayoutFrameObj layout = getLayoutByName(frameId);
        if (layout == null)
            return;
        DataContentObj contentObj = new DataContentObj(content, contentType);
        layout.addContent(contentObj);
        addFragment(layout);
    }

    public void addFragment(double x, double y, double w, double h, List<DataContentObj> content) {
        addFragment(x, y, w, h, content, "");
    }

    public void addFragment(double x, double y, double w, double h, List<DataContentObj> content, String name) {
        LayoutFrameObj layout = new LayoutFrameObj();
        layout.setDataContentType(Constants.TYPE_SLIDE);
        layout.setWidth(w);
        layout.setHeight(h);
        layout.setLeft(x);
        layout.setTop(y);
        layout.addContent(content);
        layout.setId(CommonUtil.getRandom(100, 999));

        layout.setBackground("#ffffff");
        layout.setName(name);

        addFragment(layout);
    }

    public void addFragment(double x, double y, double w, double h, DataContentObj contentObj) {
        List<DataContentObj> arr = new ArrayList<DataContentObj>();
        arr.add(contentObj);
        addFragment(x, y, w, h, arr);
    }

    public void addFragment(double x, double y, double w, double h, String content, String contentType) {
        DataContentObj contentObj = new DataContentObj(content, contentType);
        addFragment(x, y, w, h, contentObj);
    }

    public void createCommonLayout() {
        //add device name
        if (labelDeviceName == null) {
            labelDeviceName = new TextView(self);
        }

        //set background
        labelDeviceName.setBackgroundColor(Color.parseColor("#ffffff"));
        //set fragment content

        labelDeviceName.setTextColor(Color.parseColor("#00000000"));
        labelDeviceName.setTextSize(AppConfigs.SHOW_DEVICE_NAME_FONT_SIZE);

        getFrameLayout().addView(labelDeviceName, CommonUtil.getLayoutParams(self, "bottom_right"));

        //add current time
        if (labelTime == null) {
            labelTime = new TextView(self);
        }

        labelTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AppData().getCurrentUrl().equals("clock")) {
                    goBack();
                    return;
                }
            }
        });

        labelTime.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (AppData().getCurrentUrl().equals("clock")) {
                    goBack();
                    return false;
                }
                setTimeOut(0);
                gotoWebScreen("clock");
                return false;
            }
        });

        //set background
        labelTime.setBackgroundColor(Color.parseColor("#00000000"));
        labelTime.setTextSize(AppConfigs.SHOW_TIMER_FONT_SIZE);
        labelTime.setAlpha(AppConfigs.SHOW_TIMER_FONT_OPACITY);

        getFrameLayout().addView(labelTime, CommonUtil.getLayoutParams(self, "top_right"));

        //add app logo in top - left screen

        initLogo();
    }

    //set alarm in next time
    private void setNextTimeRefresh() {
        try {
            long scheduleTime = 0;
            long scheduleTimeFinished = 0;
            boolean needRefresh = false;

            //check next schedule
            Schedule nextSchedule = AppData.getInstance().getNextSchedule(self);
            Schedule currentSchedule = AppData.getInstance().getCurrentSchedule(self);

            if (nextSchedule == null || nextSchedule.getStartTime() == 0) {
                //CommonUtil.log(TAG, "next schedule is not available");
                if (AppData.getInstance().getCurrentSchedule() == null || AppData.getInstance().getCurrentSchedule().isDefault()) {
                    scheduleTime = DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.MILLISECOND) + (60 * 1000);
                } else {
                    scheduleTime = DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.MILLISECOND) + (AppData.getInstance().getCurrentSchedule().getDuration() * 60 * 1000);
                    AppData.getInstance().getCurrentSchedule().setFinishTime(scheduleTime);
                }
            } else {
                if (nextSchedule != null) {
                    scheduleTime = nextSchedule.getStartTime();
                    scheduleTimeFinished = nextSchedule.getFinishTime();
                }
                needRefresh = true;
            }

            if (needRefresh && scheduleTime != 0) {
                //set time to request new layout
                long delayTime = scheduleTime - DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.MILLISECOND);

                if (delayTime > 0) {
                    showMessage( Constants.TEXT_SCHEDULE_TIME + ": " + DateTimeUtil.getScheduleTimeDisplay(currentSchedule) + ". Tiếp theo: " + DateTimeUtil.getScheduleTimeDisplay(nextSchedule));
                    ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            LocalBroadCastUtil.sendBroadcastListener(self, LocalBroadCastUtil.ACTION_REFRESH_LAYOUT);
                        }
                    }, delayTime);
                }
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    private String getWarningInfo() {
        if (AppConfigs.LICENSE_KEY.equalsIgnoreCase("DEMO"))
            return "DEMO LICENSE";
        if (AppConfigs.isDebug)
            return AppData().getAppMode() + " " + AppData().getCurrentUrl() + " " + CacheManager.getAppMode(self);
        else
            return CacheManager.getSettingDeviceName(this) + " " + Constants.TEXT_INVALID_LICENSE + ". Contact " + AppConfigs.AUTHOR;
    }

    public void showCurrentTime() {
        try {
            //display current time
            if (labelTime != null) {
                if (NetworkUtility.getInstance(self).isOnline()) {
                    if (NetworkUtility.getInstance(self).isOffline())
                        labelTime.setTextColor(Color.parseColor("#F9D71C"));
                    else
                        labelTime.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    labelTime.setTextColor(Color.parseColor("#ff0000"));
                }
                //labelTime.setText(DateTimeUtil.convertTimeStampToDate(DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.MILLISECOND), "dd/MM/yyyy HH:mm"));
                labelTime.setText(DateTimeUtil.showCurrentTime());
            }
            if (labelDeviceName != null && !AppData().getIsLicense()) {
                labelDeviceName.setTextColor(Color.parseColor("#ff0000"));
                labelDeviceName.setAlpha(1.0f);
                labelDeviceName.setTextSize(AppConfigs.SHOW_TIMER_FONT_SIZE);
                labelDeviceName.setText("   " + getWarningInfo() + "   ");
            }

        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    //create socket connect
    private void connectSocketIO() {
        if (SocketConfigs.isEnabled) {
            try {
                IO.Options opts = new IO.Options();
                opts.forceNew = true;
                opts.reconnection = false;
                String socketServer = AppData().getSocketAddressIp();
                showMessage( "Socket server [" + socketServer + "]: " + Constants.TEXT_CONNECTING);
                socket = IO.socket(socketServer);

                socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        showMessage( "Socket server: " + Constants.TEXT_CONNECTED);

                        //send message confirm correct device to server
                        if (socket != null) {
                            socket.emit("foo", "hi");
                        } else {
                            connectSocketIO();
                        }


                    }

                }).on(SocketConfigs.SOCKET_CHANNEL, new Emitter.Listener() { //notification : customize listener channel

                    @Override
                    public void call(Object... args) {
                        final String obj = (String) args[0];
                        getResponse(obj);
                    }

                }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        showMessage( "Socket server: " + Constants.TEXT_DISCONNECTED);

                        //reconnect socket
                        reConnectSocketIO();
                    }

                }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        showMessage( "Socket server: " + Constants.TEXT_DISCONNECTED + ". " + Constants.TEXT_TRY_AGAIN);

                        //reconnect socket
                        if (!isReconnectSocket) {
                            ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    reConnectSocketIO();
                                }
                            }, 10 * 1000);
                        }


                    }

                }).on(Socket.EVENT_CONNECT_TIMEOUT, new Emitter.Listener() {

                    @Override
                    public void call(Object... args) {
                        showMessage( "Socket server " + Constants.TEXT_ERROR + ". " + Constants.TEXT_TRY_AGAIN);

                        if (!isReconnectSocket) {
                            ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //reconnect socket
                                    reConnectSocketIO();
                                }
                            }, 10 * 1000);
                        }


                    }

                });

                socket.connect();
            } catch (URISyntaxException e) {
                showMessage( "Socket server " + Constants.TEXT_ERROR + ". " + Constants.TEXT_TRY_AGAIN);

                //reconnect socket
                reConnectSocketIO();
            }
        }
    }

    private void reConnectSocketIO() {
        try {
            if (SocketConfigs.isEnabled) {
                if (isReconnectSocket) return;

                isReconnectSocket = true;

                if (socket != null) {
                    ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (socket != null) {
                                socket.connect();
                                isReconnectSocket = false;
                            }
                        }
                    }, 10 * 1000);

                } else {
                    connectSocketIO();
                }
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    public void getResponse(String response) { //get Socket Response
        // Parse data

        CommonUtil.error(TAG, "getSocketResponse: " + response);

        if (response != null && !response.isEmpty()) {
            try { //{"device":"all or specified device id","action":" ","frame":""}
                JSONObject jsonWrapper = new JSONObject(response);

                //check device
                String devices = jsonWrapper.getString("device");
                if (devices.equalsIgnoreCase("all") || devices.contains(PacketUtility.getDeviceUniqueID(self))) {
                    //check frames
                    String action = jsonWrapper.getString("action");
                    String scheduleId = jsonWrapper.isNull("schedule_id") ? "" : jsonWrapper.getString("schedule_id");
                    String channelId = jsonWrapper.isNull("channel_id") ? "" : jsonWrapper.getString("channel_id");
                    switch (action) {
                        case "refreshSchedule":
                            showMessage( Constants.TEXT_SERVER + " " + Constants.TEXT_REQUEST + " " + Constants.TEXT_DOWNLOAD);

                            getSchedules();
                            break;

                        case "refreshScheduleNow":
                            showMessage( Constants.TEXT_SERVER + " " + Constants.TEXT_REQUEST + " " + Constants.TEXT_DOWNLOAD);
                            getSchedules();
                            break;


                        case "updateExistedFrame":

                            JSONObject frameJson = jsonWrapper.getJSONObject("frame");
                            updateFrameContent(frameJson);
                            break;
                    }
                }

            } catch (Exception ex) {
                CommonUtil.error(self, ex);
            }
        }
    }


    //update frame
    private void updateFrameContent(JSONObject frameJson) {
        try {
            LayoutFrameObj frame = new LayoutFrameObj(frameJson);
            //check if parse frame un-successfully
            if (frame.getId() == 0) {
                return;
            }
            //update data content
            int size = AppData.getInstance().getCurrentSchedule().getFrameLayouts().size();
            for (int i = 0; i < size; i++) {
                LayoutFrameObj currentFrame = AppData.getInstance().getCurrentSchedule().getFrameLayouts().get(i);
                if (currentFrame.getId() == frame.getId()) {
                    AppData.getInstance().getCurrentSchedule().getFrameLayouts().add(i, frame);
                    break;
                }
            }
            //update layout
            FrameLayout frameLayout = (FrameLayout) getFrameLayout().findViewById(frame.getId());
            if (frameLayout != null) {
                addFragment(frameLayout, frame.getFragmentContent());
            }
        } catch (Exception ex) {
            /* ignore */
            CommonUtil.error(self, ex);
        }
    }

    // HUNG: Fix issue IllegalStateException: Can not perform this action after onSaveInstanceState with ViewPager
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        try {
            outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
            super.onSaveInstanceState(outState);
        } catch (Exception ex)  {
            CommonUtil.error(self, ex);
        }
    }

    //download files
    private void handleDownloadFile() {
        try {
            //delete file in cache folder if file is not in resource file list
            List<File> cacheFiles = CacheManager.getCacheListFile();

            if (cacheFiles != null) {
                if (cacheFiles.size() > 0) {

                    boolean isExisted = false;
                    for (File file : cacheFiles) {
                        if (file == null)
                            continue;

                        //set false when start
                        isExisted = false;
                        for (String fileUrl : AppData.getInstance().getArrResourceFilePaths()) {
                            if (fileUrl != null && fileUrl.toLowerCase().contains(file.getName().toLowerCase())) {
                                isExisted = true;
                                break;
                            }
                        }

                        if (!isExisted) {
                            file.deleteOnExit();
                        }
                    }
                }
            }

            //if downloading is processing -> cancel new request download
            if (DownloadService.isDownloading) return;

            long currentTime = DateTimeUtil.getCurrentTime(((MainApplication) getApplication()).getCurrentCalendar(), DateTimeUtil.MILLISECOND);
            if (currentTime < AppData.getInstance().getDownloadResourceTime()) {

                ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (AppData.getInstance().getArrResourceFilePaths().size() > 0) {
                            callCheckDownloadFile(AppData.getInstance().getArrResourceFilePaths().get(0));
                        }
                    }
                }, AppData.getInstance().getDownloadResourceTime() - currentTime);

            } else {
                if (AppData.getInstance().getArrResourceFilePaths().size() > 0) {
                    callCheckDownloadFile(AppData.getInstance().getArrResourceFilePaths().get(0));
                }
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    private void callCheckDownloadFile(final String fileUrl) {
        try {
            final Context context = AppData().getContext();

            if (DownloadService.isDownloading) return;

            //check file is able to download or not
            boolean isAbleDownloadFile = CacheManager.isAbleDownloadFile(context, fileUrl);

            if (!isAbleDownloadFile) {
                //send notification to app
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PARAM_FILEURL, fileUrl);
                bundle.putString(Constants.PARAM_STATUS, DownloadService.STATUS_FILE_EXISTED);
                LocalBroadCastUtil.sendBroadcastListener(context, LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED, bundle);

            } else {

                //check server is ready for download
                AppData.getInstance(context).downloadFile(fileUrl, false, new IModelListener() {
                    @Override
                    public void onSuccess(Object obj) {

                        try {
                            JSONObject jsonObject = new JSONObject(obj.toString());

                            if (!jsonObject.isNull("can_download")) {

                                boolean isAbleDownload = jsonObject.getBoolean("can_download");

                                if (isAbleDownload) {
                                    CacheManager.downloadFile(context, fileUrl, 0);
                                } else {
                                    long downloadTime = jsonObject.getLong("download_time") * 1000;
                                    long currentTime = Calendar.getInstance().getTimeInMillis();

                                    if (downloadTime <= currentTime) { // check download <= current time
                                        ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                CacheManager.downloadFile(context, fileUrl, 0);
                                            }
                                        }, 10000);

                                    } else {
                                        ((MainApplication) getApplication()).getAppHandler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                CacheManager.downloadFile(context, fileUrl, 0);
                                            }
                                        }, downloadTime - currentTime);
                                    }

                                }
                            } else { // file is not found -> process to next file
                                //send notification to app
                                Bundle bundle = new Bundle();
                                bundle.putString(Constants.PARAM_FILEURL, fileUrl);
                                bundle.putString(Constants.PARAM_STATUS, DownloadService.STATUS_ERROR_SERVER);
                                LocalBroadCastUtil.sendBroadcastListener(context, LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED, bundle);
                            }
                        } catch (JSONException e) {
                            CommonUtil.error(e);

                            //error -> process to next file
                            Bundle bundle = new Bundle();
                            bundle.putString(Constants.PARAM_FILEURL, fileUrl);
                            bundle.putString(Constants.PARAM_STATUS, DownloadService.STATUS_ERROR_SERVER);
                            LocalBroadCastUtil.sendBroadcastListener(context, LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED, bundle);

                        }

                    }

                    @Override
                    public void onError(Throwable ex) {
                        //error connect to server -> stop download queue
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.PARAM_FILEURL, fileUrl);
                        bundle.putString(Constants.PARAM_STATUS, DownloadService.STATUS_ERROR_SERVER);
                        LocalBroadCastUtil.sendBroadcastListener(context, LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED, bundle);
                    }
                });
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        try {
            super.onConfigurationChanged(newConfig);
            getDecorView().setSystemUiVisibility(uiOptions);
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }



    //*********************************************************************************************
    //Audio service
    private void initAudioServiceIntent() {
        if (musicServiceIntent == null) {
            musicServiceIntent = new Intent(getApplicationContext(), AudioService.class);
            startService(musicServiceIntent);
        }
    }

    //bind to service
    private void bindToAudioService() {
        initAudioServiceIntent();
        bindService(musicServiceIntent, musicConnection, Context.BIND_AUTO_CREATE);
    }

    //unbind to service
    private void unBindToAudioService() {
        unbindService(musicConnection);
    }

    //start Service
    public void startAudioService(List<Audio> audios) {
        ((MainApplication) getApplication()).startAudioService(audios);
    }

    protected boolean checkLicense() {
        return false;
    }

    protected void timeOutAction() {
        timeCounter += 1;
        if (AppData().isWebsiteMode()) {
            goBack();
        }
    }

    @Override
    public void switchAppMode(final String mode) {
        super.switchAppMode(mode);
        if (mode.equalsIgnoreCase(Constants.APP_MODE_API)) {
            AppData().getSchedules(new IModelListener() {
                @Override
                public void onSuccess(Object obj) {
                    createLayout(Constants.APP_MODE_API);
                }

                @Override
                public void onError(Throwable x) {
                    createLayout(Constants.APP_MODE_API);
                }
            });
            return;
        }
        createLayout(mode);
    }

    public void gotoWebScreen(String url) {
        super.gotoWebScreen(url);
        createLayout(url);
    }

    public void gotoWebScreen(String url, int timeout) {
        super.gotoWebScreen(url, timeout);
        createLayout(url);
    }

    @Override
    public void goBack() {
        super.goBack();
    }

}
