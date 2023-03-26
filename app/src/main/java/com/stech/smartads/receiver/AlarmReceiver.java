package com.stech.smartads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stech.smartads.activities.MainActivity;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.utils.CommonUtil;


public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		try{
			CommonUtil.log("kiemdv", "/receiver/AlarmReceiver:");
			MainActivity activity = (MainActivity) ((MainApplication) context.getApplicationContext()).getMainActivity();
			activity.getLayoutSetting();
		} catch (Exception ex){
			CommonUtil.error(ex);
		}
	}

}
