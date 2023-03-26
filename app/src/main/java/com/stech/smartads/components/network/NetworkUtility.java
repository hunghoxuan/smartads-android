/*
 * Name: $RCSfile: NetworkUtility.java,v $
 * Version: $Revision: 1.1 $
 * Date: $Date: Oct 31, 2011 3:57:18 PM $
 *
 * Copyright (C) 2011 COMPANY_NAME, Inc. All rights reserved.
 */

package com.stech.smartads.components.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.provider.Settings;

import com.stech.smartads.config.Constants;
import com.stech.smartads.utils.CommonUtil;

/**
 * NetworkUtility checks available network
 *
 * @author Lemon
 */
public final class NetworkUtility {
    private Context context = null;

    private static NetworkUtility instance = null;
    private boolean isOnline = false;
    private boolean isOffline = false;
    private boolean isFirstCheck = true;

    /**
     * Constructor
     *
     * @param context
     */
    private NetworkUtility(Context context) {
        this.context = context;
    }

    /**
     * Get class instance
     *
     * @param context
     * @return
     */
    public static NetworkUtility getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkUtility(context);
            instance.checkNetwork();
        }
        return instance;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setOffline(boolean offline) {
        setOffline(offline, "");
    }

    public void setOffline(boolean offline, String msg) {
        if (!msg.trim().isEmpty() && !msg.trim().endsWith("."))
            msg = msg.trim() + ". ";

        if (!isOffline && offline)
            CommonUtil.message(this.context, msg + Constants.TEXT_NETWORK_CONDITION + ": " + Constants.TEXT_OFFLINE);
        else if (isOffline && !offline)
            CommonUtil.message(this.context, msg + Constants.TEXT_NETWORK_CONDITION + ": " + Constants.TEXT_ONLINE);
        isOffline = offline;
        //isOnline = !isOffline;
        isFirstCheck = false;
    }

    public void setOffline() {
        setOffline(true);
        setOnline(false);
    }

    public void setOnline() {
        setOnline(true);
    }

    public void setOnline(boolean online) {
        setOnline(online, "");
    }

    public void setOnline(boolean online, String msg) {
        if (!msg.trim().isEmpty() && !msg.trim().endsWith("."))
            msg = msg.trim() + ". ";

        if (!isOnline && online)
            CommonUtil.message(this.context, msg + Constants.TEXT_NETWORK_CONDITION + ": " + Constants.TEXT_SUCCESSFUL);
        else if (isOnline && !online)
            CommonUtil.message(this.context, msg + Constants.TEXT_NETWORK_CONDITION + ": " + Constants.TEXT_DISCONNECTED);

        isOnline = online;

        isFirstCheck = false;
    }

    public boolean isNetworkAvailable() {
        return isOnline();
    }

    /**
     * Check network connection
     *
     * @return
     */
    public boolean checkNetwork() {
        try {

            ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo i = conMgr.getActiveNetworkInfo();
            if (i == null || !i.isAvailable() || !i.isConnected()) {
                setOnline(false);
            } else {
                setOnline(true);
            }

            return isOnline;
        } catch (Exception ex) {
            CommonUtil.message(context, Constants.TEXT_NETWORK_CONDITION + " " + Constants.TEXT_ERROR + ": " + ex.getMessage());
            setOnline(false);

            return false;
        }
    }


    // turn on use network for location
    public void turnNetWorkLocationOn(Activity activity) {
        String provider = Settings.Secure.getString(
                activity.getContentResolver(),
                Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) { // if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings",
                    "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("1"));
            activity.sendBroadcast(poke);
        }
        // String provider = Settings.Secure.getString(
        // activity.getContentResolver(),
        // Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        // CommonUtil.log("provider", "ok " + provider);
        // final Intent poke = new Intent();
        // poke.setClassName("com.android.settings",
        // "com.android.settings.widget.SettingsAppWidgetProvider");
        // poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
        // poke.setData(Uri.parse("3"));
        // activity.sendBroadcast(poke);
        // Settings.Secure.setLocationProviderEnabled(
        // activity.getContentResolver(),
        // LocationManager.NETWORK_PROVIDER, true);
    }

    // turn on GPS
    // public void turnGPSOn(Activity activity) {
    //
    // String provider = Settings.Secure.getString(
    // activity.getContentResolver(),
    // Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
    // if (!provider.contains("gps")) { // if gps is disabled
    // final Intent poke = new Intent();
    // poke.setClassName("com.android.settings",
    // "com.android.settings.widget.SettingsAppWidgetProvider");
    // poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
    // poke.setData(Uri.parse("3"));
    // activity.sendBroadcast(poke);
    // }
    //
    // }
}
