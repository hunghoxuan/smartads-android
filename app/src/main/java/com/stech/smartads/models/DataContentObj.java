package com.stech.smartads.models;


import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.stech.smartads.config.Constants;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.StringUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class DataContentObj implements Parcelable {

    private int id;
    private String title, url, description, dataType;

    //only for video data
    private String displayType = DISPLAY_TYPE_TIME;
    private int displayDuration ;
    public static final String DISPLAY_TYPE_NUMBER ="number";
    public static final String DISPLAY_TYPE_TIME ="time";
    public static final String DISPLAY_TYPE_SECOND ="second";

    public DataContentObj(int id, String title, String url, String description, String dataType) {
        this.id = id;
        this.title = title;
        this.url = url;
        this.description = description;
        this.dataType = dataType;
        this.displayType = DISPLAY_TYPE_TIME;
    }

    public DataContentObj(String content, String type) {
        this.id = 0;
        if (type.equalsIgnoreCase(Constants.TYPE_VIDEO) || type.equalsIgnoreCase(Constants.TYPE_IMAGE) || type.equalsIgnoreCase(Constants.TYPE_URL)) {
            this.url = content;
        } else {
            this.title = content;
            this.description = content;
        }
        this.dataType = type;
        this.displayType = DISPLAY_TYPE_TIME;
        this.displayDuration = 10;
    }

    public DataContentObj(String title, String content, String type) {
        this.id = 0;
        if (type.equalsIgnoreCase(Constants.TYPE_VIDEO) || type.equalsIgnoreCase(Constants.TYPE_IMAGE) || type.equalsIgnoreCase(Constants.TYPE_URL)) {
            this.url = content;
            this.title = title;
        } else {
            this.title = title;
            this.description = content;
            this.url = content;
        }
        this.dataType = type;
        this.displayType = DISPLAY_TYPE_TIME;
        this.displayDuration = 10;
    }

    public DataContentObj(String url) {
        this.id = 0;
        this.title = "";
        this.url = url;
        this.description = "";
        this.dataType = CommonUtil.getUrlType(url);

        this.displayType = DISPLAY_TYPE_TIME;
    }

    public DataContentObj() {
        this.id = 0;
        this.title = "";
        this.url = "";
        this.description = "";
        this.dataType = Constants.TYPE_TEXT;
        this.displayType = DISPLAY_TYPE_TIME;
    }


    public DataContentObj(JSONObject jsonObj)
    {
        try {
            this.id = jsonObj.isNull("id")? 0:jsonObj.getInt("id");
            this.title = jsonObj.isNull("title")? "":jsonObj.getString("title");
            this.url = jsonObj.isNull("url")? "":jsonObj.getString("url");

            this.description = jsonObj.isNull("description")? "":jsonObj.getString("description");
            this.dataType = jsonObj.isNull("dataType")? "": jsonObj.getString("dataType");
            this.displayType = jsonObj.isNull("kind")? displayType: jsonObj.getString("kind");
            if (this.displayType.equals(DataContentObj.DISPLAY_TYPE_SECOND)) {
                this.displayType = DataContentObj.DISPLAY_TYPE_TIME;
                this.displayDuration = jsonObj.isNull("duration") ? 0 : (int) (jsonObj.getDouble("duration"));
            } else if (this.displayType.equals(DataContentObj.DISPLAY_TYPE_TIME)) {
                this.displayDuration = jsonObj.isNull("duration") ? 0 : (int) (jsonObj.getDouble("duration") * 60);
            } else {
                this.displayDuration = jsonObj.isNull("duration") ? 0 : (int) (jsonObj.getDouble("duration"));
            }

        } catch (Exception e) {
            this.id = 0;
            this.title = "";
            this.url = "";
            this.description = "";
            this.dataType = "";
            this.displayType = "";
            this.displayDuration = 0;
        }
    }

    protected DataContentObj(Parcel in) {
        id = in.readInt();
        title = in.readString();
        url = in.readString();
        description = in.readString();
        dataType = in.readString();
        displayType = in.readString();
        displayDuration= in.readInt();
    }

    public static final Creator<DataContentObj> CREATOR = new Creator<DataContentObj>() {
        @Override
        public DataContentObj createFromParcel(Parcel in) {
            return new DataContentObj(in);
        }

        @Override
        public DataContentObj[] newArray(int size) {
            return new DataContentObj[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        if (description == null)
            description = "";
        description = description.trim();


        return description;
//        if (description.startsWith("<html"))
//            return description;
//
//        if (!dataType.equalsIgnoreCase(Constants.TYPE_HTML)) {
//            return StringUtil.removeHtmlTags(description);
//        } else {
//            return description;
//        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        if (url == null)
            url = "";
        return url;
    }

    //get play url file in cache - //only for file; not html
    public String getPlayUrl(Context context) {

        //if url is empty
        if(url == null || url.trim().isEmpty()) return "";

        // if file from asset
        if(url.contains("asset")){
            return url;
        }

        String cacheUrl = CacheManager.getCacheFileUrl(context,url);
        if(cacheUrl.isEmpty()){
            CacheManager.downloadFile(context,url,1);
            return url;
        }else{
            File file = new File(cacheUrl);
            if(file.exists()){
                return file.getAbsolutePath();
            }else{
                CacheManager.downloadFile(context,url,1);
                return url;
            }

        }

    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDisplayType() {
        return displayType == null ? DISPLAY_TYPE_TIME : displayType;
    }

    public void setDisplayType(String displayType) {
        this.displayType = displayType;
    }

    public int getDisplayDuration() {
        return displayDuration;
    }

    public void setDisplayDuration(int displayDuration) {
        this.displayDuration = displayDuration;
    }

    @Override
    public String toString() {
        return getJson();
    }

    public String getJson() {
        JSONObject child = getJSONObject();
        return child != null ? child.toString() : "{}";
    }

    public JSONObject getJSONObject() {
        try {
            JSONObject child = new JSONObject();
            child.put("id", getId());
            child.put("title", getTitle());
            child.put("url", getUrl());
            child.put("description", StringUtil.getFullUrl(getDescription()));
            child.put("dataType", dataType);
            child.put("kind", displayType);
            child.put("duration", displayDuration);

            return child;
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(description);
        dest.writeString(dataType);
        dest.writeString(displayType);
        dest.writeInt(displayDuration);
    }
}
