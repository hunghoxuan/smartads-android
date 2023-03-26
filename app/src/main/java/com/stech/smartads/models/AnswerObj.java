package com.stech.smartads.models;


import org.json.JSONObject;

public class AnswerObj {

    private String key, value;
    private boolean isSelected = false;




    public AnswerObj(JSONObject jsonObj)
    {
        try {
            this.key = jsonObj.isNull("key")? "":jsonObj.getString("key");
            this.value = jsonObj.isNull("value")? "":jsonObj.getString("value");


        }catch (Exception e) {
            this.key = "";
            this.value = "";

        }
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
