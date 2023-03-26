package com.stech.smartads.adapters;

import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.stech.smartads.R;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.utils.ImageUtil;
import com.stech.smartads.components.textview.TextViewRegular;
import com.stech.smartads.utils.CommonUtil;

import java.util.List;


public class NewsListAdapter extends BaseAdapter {

    private static final String TAG = NewsListAdapter.class.getSimpleName();

    private AppCompatActivity context;
    private List<DataContentObj> newsObjs;

    public NewsListAdapter(AppCompatActivity context, List<DataContentObj> newsObjs) {
        this.context = context;
        this.newsObjs = newsObjs;
    }


    @Override
    public int getCount() {
        try {
            return newsObjs.size();
        } catch (NullPointerException ex) {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return newsObjs.get(position);
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_news, parent, false);
            viewHolder.lblPosition = (TextViewRegular) convertView.findViewById(R.id.lbl_position);
            viewHolder.lblTitle = (TextViewRegular) convertView.findViewById(R.id.lbl_title);
//            lblTitle.setSelected(true);
            viewHolder.lblDesc = (TextViewRegular) convertView.findViewById(R.id.lbl_desc);
            viewHolder.img = (ImageView) convertView.findViewById(R.id.img);
            viewHolder.divider = convertView.findViewById(R.id.divider);
            // Cache the viewHolder object inside the fresh view
            convertView.setTag(viewHolder);
        } else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ItemViewHolder) convertView.getTag();
        }


            CommonUtil.log(TAG,"item :"+ position);
            DataContentObj obj = newsObjs.get(position);
            if (obj != null) {
                int pos = position + 1;
                viewHolder.lblPosition.setText(String.valueOf(pos < 10 ? "0" + pos : pos));
                viewHolder.lblTitle.setText(obj.getTitle());
//                holder.lblDesc.setText(obj.getDescription());
                ImageUtil.setImage(context, viewHolder.img, obj.getUrl());

                if (position < getCount() - 1) {
                    viewHolder.divider.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.divider.setVisibility(View.GONE);
                }
            }

        return convertView;
    }

    private class ItemViewHolder {
        TextViewRegular lblPosition, lblTitle, lblDesc;
         ImageView img;
         View divider;

    }
}
