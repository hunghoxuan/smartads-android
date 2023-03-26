package com.stech.smartads.models;


public class Song {
    private int id=0;
    private String title="";
    private String url="";
    private String description="";

    public Song(DataContentObj data){
        this.id = data.getId();
        this.title = data.getTitle();
        this.url = data.getUrl();
        this.description = data.getDescription();

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
        return url;
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
