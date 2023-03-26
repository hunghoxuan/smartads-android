package com.stech.smartads.models;


import org.json.JSONObject;

public class AppVersionObj {

    private String fileUrl = "";
    private String name = "";

    private String packageName, description, id, modified_date = "";
    private int versionCode = 0;
    private boolean isAutoDownload = true;

    public AppVersionObj(){

    }

    public AppVersionObj(JSONObject jsonObj)
    {
        try {
            this.fileUrl = jsonObj.isNull("file") ? "" : jsonObj.getString("file");
            this.id = jsonObj.isNull("id") ? "" : jsonObj.getString("id");
            this.packageName = jsonObj.isNull("package_name") ? "" : jsonObj.getString("package_name");
            this.name = jsonObj.isNull("name") ? "" : jsonObj.getString("name");
            this.description = jsonObj.isNull("description") ? "" : jsonObj.getString("description");
            this.versionCode = jsonObj.isNull("version") ? 0 : jsonObj.getInt("version");
            this.isAutoDownload = jsonObj.isNull("is_auto") || jsonObj.getBoolean("is_auto");
            this.modified_date = jsonObj.isNull("modified_date") ? "" : jsonObj.getString("modified_date");

        } catch (Exception e) {

        }
    }

    public String getId() {
        return id;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getPackageName() {
        return packageName;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }

    public int getVersionCode() {
        return versionCode;
    }
    public String getLastUpdate() {
        return modified_date;
    }


    public boolean isAutoDownload() {
        return isAutoDownload;
    }
}
