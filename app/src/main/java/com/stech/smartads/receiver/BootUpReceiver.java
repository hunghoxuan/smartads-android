package com.stech.smartads.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.stech.smartads.activities.MainActivity;
import com.stech.smartads.activities.SettingActivity;
import com.stech.smartads.activities.SplashActivity;


public class BootUpReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
			startApplication(context);
		}
	}

	private void startApplication(Context context) {
		Intent i = new Intent(context, SplashActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(i);
	}
}
