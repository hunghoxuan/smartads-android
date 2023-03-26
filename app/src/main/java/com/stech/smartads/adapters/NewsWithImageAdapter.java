package com.stech.smartads.adapters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


import com.squareup.picasso.Picasso;
import com.stech.smartads.R;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.utils.ImageUtil;
import com.stech.smartads.components.autoscrolltwoway.CircularLoopAdapter;
import com.stech.smartads.components.textview.TextViewRegular;


public class NewsWithImageAdapter extends CircularLoopAdapter {
	private List<DataContentObj> listPlaces = new ArrayList<DataContentObj>();
	private Context mContext;
	private LayoutInflater inflater;
	private String fontColor ="";

	public NewsWithImageAdapter(Context context, List<DataContentObj> list, String fontColor) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.fontColor = fontColor;
		initData(list);
		inflater = LayoutInflater.from(mContext);
	}

	public void setFontColor(String fontColor){
		this.fontColor = fontColor;
		notifyDataSetChanged();
	}

	private void initData(List<DataContentObj> sub) {
		this.listPlaces.clear();
		this.listPlaces.addAll(sub);
		notifyDataSetChanged();
	}

	@Override
	public DataContentObj getItem(int arg0) {
		// TODO Auto-generated method stub
		return listPlaces.get(getCircularPosition(arg0));
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ItemViewHolder viewHolder;
		if (convertView == null) {
			// If there's no view to re-use, inflate a brand new view for row
			viewHolder = new ItemViewHolder();
			convertView = inflater.inflate(R.layout.item_news, parent, false);
			viewHolder.lblPosition = (TextViewRegular) convertView.findViewById(R.id.lbl_position);
			viewHolder.lblPosition.setVisibility(View.GONE);
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

		DataContentObj obj = listPlaces.get(getCircularPosition(position));
		if (obj != null) {
			int pos = position + 1;
			viewHolder.lblPosition.setText(String.valueOf(pos < 10 ? "0" + pos : pos));
			viewHolder.lblPosition.setTextColor(Color.parseColor(fontColor));
			viewHolder.lblTitle.setText(obj.getTitle());
			viewHolder.lblTitle.setTextColor(Color.parseColor(fontColor));


			//check image
			String url = obj.getPlayUrl(mContext);

			if(url.startsWith("http")) {
				ImageUtil.setImage(mContext, viewHolder.img, url);
			}else{
				File f = new File(url);
				Picasso.with(mContext).load(f).into(viewHolder.img);
			}


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

	@Override
	protected int getCircularCount() {
		// TODO Auto-generated method stub
		return listPlaces.size();
	}
}