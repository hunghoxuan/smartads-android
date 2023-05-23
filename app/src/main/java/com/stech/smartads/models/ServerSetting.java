package com.stech.smartads.models;

import com.stech.smartads.R;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;

import org.json.JSONObject;

/**
 * Created by mac on 10/26/17.
 */

public class ServerSetting {

    //header setting
    private String headerViewColor = "#274e13";
    private int headerViewHeight = 14; // unit: %
    private String titleColor = "#ffffff";

    //bottom setting
    private String bottomViewColor = "#000000";
    private int bottomViewHeight = 6; // unit: %

    //list setting
    private String background = "#ffffff";
    private String fontColor = "#000000";
    private int rowViewHeight = 16; // unit: %

    //list header
    private String listViewHeaderBackground = "#e1e1e1";


    //text content
    private String textTicketTitle = "Số phiếu";
    private String textNameTitle = "Họ và tên bệnh nhân";
    private String textStatusTitle = "Tình trạng";

    //refresh data time
    private long refreshDataTime = AppConfigs.API_GET_SCHEDULES_TIMER; //60 second
    private boolean autoRestartAppWithUnCaughtError = AppConfigs.AUTO_RESTART_WITH_UNCAUGHT_ERROR; //60 second
    private boolean isDebug = AppConfigs.isDebug; //60 second

    private boolean useWebViewToShowImage = AppConfigs.USE_WEBVIEW_TO_SHOW_IMAGE;
    private boolean useWebViewToShowVideo = AppConfigs.USE_WEBVIEW_TO_SHOW_VIDEO;

    private boolean isShowFlashing = false;
    private String MintAPIKey = AppConfigs.MINT_API_KEY;


    //refresh app
    private int refreshAppTime = AppConfigs.API_REFRESH_APP_TIMER_MAX;  //720 mins
    private int mainTimer = AppConfigs.MAIN_TIMER;

    private int frameBorder = AppConfigs.SCREEN_FRAME_BORDER;
    private String frameBorderColor = AppConfigs.SCREEN_FRAME_BORDER_COLOR;


    private int logoOpacity = AppConfigs.SHOW_LOGO_OPACITY; // 0%
    private String logoUrl = ""; // 0%
    private String logoPosition = AppConfigs.SHOW_LOGO_POSITION;

    private JSONObject jsonSetting;

    private int videoFillMode = Constants.FILL_MODE_FULL_SCREEN;

    public ServerSetting(){

    }

