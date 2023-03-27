package com.stech.smartads.config;


public class AppConfigs {

    public final static String APP_NAME = "SMARTADS";
    public final static String AUTHOR = "STECH.VN";
    public final static String HOMEPAGE = "https://stech.vn";
    public final static String SOURCE_PACKAGE = "com.stech.smartads";

    public final static String LICENSE_KEY = "DEMO";

    public final static String DEFAULT_CONTENT_APP = "home";

    public final static int CHECK_LICENSE_TIMER = 60*60;

    public final static double SHOW_LOGO_WIDTH = 200;
    public final static double SHOW_LOGO_HEIGHT = 200;
    public final static String SHOW_LOGO_POSITION = "top_left";

    public static final int SHOW_LOGO_OPACITY = 0; //0.0-1.0f

    public final static int SCREEN_PADDING = 50;
    public final static int SCREEN_MARGIN_LEFT = 0;
    public final static int SCREEN_MARGIN_RIGHT = 0;
    public final static int SCREEN_MARGIN_TOP = 0;
    public final static int SCREEN_MARGIN_BOTTOM = 0;

    public final static int MAIN_TIMER = 5; //seconds used in MainApplication
    public final static int SETTING_WEBSITE_TIMER = 30; //seconds

    public static final int API_GET_SCHEDULES_TIMER = 1*60; // second
    public static final int API_GET_SCHEDULES_TIMER_MAX = 5*60; // second

    public static final int API_REFRESH_APP_TIMER_MAX = 120*60; // second

    public static final int API_CHECK_STATUS_DEVICE_TIMER = 15*60; //seconds
    public static final int CHECK_MEMORY_DEVICE_TIME = 15*60; //seconds

    public static final float SHOW_DEVICE_NAME_FONT_SIZE = 12f;

    public static final float SHOW_TIMER_FONT_SIZE = 24f;
    public static final float SHOW_TIMER_FONT_OPACITY = 1.0f;

    public static final boolean AUTO_LOOP_VIDEO = true;
    public static final int DURATION_UNIT = 1; //1:second, 60: minute

    public static final boolean isDebug = false; // call Log.e or not
    public static final int debugTraceLevel = 5;

    public static final boolean AUTO_RESTART_WITH_UNCAUGHT_ERROR = true;
    public static final boolean AUTO_SEND_HASHKEY_TO_API = false;

    public static final String MINT_API_KEY = "4800f11f";

    public static final boolean OFFLINE_ENABLED = true; //

    public final static String FORMAT_SCHEDULE_DATE = "yyyy-MM-dd";
    public final static String FORMAT_SCHEDULE_TIME = "HH:mm";
    public final static String FORMAT_SCHEDULE_DATE_TIME = FORMAT_SCHEDULE_DATE + " " + FORMAT_SCHEDULE_TIME;

    public static final boolean USE_WEBVIEW_TO_SHOW_IMAGE = true; // can zoom,  touch ..
    public static final boolean USE_WEBVIEW_TO_SHOW_VIDEO = false;

    public static final boolean OVERRIDE_DOWNLOAD_APP = true;
    public static final boolean CACHE_WEBSITE = false;


    public static final String DEFAULT_SCHEDULE_FILE_API = "json/default_screen.json";
    public static final String DEFAULT_SCHEDULE_FILE_HOMEPAGE = "json/homepage_screen.json";
    public static final String ASSET_FOLDER = "android_asset";
    public static final String ASSET_ROOT_FOLDER = Constants.PROTOCOL_FILE + "/" + ASSET_FOLDER;

    public static final String ASSET_DOWNLOAD_FOLDER = "/data/data/";

    public static final String GALLERY_FOLDER = "galleries";
    public static final String APPS_FOLDER = "apps";
    public static final String COMMON_FOLDER = "common";
    public static final String LOCAL_DATABASE = "database";

    public static final String WEB_INDEX = "index.html";
}
