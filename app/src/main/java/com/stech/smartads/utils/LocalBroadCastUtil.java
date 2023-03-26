package com.stech.smartads.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class LocalBroadCastUtil extends BaseUtil {

	//system action
	public final static String ACTION_SYSTEM_NETWORK_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";

	//register action key
	public final static String ACTION_DOWNLOAD_FILE_COMPLETED = "ACTION_DOWNLOAD_FILE_COMPLETED";
	public final static String ACTION_REFRESH_LAYOUT = "ACTION_REFRESH_LAYOUT";
	public final static String ACTION_SHOW_CURRENT_TIME = "ACTION_SHOW_CURRENT_TIME";
	public final static String ACTION_STOP_VIDEO_PLAYER = "ACTION_STOP_VIDEO_PLAYER";
	public final static String ACTION_SHOW_DEFAULT_SCREEN= "ACTION_SHOW_DEFAULT_SCREEN";
	public final static String ACTION_CONNECT_SERVER_SUCCESSFUL = "ACTION_CONNECT_SERVER_SUCCESSFUL";
	public final static String ACTION_RELOAD_APP = "ACTION_RELOAD_APP";

	public final static String ACTION_LOAD_SETTING_SCREEN = "ACTION_LOAD_SETTING_SCREEN";
	public final static String ACTION_LOAD_DEFAULT_SCREEN = "ACTION_LOAD_DEFAULT_SCREEN";

	public final static String ACTION_CONTINUE_LOAD_HIS = "ACTION_CONTINUE_LOAD_HIS";
	public final static String ACTION_STOP_LOAD_HIS = "ACTION_STOP_LOAD_HIS";
	public final static String ACTION_CALL_REFRESH_SCHEDULE = "ACTION_CALL_REFRESH_SCHEDULE";


	public static void registerBroadCast(Context context, BroadcastReceiver broadcastReceiver, String... actions) {
		for (String action:actions) {
			LocalBroadcastManager.getInstance(context).registerReceiver(broadcastReceiver,new IntentFilter(action));
		}
	}

	public static void sendBroadcastListener(Context context, String key) {
		if (key.equals(ACTION_RELOAD_APP)
				|| key.equals(ACTION_CALL_REFRESH_SCHEDULE)
				|| key.equals(ACTION_CONNECT_SERVER_SUCCESSFUL)
				|| key.equals(ACTION_SYSTEM_NETWORK_CHANGED)

		) {
			CommonUtil.error("sendBroadcastListener", key);
		}
		//CommonUtil.error("sendBroadcastListener", key);
		Intent intent = new Intent(key);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public static void sendBroadcastListener(Context context, String key, Bundle bundle) {
		if (key.equals(ACTION_RELOAD_APP)
				|| key.equals(ACTION_CALL_REFRESH_SCHEDULE)
				|| key.equals(ACTION_CONNECT_SERVER_SUCCESSFUL)
				|| key.equals(ACTION_SYSTEM_NETWORK_CHANGED)

		) {
			CommonUtil.error("sendBroadcastListener", key);
		}
		//CommonUtil.error("sendBroadcastListener", key);
		Intent intent = new Intent(key);
		intent.putExtras(bundle);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}

	public static void unRegisterBroadCast(Context context, BroadcastReceiver broadcastReceiver) {
		LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver);
	}
}