    public ServerSetting(JSONObject jsonObj){
        try {
            jsonSetting = jsonObj;

            //header
            this.headerViewColor = jsonObj.isNull("headerViewColor") ? headerViewColor : jsonObj.getString("headerViewColor");
            this.headerViewHeight = jsonObj.isNull("headerViewHeight") ? headerViewHeight : jsonObj.getInt("headerViewHeight");
            this.titleColor = jsonObj.isNull("titleColor") ? titleColor : jsonObj.getString("titleColor");

            //bottom view
            this.bottomViewColor = jsonObj.isNull("bottomViewColor") ? bottomViewColor : jsonObj.getString("bottomViewColor");
            this.bottomViewHeight = jsonObj.isNull("bottomViewHeight") ? bottomViewHeight : jsonObj.getInt("bottomViewHeight");

            //main list
            this.background = jsonObj.isNull("background") ? background : jsonObj.getString("background");
            this.fontColor = jsonObj.isNull("fontColor") ? fontColor : jsonObj.getString("fontColor");

            // row
            this.rowViewHeight = jsonObj.isNull("rowViewHeight") ? rowViewHeight : jsonObj.getInt("rowViewHeight");

            //header of list
            this.listViewHeaderBackground = jsonObj.isNull("listViewHeaderBackground") ? listViewHeaderBackground : jsonObj.getString("listViewHeaderBackground");

            //text header
            this.textTicketTitle = jsonObj.isNull("textTicketTitle") ? textTicketTitle : jsonObj.getString("textTicketTitle");
            this.textNameTitle = jsonObj.isNull("textNameTitle") ? textNameTitle : jsonObj.getString("textNameTitle");
            this.textStatusTitle = jsonObj.isNull("textStatusTitle") ? textStatusTitle : jsonObj.getString("textStatusTitle");

            this.MintAPIKey = jsonObj.isNull("MintAPIKey") ? MintAPIKey : jsonObj.getString("MintAPIKey");

            this.frameBorder = jsonObj.isNull(Constants.PARAM_FRAME_BORDER) ? frameBorder : jsonObj.getInt(Constants.PARAM_FRAME_BORDER);
            this.frameBorderColor = jsonObj.isNull(Constants.PARAM_FRAME_BORDER_COLOR) ? frameBorderColor : jsonObj.getString(Constants.PARAM_FRAME_BORDER_COLOR);

            //logo
            this.logoUrl = jsonObj.isNull(Constants.PARAM_LOGO_URL) ? logoUrl : jsonObj.getString(Constants.PARAM_LOGO_URL);
            this.logoOpacity = jsonObj.isNull(Constants.PARAM_LOGO_OPACITY) ? logoOpacity : jsonObj.getInt(Constants.PARAM_LOGO_OPACITY);
            this.logoPosition = jsonObj.isNull(Constants.PARAM_LOGO_POSITION) ? logoPosition : jsonObj.getString(Constants.PARAM_LOGO_POSITION);

            //auto refresh data time
            this.refreshDataTime = jsonObj.isNull(Constants.PARAM_REFRESH) ? refreshDataTime : jsonObj.getLong(Constants.PARAM_REFRESH);

            this.isShowFlashing = (!jsonObj.isNull("isBlink")) && jsonObj.getBoolean("isBlink");

            this.refreshAppTime = jsonObj.isNull(Constants.PARAM_REFRESH_APP) ? refreshAppTime : jsonObj.getInt(Constants.PARAM_REFRESH_APP);
            this.mainTimer = jsonObj.isNull(Constants.PARAM_MAIN_TIMER) ? mainTimer : jsonObj.getInt(Constants.PARAM_MAIN_TIMER);
            this.autoRestartAppWithUnCaughtError = jsonObj.isNull("autoRestartAppWithUnCaughtError") ? autoRestartAppWithUnCaughtError : jsonObj.getBoolean("autoRestartAppWithUnCaughtError");
            this.isDebug = jsonObj.isNull("isDebug") ? isDebug : jsonObj.getBoolean("isDebug");

            this.useWebViewToShowImage = jsonObj.isNull("useWebViewToShowImage") ? useWebViewToShowImage : jsonObj.getBoolean("useWebViewToShowImage");
            this.useWebViewToShowVideo = jsonObj.isNull("useWebViewToShowVideo") ? useWebViewToShowVideo : jsonObj.getBoolean("useWebViewToShowVideo");
            this.videoFillMode = jsonObj.isNull("VideoFillMode") ? videoFillMode : jsonObj.getInt("VideoFillMode");

        } catch (Exception ex) {
            this.refreshAppTime = AppConfigs.API_REFRESH_APP_TIMER_MAX;
            this.refreshDataTime = AppConfigs.API_GET_SCHEDULES_TIMER;
        }
    }

