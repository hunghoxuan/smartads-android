package com.stech.smartads.receiver;

import java.io.IOException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.stech.smartads.utils.CommonUtil;

public class InternetReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		if (isConnected())
			CommonUtil.log("NetReceiver", "Internet is connected");
		else
			CommonUtil.log("NetReceiver", "Internet is not connected");
	}

	public boolean isConnected() {
		Runtime runtime = Runtime.getRuntime();
		try {

			Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
			int exitValue = ipProcess.waitFor();
			return (exitValue == 0);

		} catch (IOException e) {
			CommonUtil.error(e);
		} catch (InterruptedException e) {
			CommonUtil.error(e);
		}

		return false;
	}
}