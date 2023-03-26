package com.stech.smartads.models;

/**
 * Created by mac on 10/26/17.
 */

public class DeviceSetting {

    private String downloadResourceTime = "5:00 AM";
    private String background = "#ffffff";
    private String fontColor = "#000000";
    private int titleFontSize = 30;
    private int normalFontSize = 20;
    private int smallFontSize = 14;

    public String getDownloadResourceTime() {
        return downloadResourceTime;
    }

    public void setDownloadResourceTime(String downloadResourceTime) {
        this.downloadResourceTime = downloadResourceTime;
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

    public int getTitleFontSize() {
        return titleFontSize;
    }

    public void setTitleFontSize(int titleFontSize) {
        this.titleFontSize = titleFontSize;
    }

    public int getNormalFontSize() {
        return normalFontSize;
    }

    public void setNormalFontSize(int normalFontSize) {
        this.normalFontSize = normalFontSize;
    }

    public int getSmallFontSize() {
        return smallFontSize;
    }

    public void setSmallFontSize(int smallFontSize) {
        this.smallFontSize = smallFontSize;
    }
}
