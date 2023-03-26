package com.stech.smartads.models;


import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class QuestionObj implements Parcelable {

    public static final String TYPE_SINGLE_CHOICE = "TYPE_SINGLE_CHOICE";
    public static final String TYPE_MULTIPLE_CHOICES = "TYPE_MULTIPLE_CHOICES";
    public static final String TYPE_ANSWER_TEXT= "TYPE_ANSWER_TEXT";

    private int id;
    private String content, image, type;
    private ArrayList<AnswerObj> answers;
    private String userAnswer = "";



    public QuestionObj(JSONObject jsonObj)
    {
        try {
            this.id = jsonObj.isNull("id")? 0:jsonObj.getInt("id");
            this.content = jsonObj.isNull("content")? "":jsonObj.getString("content");
            this.image = jsonObj.isNull("image")? "":jsonObj.getString("image");
            this.type = jsonObj.isNull("type")? "":jsonObj.getString("type");

            //get answers
            answers = new ArrayList<>();
            if(!jsonObj.isNull("answers")) {

                JSONArray array = jsonObj.getJSONArray("answers");
                AnswerObj answer;
                JSONObject answerJson;
                for (int i = 0; i < array.length(); i ++){
                    answerJson = array.getJSONObject(i);
                    answer = new AnswerObj(answerJson);

                    if(!answer.getKey().isEmpty()) {
                        answers.add(answer);
                    }
                }

            }

        }catch (Exception e) {
            this.id = 0;
            this.content = "";
            this.image = "";
            this.type = TYPE_ANSWER_TEXT;
            this.answers = new ArrayList<>();
        }
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public ArrayList<AnswerObj> getAnswers() {
        return answers;
    }

    public boolean isMultiChoices(){
        return type.equalsIgnoreCase(TYPE_MULTIPLE_CHOICES);
    }

    public boolean isSingleChoices(){
        return type.equalsIgnoreCase(TYPE_SINGLE_CHOICE);
    }

    public boolean isWriteAnswer(){
        return type.equalsIgnoreCase(TYPE_ANSWER_TEXT);
    }

    public boolean isAnswered(){
        if(isWriteAnswer()){
            return !userAnswer.isEmpty();
        } else {
            for (AnswerObj answer:answers) {
                if(answer.isSelected()){
                    return true;
                }
            }
            return false;
        }
    }

    public void updateAnswer(int index, AnswerObj answerObj){

        if(index < answers.size()){

            answers.set(index,answerObj);
        }
    }

    public void updateSingleChoice(int index, AnswerObj answerObj){
        for (AnswerObj answer : answers){

            if(!answer.getKey().equals(answerObj.getKey()) ) {
                answer.setSelected(false);
            } else {
                answer.setSelected(true);
            }
        }

    }

    protected QuestionObj(Parcel in) {
        id = in.readInt();
        content = in.readString();
        image = in.readString();
        type = in.readString();
        userAnswer = in.readString();
    }

    public static final Creator<QuestionObj> CREATOR = new Creator<QuestionObj>() {
        @Override
        public QuestionObj createFromParcel(Parcel in) {
            return new QuestionObj(in);
        }

        @Override
        public QuestionObj[] newArray(int size) {
            return new QuestionObj[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(content);
        dest.writeString(image);
        dest.writeString(type);
        dest.writeString(userAnswer);
    }
}
