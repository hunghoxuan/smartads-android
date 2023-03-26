package com.stech.smartads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.utils.CommonUtil;

public class NetworkReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (NetworkUtility.getInstance(context).isOnline()) {
			CommonUtil.log("xxxx", "Connected");
		} else {
			CommonUtil.log("xxxx", "Not connected");
		}
	}

}
