/*
 * Name: $RCSfile: PacketUtility.java,v $
 * Version: $Revision: 1.1 $
 * Date: $Date: Nov 15, 2011 2:05:59 PM $
 *
 * Copyright (C) 2011 COMPANY_NAME, Inc. All rights reserved.
 */
package com.stech.smartads.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;

import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.core.AppData;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PacketUtility extends BaseUtil {
    /**
     * Constructor
     */
    public PacketUtility() {
    }

    /**
     * Get package name
     *
     * @return
     */
    public String getPackageName() {
        return this.getClass().getPackage().getName();
    }

    public static String getVersionName(Context context){
        String versionName = "not available";
        try {
            versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            CommonUtil.error(e);
        }

        return versionName;
    }

    public static int getVersionCode(Context context){
        int versionCode = 0;
        try {
            versionCode = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            CommonUtil.error(e);
        }

        return versionCode;
    }

    public static boolean checkLicense(Context context) {
        if (AppConfigs.APP_NAME.equalsIgnoreCase("DEMO") || AppConfigs.APP_NAME.isEmpty() || AppConfigs.LICENSE_KEY.equalsIgnoreCase("NONE"))
            return true;
        String license = CacheManager.getLicenseKey(context);
        String appLicense = PacketUtility.getLicense(context, true); // license with year
        String appLicense2 = PacketUtility.getLicense(context, false); // without year (permanent license)

        if (license.equals(getHexStringFromString(appLicense)) || license.equals(getHexStringFromString(appLicense2)))
            return true;

        return false;
    }

    public static String getHexStringFromString(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes("UTF-8"));
            String hashString = getHexStringFromHashArray(hash);
            return hashString;

        } catch (NoSuchAlgorithmException e) {
            return "";
        } catch (UnsupportedEncodingException ex) {
            return "";
        }
    }

    public static String getHexStringFromHashArray(byte[] hash) {
        String result = "";
        String hashString = new String();
        for (int i=0;i<hash.length;i++) {
            int b =  (0xFF & hash[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF) result+="0";
            // add number to string
            result+=Integer.toHexString(b);
        }
        // hex string to uppercase
        result = result.toUpperCase();
        return result;
    }

    public static String getDeviceUniqueID(Context context){

        String deviceID = AppData.getInstance(context).getStringValue("DEVICE_ID");
        if(!deviceID.isEmpty()){
            return deviceID;
        }

        //Process when no deviceID is saved
        //get Android ID
        String m_szAndroidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);

        // get Pseudo-Unique ID
        String m_szDevIDShort = "35" + //we make this look like a valid IMEI
                Build.BOARD.length()%10+ Build.BRAND.length()%10 +
                Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 +
                Build.DISPLAY.length()%10 + Build.HOST.length()%10 +
                Build.ID.length()%10 + Build.MANUFACTURER.length()%10 +
                Build.MODEL.length()%10 + Build.PRODUCT.length()%10 +
                Build.TAGS.length()%10 + Build.TYPE.length()%10 +
                Build.USER.length()%10 ; //13 digits

        //The WLAN MAC Address string
        WifiManager wm = (WifiManager)context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();

        //get bluetooth
//        BluetoothAdapter m_BluetoothAdapter	= null; // Local Bluetooth adapter
//        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        String m_szBTMAC = m_BluetoothAdapter.getAddress();


        String m_szLongID = m_szDevIDShort + m_szAndroidID+ m_szWLANMAC /*+ m_szBTMAC*/;
        // compute md5
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            CommonUtil.error(e);
        }
        m.update(m_szLongID.getBytes(),0,m_szLongID.length());
        // get md5 bytes
        byte p_md5Data[] = m.digest();
        // create a hex string
        String m_szUniqueID = getHexStringFromHashArray(p_md5Data);

        //Save to preference
        AppData.getInstance(context).putStringValue("DEVICE_ID", m_szUniqueID);
        return m_szUniqueID;
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String getMacAddress() {
        return getDeviceName();
    }

    public static int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    public static int getScreenOrientation() {
        return Resources.getSystem().getConfiguration().orientation;
    }

    public static String getScreenName() {
        int orientation = getScreenOrientation();
        String result = "";
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            result = "Portrait";
        else
            result = "Landscape";

        return result + " ( " + getScreenWidth() + "px x "  + getScreenHeight() + "px )";
    }

    public static String getLicense(Context context, boolean withYear) {
        if (withYear)
            return AppConfigs.APP_NAME + '.' + getDeviceUniqueID(context) + "." + DateTimeUtil.convertTimeStampToDate(DateTimeUtil.getCurrentTime(DateTimeUtil.MILLISECOND), "yyyy");
        return AppConfigs.APP_NAME + '.' + getDeviceUniqueID(context);
    }

    public static String getLicense(Context context) {
        return getLicense(context, true);
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

}
