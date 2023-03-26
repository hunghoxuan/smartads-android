package com.stech.smartads.utils;

import android.content.Context;

import com.stech.smartads.core.AppData;
import com.stech.smartads.models.ServerSetting;

public class BaseUtil {
    public static AppData AppData(Context ctx) {
        return AppData.getInstance(ctx);
    }

    public static ServerSetting Settings(Context ctx) {
        return AppData(ctx).getServerSetting();
    }
}
