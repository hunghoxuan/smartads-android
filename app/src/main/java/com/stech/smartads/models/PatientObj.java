package com.stech.smartads.models;

import org.json.JSONObject;

/**
 * Created by mac on 10/26/17.
 */

public class PatientObj {

    public static String STATUS_WAITING = "O";
    public static String STATUS_CALLING = "C";
    private int id;
    private int ticketNumber;
    private String name;
    private String status;

    public PatientObj(JSONObject jsonObj){
        try {
            this.id = jsonObj.isNull("id") ? 0 : jsonObj.getInt("id");
            this.ticketNumber = jsonObj.isNull("receptno") ? -1 : jsonObj.getInt("receptno");
            this.name = jsonObj.isNull("patientname") ? "" : jsonObj.getString("patientname");
            this.status = jsonObj.isNull("status") ? "" : jsonObj.getString("status");

        } catch (Exception ex){
            id = ticketNumber= -1;
            name = status = "...";
        }
    }

    public PatientObj(){
        id = ticketNumber= -1;
        name = status = "";
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTicketNumber() {
        return ticketNumber;
    }

    public void setTicketNumber(int ticketNumber) {
        this.ticketNumber = ticketNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCalling(){
        return status.equalsIgnoreCase(STATUS_CALLING);
    }
}
