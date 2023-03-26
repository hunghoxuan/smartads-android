package com.stech.smartads.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.stech.smartads.R;
import com.stech.smartads.models.DataContentObj;
import com.stech.smartads.components.autoscrolltwoway.CircularLoopAdapter;

import java.util.ArrayList;
import java.util.List;

public class AdsAutoScrollAdapter extends CircularLoopAdapter {

	private List<DataContentObj> mItems = new ArrayList<DataContentObj>();
	private Context mContext;

	public AdsAutoScrollAdapter(List<DataContentObj> list, Context context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		initArray(list);
	}

	private void initArray(List<DataContentObj> list) {
		mItems.clear();
		mItems.addAll(list);
	}

	@Override
	public DataContentObj getItem(int position) {
		// TODO Auto-generated method stub
		return mItems.get(getCircularPosition(position));
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub

		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.item_list_text_ads, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (getItem(position) != null) {
			holder.text.setText(Html.fromHtml(getItem(position).getDescription()));
		}
		return convertView;
	}

	@Override
	protected int getCircularCount() {
		// TODO Auto-generated method stub
		return mItems.size();
	}

	static class ViewHolder {
		TextView text;

		public ViewHolder(View convertView) {
			text = (TextView) convertView.findViewById(R.id.txtAds);
		}
	}

}
