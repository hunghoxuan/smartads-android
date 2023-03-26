package com.stech.smartads.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mac on 10/26/17.
 */

public class HisDataWrapper {

    private String title;
    private String doctorName;
    private List<PatientObj> arrPatients;

    public HisDataWrapper(){
        title = doctorName = "";
        arrPatients = new ArrayList<>();
    }

    public HisDataWrapper(JSONObject jsonObj){
        try {
            this.title = jsonObj.isNull("title") ? "" : jsonObj.getString("title");
            this.doctorName = jsonObj.isNull("doctor") ? "" : jsonObj.getString("doctor");

            arrPatients = new ArrayList<>();
            PatientObj patientObj;

            //add calling list
            if(!jsonObj.isNull("callinglist")){
                JSONArray arrCallingListJson = jsonObj.getJSONArray("callinglist");

                for(int i = 0; i < arrCallingListJson.length(); i++){
                    patientObj = new PatientObj(arrCallingListJson.getJSONObject(i));
                    arrPatients.add(patientObj);
                }
            }

            //add pending list
            if(!jsonObj.isNull("pendinglist")){
                JSONArray arrCallingListJson = jsonObj.getJSONArray("pendinglist");

                for(int i = 0; i < arrCallingListJson.length(); i++){
                    patientObj = new PatientObj(arrCallingListJson.getJSONObject(i));
                    arrPatients.add(patientObj);
                }
            }


        } catch (Exception ex){
            title = doctorName = "";
            arrPatients = new ArrayList<>();
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public List<PatientObj> getArrPatients() {
        return arrPatients;

    }

    public void setArrPatients(List<PatientObj> arrPatients) {
        this.arrPatients = arrPatients;
    }
}
