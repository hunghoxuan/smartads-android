package com.stech.smartads.models;


public class FeedbackObj {

    private int rate = 3;
    private String reason = "";
    private boolean isSelected = false;




    public FeedbackObj()
    {

    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
