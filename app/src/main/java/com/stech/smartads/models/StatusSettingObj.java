package com.stech.smartads.models;

import org.json.JSONObject;

public class StatusSettingObj {
    private boolean needRefreshSchedules;

    public StatusSettingObj(JSONObject jsonObj){
        try {
            this.needRefreshSchedules = jsonObj.isNull("needRefreshSchedules") ? true : jsonObj.getBoolean("needRefreshSchedules");

        } catch (Exception ex){
            needRefreshSchedules = true;
        }
    }

    public StatusSettingObj(){
    }

    public boolean isNeedRefreshSchedules() {
        return needRefreshSchedules;
    }

    public void setNeedRefreshSchedules(boolean needRefreshSchedules) {
        this.needRefreshSchedules = needRefreshSchedules;
    }
}