    public String getSetting(String key, String defaultValue) {
        try {
            return jsonSetting.isNull(key) ? defaultValue : jsonSetting.getString(key);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public int getSettingInt(String key, int defaultValue) {
        try {
            return jsonSetting.isNull(key) ? defaultValue : jsonSetting.getInt(key);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public String getSetting(String key) {
        return getSetting(key, "");
    }

    public String getHeaderViewColor() {
        return headerViewColor;
    }

    public void setHeaderViewColor(String headerViewColor) {
        this.headerViewColor = headerViewColor;
    }

    public int getHeaderViewHeight() {
        return headerViewHeight;
    }

    public void setHeaderViewHeight(int headerViewHeight) {
        this.headerViewHeight = headerViewHeight;
    }

    public boolean getAutoRestartServerWithUnCaughtError() {
        return true;
        //return autoRestartAppWithUnCaughtError;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public String getFrameBorderColor() {
        return frameBorderColor;
    }
    public int getFrameBorder() {
        return frameBorder;
    }

    public boolean getUseWebViewToShowImage() {
        return useWebViewToShowImage;
    }

    public boolean getUseWebViewToShowVideo() {
        return useWebViewToShowVideo;
    }

    public String getTitleColor() {
        return titleColor;
    }

    public void setTitleColor(String titleColor) {
        this.titleColor = titleColor;
    }

    public String getBottomViewColor() {
        return bottomViewColor;
    }

    public void setBottomViewColor(String bottomViewColor) {
        this.bottomViewColor = bottomViewColor;
    }

    public int getBottomViewHeight() {
        return bottomViewHeight;
    }

    public void setBottomViewHeight(int bottomViewHeight) {
        this.bottomViewHeight = bottomViewHeight;
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

    public int getRowViewHeight() {
        return rowViewHeight;
    }

    public void setRowViewHeight(int rowViewHeight) {
        this.rowViewHeight = rowViewHeight;
    }

    public String getListViewHeaderBackground() {
        return listViewHeaderBackground;
    }

    public void setListViewHeaderBackground(String listViewHeaderBackground) {
        this.listViewHeaderBackground = listViewHeaderBackground;
    }

    public String getTextTicketTitle() {
        return textTicketTitle;
    }

    public void setTextTicketTitle(String textTicketTitle) {
        this.textTicketTitle = textTicketTitle;
    }

    public String getTextNameTitle() {
        return textNameTitle;
    }

    public void setTextNameTitle(String textNameTitle) {
        this.textNameTitle = textNameTitle;
    }

    public String getTextStatusTitle() {
        return textStatusTitle;
    }

    public void setTextStatusTitle(String textStatusTitle) {
        this.textStatusTitle = textStatusTitle;
    }

    public boolean isShowFlashing() {
        return isShowFlashing;
    }

    public void setShowFlashing(boolean showFlashing) {
        isShowFlashing = showFlashing;
    }

    public long getRefreshDataTime() {
        if(refreshDataTime < AppConfigs.API_GET_SCHEDULES_TIMER_MAX)
          return refreshDataTime * AppConfigs.DURATION_UNIT;
        else
            return AppConfigs.API_GET_SCHEDULES_TIMER_MAX * AppConfigs.DURATION_UNIT;
    }

    public int getMainTimer() {
        return mainTimer;
    }

    public int getIdleTimeOut() {
        return getSettingInt(Constants.PARAM_WEBSITE_TIMER, AppConfigs.SETTING_WEBSITE_TIMER);
    }

    public String getHomepage() {
        return getSetting(Constants.PARAM_HOME_PAGE);
    }

    public String getAppMode() {
        return getSetting(Constants.PARAM_APP_MODE);
    }

    public void setRefreshDataTime (long refreshDataTime) {
        this.refreshDataTime = refreshDataTime;
    }

    public int getRefreshAppTime() {
        return refreshAppTime * AppConfigs.DURATION_UNIT;
    }

    public int getLogoOpacity() {
        return logoOpacity;
    }

    public int getDefaultLogoResource() {
        return R.drawable.ic_logo_108_tron;
    }

    public void setLogoOpacity(int logoOpacity) {
        this.logoOpacity = logoOpacity;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoPosition(String logoPosition) {
        this.logoPosition = logoPosition;
    }

    public String getLogoPosition() {
        return logoPosition;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public int getVideoFillMode() {
        return videoFillMode;
    }

    public String getMintAPIKey() {
        return MintAPIKey;
    }
}
