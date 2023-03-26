package com.stech.smartads.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.adapters.PatientListAdapter;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.core.AppData;
import com.stech.smartads.interfaces.IModelListener;
import com.stech.smartads.utils.ParseUtility;
import com.stech.smartads.models.HisDataWrapper;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.models.PatientObj;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PatientListFragment extends BaseFragment {

    private static final String TAG = PatientListFragment.class.getSimpleName();
    private static final String PARAM_DATA = "data";


    private LayoutFrameObj frameObj;
    private HisDataWrapper hisDataWrapper;
    private List<PatientObj> arrPatients = new ArrayList<>();
    private PatientListAdapter mAdapter;
    private ListView listView;

    //header
    private LinearLayout viewHeader,viewListHeader;
    private TextView lblTitle;
    private TextView lblTicketNumber, lblPatientNameTitle, lblStatusTitle;
    private int counterDataEmpty = 0;
    private int maxCounterDataEmpty = 10;
    private int maxDelayRefreshTime = 10;

    //bottom
    private LinearLayout viewBottom;

    private Runnable runnableReloadHISData = new Runnable() {
        @Override
        public void run() {
            loadHisData();
        }
    };

    //check refresh layout
    private BroadcastReceiver broadcastReceiverRefreshLayout = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((MainApplication)getApplication()).getAppHandler().removeCallbacks(runnableReloadHISData);
        }
    };

    private ServerSetting serverSetting;


    public PatientListFragment() {
        // Required empty public constructor
    }

    public static PatientListFragment getInstance(LayoutFrameObj data) {

        PatientListFragment fragment = new PatientListFragment();
        fragment.setLayoutFrame(data);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CommonUtil.log(TAG, TAG + " :onCreate");

        serverSetting = AppData.getInstance().getServerSetting();
        maxDelayRefreshTime = (int)(serverSetting.getRefreshDataTime());//kiemdv /1000
        MainApplication.counterCallingHisData = maxDelayRefreshTime;
    }

    @Override
    View inflateLayout(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list_patients, container, false);
    }

    @Override
    void initUI(View view) {
        lblTitle = view.findViewById(R.id.lblRoomNumber);
        viewHeader = view.findViewById(R.id.viewHeader);
        viewBottom = view.findViewById(R.id.viewBottom);

        viewListHeader = view.findViewById(R.id.viewListHeader);
        lblTicketNumber = view.findViewById(R.id.lblTicketNumber);
        lblPatientNameTitle = view.findViewById(R.id.lblPatientName);
        lblStatusTitle = view.findViewById(R.id.lblStatus);

        listView = (ListView) view.findViewById(R.id.lsvPatients);
        arrPatients.add(new PatientObj());
        mAdapter = new PatientListAdapter(self,arrPatients);
        listView.setAdapter(mAdapter);

        //load content
        loadServerSetting();
    }

    private void loadServerSetting(){
        ServerSetting setting = AppData.getInstance().getServerSetting();

        //set header
        //background color
        viewHeader.setBackgroundColor(Color.parseColor(setting.getHeaderViewColor()));

        //height
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHeader.getLayoutParams();
        params.height = ((int)(CommonUtil.getScreenHeightAsPixel(self)*setting.getHeaderViewHeight())/100);
        viewHeader.setLayoutParams(params);

        lblTitle.setTextColor(Color.parseColor(setting.getTitleColor()));
        float textSizeTitle = (float) (((params.height*46)/100)/ getResources().getDisplayMetrics().scaledDensity);
        lblTitle.setTextSize(textSizeTitle);

        //setting bottom
        //background color
        viewBottom.setBackgroundColor(Color.parseColor(setting.getBottomViewColor()));

        //height
        LinearLayout.LayoutParams paramBottoms = (LinearLayout.LayoutParams) viewBottom.getLayoutParams();
        paramBottoms.height = ((int)(CommonUtil.getScreenHeightAsPixel(self)*setting.getBottomViewHeight())/100);
        viewBottom.setLayoutParams(paramBottoms);

        //header of listview
        viewListHeader.setBackgroundColor(Color.parseColor(setting.getListViewHeaderBackground()));

        //height
        LinearLayout.LayoutParams paramsHeaderOfList = (LinearLayout.LayoutParams) viewListHeader.getLayoutParams();
        paramsHeaderOfList.height = ((int)(CommonUtil.getScreenHeightAsPixel(self)*setting.getRowViewHeight())/100);
        viewListHeader.setLayoutParams(paramsHeaderOfList);

        //text of header of list
        int textColor = Color.parseColor(setting.getFontColor());
        lblTicketNumber.setTextColor(textColor);
        lblPatientNameTitle.setTextColor(textColor);
        lblStatusTitle.setTextColor(textColor);

        float textSize = (paramsHeaderOfList.height*46/100)/ getResources().getDisplayMetrics().scaledDensity;
        lblTicketNumber.setTextSize(textSize);
        lblPatientNameTitle.setTextSize(textSize);
        lblStatusTitle.setTextSize(textSize);

        lblTicketNumber.setText(setting.getTextTicketTitle());
        lblPatientNameTitle.setText(setting.getTextNameTitle());
        lblStatusTitle.setText(setting.getTextStatusTitle());

        //listview
        listView.setBackgroundColor(Color.parseColor(setting.getBackground()));

        LocalBroadCastUtil.registerBroadCast(self,broadcastReceiverRefreshLayout,LocalBroadCastUtil.ACTION_STOP_LOAD_HIS);
    }

    protected void initControl() {

        loadHisData();
        ((MainApplication)getApplication()).getAppHandler().post(runnableReloadHISData);
    }

    @Override
    void refreshData(LayoutFrameObj data) {
        this.frameObj = data;
        mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        CommonUtil.log(TAG, TAG + " :onAttach");
    }

    @Override
    public void onDetach() {

        super.onDetach();
        CommonUtil.log(TAG, TAG + " :onDetach");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        CommonUtil.log(TAG, TAG + " :onDestroy - remove runnableReloadHISData");

        ((MainApplication)getApplication()).getAppHandler().removeCallbacks(runnableReloadHISData);
        LocalBroadCastUtil.unRegisterBroadCast(self,broadcastReceiverRefreshLayout);
    }

    private void loadHisData(){

        if(MainApplication.counterCallingHisData < maxDelayRefreshTime) return;

        MainApplication.counterCallingHisData = 0;

       // MainApplication.isCallingHisApi = true;

        AppData().requestHisData(new IModelListener() {
            @Override
            public void onSuccess(Object obj) {

                hisDataWrapper = ParseUtility.parseHISData(obj.toString());

                if(!hisDataWrapper.getTitle().isEmpty()){
                    AppData.getInstance(self).putStringValue("cache_his",obj.toString());
                    counterDataEmpty = 0;
                } else {

                    if(counterDataEmpty < maxCounterDataEmpty) {
                        counterDataEmpty++;
                        String data = AppData.getInstance(self).getStringValue("cache_his");
                        if(!data.isEmpty()) {
                            hisDataWrapper = ParseUtility.parseHISData(data);
                        }
                    } else {
                        //remove cache json
                        counterDataEmpty = 0;
                        AppData.getInstance(self).putStringValue("cache_his","");
                    }

                }

                displayHISContent();

            }

            @Override
            public void onError(Throwable ex) {
                //still use old data

                if(counterDataEmpty < maxCounterDataEmpty) {
                    counterDataEmpty++;
                    String data = AppData.getInstance(self).getStringValue("cache_his");
                    hisDataWrapper = ParseUtility.parseHISData(data);
                } else {
                    counterDataEmpty = 0;
                    AppData.getInstance(self).putStringValue("cache_his","");
                    hisDataWrapper = ParseUtility.parseHISData("");
                }

                    displayHISContent();

            }
        });
    }

    private void displayHISContent(){

        if(PatientListFragment.this.isDetached()) return;

        String strTitle = "";

        if(hisDataWrapper != null && !hisDataWrapper.getTitle().isEmpty()){
            strTitle = hisDataWrapper.getTitle();
            CacheManager.cacheRoomTitle(self,hisDataWrapper.getTitle());

            if(!hisDataWrapper.getDoctorName().isEmpty()) {

                CacheManager.cacheDoctorName(self,hisDataWrapper.getDoctorName());

                strTitle += " (" + hisDataWrapper.getDoctorName() + ")";
            } else {
                CacheManager.cacheDoctorName(self,"");
            }

        } else {

            if(!CacheManager.getCacheRoomTitle(self).isEmpty()){
                strTitle = CacheManager.getCacheRoomTitle(self);

                if(!CacheManager.getCacheDoctorName(self).isEmpty()){
                    strTitle += " (" + CacheManager.getCacheDoctorName(self) + ")";
                }

            } else {
                strTitle = getString(R.string.danh_sach_benh_nhan);
            }

        }
        lblTitle.setText(strTitle);

        //clear current list
        arrPatients.clear();
        if(hisDataWrapper != null && hisDataWrapper.getArrPatients().size() > 0){
            arrPatients.addAll(hisDataWrapper.getArrPatients());
        } else {
            arrPatients.add(new PatientObj());
        }


        mAdapter.notifyDataSetChanged();

        //set reload his data content
        ((MainApplication)getApplication()).getAppHandler().postDelayed(runnableReloadHISData, AppData.getInstance().getServerSetting().getRefreshDataTime());
    }





}
