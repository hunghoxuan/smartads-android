package com.stech.smartads.models;

import android.app.Activity;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.FrameLayout;

import com.stech.smartads.activities.BaseActivity;
import com.stech.smartads.components.audio.Audio;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.fragments.BaseFragment;
import com.stech.smartads.fragments.CycleListFragment;
import com.stech.smartads.fragments.PatientListFragment;
import com.stech.smartads.fragments.SlideFragment;
import com.stech.smartads.fragments.WebViewFragment;
import com.stech.smartads.config.Constants;
import com.stech.smartads.core.AppData;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.ParseUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class LayoutFrameObj implements Parcelable {

    private int id = 0;
    private String name = "";
    private int percentOfScreenWidth =100;
    private int percentOfScreenHeight = 100; // % of screen - value : 1 -> 100
    private double positionMarginLeft = 0;
    private double positionMarginTop = 0;
    private int width = 0;
    private int height = 0;
    private int left = 0;
    private int top = 0;

    private String dataContentType = "html";
    private List<DataContentObj> data;
    private List<Audio> listAudios;

    //extra
    private String background = "#3F51B5";
    private String fontColor = "#ffffff";

    private BaseActivity activity;
    //public boolean isPercent = true;

    private FrameLayout frame;

    public LayoutFrameObj() {
        super();
        this.data = new LinkedList<DataContentObj>();
    }

    public LayoutFrameObj(LayoutFrameObj obj) {
        super();
        setBackground(obj.getBackground());
        getData().addAll(obj.getData());
        setId(obj.getId() + CommonUtil.getRandom(1, 100));
        setWidth(obj.getWidth(activity));
        setHeight(obj.getHeight(activity));
        setLeft(obj.getLeft(activity));
        setTop(obj.getTop(activity));
        setDataContentType(obj.getDataContentType());
    }

    public LayoutFrameObj(String text) {
        super();
        this.data = new LinkedList<DataContentObj>();
        addContent(text, CommonUtil.getUrlType(text));
    }

    public LayoutFrameObj(String text, String type) {
        super();
        this.data = new LinkedList<DataContentObj>();
        addContent(text, type);
    }

    public void addContent(List<DataContentObj> contentList) {
        this.data.addAll(contentList);
    }

    public void addContent(DataContentObj content) {
        this.data.add(content);
    }

    public void addContent(String text, String type) {
        DataContentObj content;

        if (type.equalsIgnoreCase(Constants.TYPE_IMAGE) || type.equalsIgnoreCase(Constants.TYPE_URL) || type.equalsIgnoreCase(Constants.TYPE_VIDEO))
            content = new DataContentObj(0, "", text, "", type);
        else
            content = new DataContentObj(0, text, "", text, type);
        content.setDisplayType(type);
        this.data.add(content);
    }

    public LayoutFrameObj(JSONObject jsonObj)
    {
        try {
            this.id = jsonObj.isNull("id")? 0:jsonObj.getInt("id");
            this.name = jsonObj.isNull("name")? "":jsonObj.getString("name");
            this.percentOfScreenWidth = jsonObj.isNull("percentWidth")? 100:jsonObj.getInt("percentWidth");
            this.percentOfScreenHeight = jsonObj.isNull("percentHeight")? 100:jsonObj.getInt("percentHeight");
            this.positionMarginLeft = jsonObj.isNull("marginLeft")? 0:jsonObj.getInt("marginLeft");
            this.positionMarginTop = jsonObj.isNull("marginTop")? 0:jsonObj.getInt("marginTop");
            this.dataContentType = jsonObj.isNull("contentLayout")? "text":jsonObj.getString("contentLayout");
            this.background = jsonObj.isNull("backgroundColor")? background : ((jsonObj.getString("backgroundColor").length() == 7) ? jsonObj.getString("backgroundColor"): background);
            this.fontColor = jsonObj.isNull("fontColor")? fontColor:jsonObj.getString("fontColor");

            //parse json Array
            JSONArray dataJson = jsonObj.getJSONArray("data");
            this.data = new LinkedList<>();
            for (int i = 0; i < dataJson.length();i++) {
                data.add(new DataContentObj((JSONObject) dataJson.get(i)));
            }

            //parse json audio
            listAudios = new LinkedList<>();
            if(!jsonObj.isNull("audio")) {
                JSONArray audioJson = jsonObj.getJSONArray("audio");
                for (int i = 0; i < audioJson.length(); i++) {
                    listAudios.add(new Audio((JSONObject) audioJson.get(i)));
                }
            }

        } catch (Exception e) {
            this.id = 0;
            this.name = "";
            this.percentOfScreenWidth = 0;
            this.percentOfScreenHeight = 0;
            this.positionMarginLeft = 0;
            this.positionMarginTop = 0;
            this.dataContentType = "text";
            this.data = new LinkedList<>();
            this.listAudios = new LinkedList<>();
        }
    }

    protected LayoutFrameObj(Parcel in) {
        id = in.readInt();
        name = in.readString();
        percentOfScreenWidth = in.readInt();
        percentOfScreenHeight = in.readInt();
        positionMarginLeft = in.readDouble();
        positionMarginTop = in.readDouble();
        dataContentType = in.readString();
        data = in.createTypedArrayList(DataContentObj.CREATOR);
        listAudios = new LinkedList<Audio>(); // in.createTypedArrayList(Audio.CREATOR);
        background = in.readString();
        fontColor = in.readString();
    }

    public static final Creator<LayoutFrameObj> CREATOR = new Creator<LayoutFrameObj>() {
        @Override
        public LayoutFrameObj createFromParcel(Parcel in) {
            return new LayoutFrameObj(in);
        }

        @Override
        public LayoutFrameObj[] newArray(int size) {
            return new LayoutFrameObj[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBackground() {
        if(background != null && !background.isEmpty())
             return background;
        else
            return AppData.getInstance().getCurrentSchedule().getBackground();
    }

    public void setBackground(String bg) {
        background = bg;
    }

    public String getFontColor() {
        if(fontColor != null && !fontColor.isEmpty())
            return fontColor;
        else
            return AppData.getInstance().getCurrentSchedule().getFontColor();
    }

    public List<DataContentObj> getData() {
        return data != null? data : new ArrayList<DataContentObj>();
    }

    public List<Audio> getAudios() {
        return this.listAudios;
    }

    public boolean isAvailableSongs(){
        return listAudios!=null && listAudios.size()>0;
    }

    public void setWidth(double w) {
        if (w <= 1)
            percentOfScreenWidth = (int) w * 100;
        else
            width = (int) w;
    }

    public void setHeight(double h) {
        if (h <= 1)
            percentOfScreenWidth = (int) h * 100;
        else
            height = (int) h;
    }

    public void setLeft(double x) {
        if (x <= 1)
            positionMarginLeft = x * 100;
        else
            left = (int) x;
    }

    public void setTop(double y) {
        if (y <= 1)
            positionMarginTop = y * 100;
        else
            top = (int) y;
    }

    public int getWidthPercent() {
        return this.percentOfScreenWidth;
    }

    public int getHeightPercent() {
        return this.percentOfScreenHeight;
    }

    public double getLeftPercent() {
        return this.positionMarginLeft;
    }

    public double getTopPercent() {
        return this.positionMarginTop;
    }

    public double getRightPercent() {
        return this.positionMarginLeft + (double) this.percentOfScreenWidth;
    }

    public double getBottomPercent() {
        return this.positionMarginTop + (double) this.percentOfScreenHeight;
    }

    public int getWidth(Activity act) {
        return width > 0 ? width : getPxValue(act,true, percentOfScreenWidth);
    }

    public int getWidth() {
        return this.getWidth(this.getActivity());
    }

    public int getHeight(Activity act) {
        return height > 0 ? height : getPxValue(act,false, percentOfScreenHeight);
    }

    public int getHeight() {
        return this.getHeight(this.getActivity());
    }

    public int getLeft(Activity act) {
        return (left > 0 ? left : getPxValue(act,true, positionMarginLeft)) + CommonUtil.getScreenMarginLeft();
    }

    public int getLeft() {
        return this.getLeft(this.getActivity());
    }

    public int getTop(Activity act) {
        return (top > 0 ? top : getPxValue(act,false, positionMarginTop)) + CommonUtil.getScreenMarginTop();
    }

    public int getTop() {
        return this.getTop(this.getActivity());
    }

    public String getDataContentType() {
        return dataContentType;
    }

    public void setDataContentType(String dataContentType) {
        this.dataContentType = dataContentType;
    }

    public BaseActivity getActivity() {
        return this.activity != null ? this.activity : AppData.getInstance().getCurrentActivity();
    }

    public void setActivity(BaseActivity activity) {
        this.activity = activity;
    }

    public FrameLayout getFrame() {
        return frame;
    }

    public void setFrame(FrameLayout frame) {
        this.frame = frame;
    }

    public BaseFragment getFragmentContent()
    {
        if(dataContentType.equals(Constants.TYPE_NEWS))
            return CycleListFragment.getInstance(this);

        if(dataContentType.equals(Constants.TYPE_GALLERY))
            return SlideFragment.getInstance(this);

        if(dataContentType.equals(Constants.TYPE_SLIDE))
            return SlideFragment.getInstance(this);

        if(dataContentType.equals(Constants.TYPE_VIDEO))
            return SlideFragment.getInstance(this);

        if(dataContentType.equals(Constants.TYPE_HTML))
            return WebViewFragment.getInstance(this);

        if(dataContentType.equals(Constants.TYPE_TEXT))
            return WebViewFragment.getInstance(this);

        if(dataContentType.equals(Constants.TYPE_HIS_VIMES))
            return PatientListFragment.getInstance(this);

        return WebViewFragment.getInstance(this);
    }


    @Override
    public String toString() {
        return getJson();
    }

    public String getJson() {
        return ParseUtility.getDataJsonObject(getData()).toString();
    }

    public String getDataJson() {
        return getDataJsonArray().toString();
    }

    public JSONArray getDataJsonArray() {
        return ParseUtility.getDataJsonArray(getData());
    }

    public String encode(){
        return MainApplication.getGson().toJson(this);
    }

    private int getPxValue(Activity act, Boolean isScreenWidth, double percent)
    {
        if (isScreenWidth)
            return (int) (CommonUtil.getScreenWidth(act) * percent / 100);

        return (int) (CommonUtil.getScreenHeight(act) * percent / 100);
    }

    public LayoutFrameObj clone() {
        return new LayoutFrameObj(this);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeInt(percentOfScreenWidth);
        dest.writeInt(percentOfScreenHeight);
        dest.writeDouble(positionMarginLeft);
        dest.writeDouble(positionMarginTop);
        dest.writeString(dataContentType);
        dest.writeTypedList(data);
        // dest.writeTypedList(listAudios);
        dest.writeString(background);
        dest.writeString(fontColor);
    }
}
