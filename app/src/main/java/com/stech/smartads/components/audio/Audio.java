package com.stech.smartads.components.audio;


import com.stech.smartads.models.DataContentObj;

import org.json.JSONObject;

public class Audio {
    private int id=0;
    private String title="";
    private String url="";
    private String description="";

    public Audio(String url){
        this.id = 0;
        this.title = "";
        this.url = url;
        this.description = "";

    }

    public Audio(DataContentObj data){
        this.id = data.getId();
        this.title = data.getTitle();
        this.url = data.getUrl();
        this.description = data.getDescription();

    }

    public Audio(JSONObject jsonObj)
    {
        try {
            this.id = jsonObj.isNull("id")? 0:jsonObj.getInt("id");
            this.title = jsonObj.isNull("title")? "":jsonObj.getString("title");
            this.url = jsonObj.isNull("url")? "":jsonObj.getString("url");
            this.description = jsonObj.isNull("description")? "":jsonObj.getString("description");
        } catch (Exception e) {
            this.id = 0;
            this.title = "";
            this.url = "";
            this.description = "";
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String name) {
        this.title = name;
    }

    public String getUrl() {
        return description;
        // return "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3";
        // return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
