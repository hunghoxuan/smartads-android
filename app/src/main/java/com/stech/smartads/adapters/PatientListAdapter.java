package com.stech.smartads.adapters;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stech.smartads.R;
import com.stech.smartads.core.AppData;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.models.PatientObj;
import com.stech.smartads.utils.CommonUtil;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public class PatientListAdapter extends BaseAdapter {

    private static final String TAG = PatientListAdapter.class.getSimpleName();

    private Activity context;
    private List<PatientObj> arrPatients;
    private Animation anim;
    private ServerSetting screenSetting;

    public PatientListAdapter(AppCompatActivity context, List<PatientObj> arr) {
        this.context = context;
        this.arrPatients = arr;
        this.anim = initAnimation();
        this.screenSetting = AppData.getInstance().getServerSetting();

        CommonUtil.log(TAG, "rowViewHeight :"+this.screenSetting.getRowViewHeight());
    }


    @Override
    public int getCount() {
        try {
            return arrPatients.size();
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return arrPatients.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        ItemViewHolder viewHolder;
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ItemViewHolder();

            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_patients_list, parent, false);
            viewHolder.lblTicketNumber = (TextView) convertView.findViewById(R.id.lblTicketNumber);
            viewHolder.lblPatientName = (TextView) convertView.findViewById(R.id.lblPatientName);
            viewHolder.lblStatus = (TextView) convertView.findViewById(R.id.lblStatus);
            viewHolder.imgDangKhamStatus = convertView.findViewById(R.id.imgDangKhamStatus);
            viewHolder.viewRow = convertView.findViewById(R.id.viewRow);

//            //set size of row
//            //height

            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) viewHolder.viewRow.getLayoutParams();
            params.height = ((int)(CommonUtil.getScreenHeightAsPixel(context)*screenSetting.getRowViewHeight())/100);
            viewHolder.viewRow.setLayoutParams(params);
//
//            //setting text
            float textSize = (params.height*45/100)/ context.getResources().getDisplayMetrics().scaledDensity;
            viewHolder.lblTicketNumber.setTextSize(textSize);
            viewHolder.lblPatientName.setTextSize(textSize);
            viewHolder.lblStatus.setTextSize(textSize);

            //set image size
            ViewGroup.LayoutParams paramImage =  viewHolder.imgDangKhamStatus.getLayoutParams();
            paramImage.height = params.height*70/100;
            viewHolder.imgDangKhamStatus.setLayoutParams(paramImage);

//
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ItemViewHolder) convertView.getTag();
        }

        PatientObj patient = arrPatients.get(position);
        if (patient != null) {

            if(patient.getTicketNumber() != -1) {
                viewHolder.lblTicketNumber.setText(String.valueOf(patient.getTicketNumber()));
                viewHolder.lblPatientName.setText(patient.getName());


                if (patient.isCalling()) {
                    viewHolder.lblStatus.setText(context.getText(R.string.status_calling));

                    //set color
                    viewHolder.lblPatientName.setTextColor(Color.WHITE);
                    viewHolder.lblTicketNumber.setTextColor(Color.WHITE);
                    viewHolder.lblStatus.setTextColor(Color.WHITE);
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));

                    viewHolder.lblStatus.setVisibility(View.GONE);
                    viewHolder.imgDangKhamStatus.setVisibility(View.VISIBLE);
                    viewHolder.imgDangKhamStatus.setBackgroundResource(R.drawable.img_moi_vao_kham);

                } else {
                    viewHolder.lblStatus.setText(context.getText(R.string.status_waiting));

                    viewHolder.lblPatientName.setTextColor(Color.BLACK);
                    viewHolder.lblTicketNumber.setTextColor(Color.BLACK);
                    viewHolder.lblStatus.setTextColor(Color.BLACK);
                    convertView.setBackgroundColor(context.getResources().getColor(R.color.white));

                    viewHolder.lblStatus.setVisibility(View.GONE);
                    viewHolder.imgDangKhamStatus.setVisibility(View.VISIBLE);
                    viewHolder.imgDangKhamStatus.setBackgroundResource(R.drawable.img_doi_kham);
                }
            } else {
                viewHolder.lblTicketNumber.setText("...");
                viewHolder.lblPatientName.setText("...");
                viewHolder.lblStatus.setText("...");

                viewHolder.lblStatus.setVisibility(View.VISIBLE);
                viewHolder.imgDangKhamStatus.setVisibility(View.GONE);

            }
        }

        return convertView;
    }

    private Animation initAnimation(){
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        anim.setStartOffset(0);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        return anim;
    }

    private class ItemViewHolder {
        TextView lblTicketNumber, lblPatientName, lblStatus;
        GifImageView imgDangKhamStatus;
        LinearLayout viewRow;
    }
}
