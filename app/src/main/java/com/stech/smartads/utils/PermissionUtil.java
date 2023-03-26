package com.stech.smartads.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * Created by NaPro on 12/01/2015.
 */
public class PermissionUtil extends BaseUtil {

    private static boolean isMarshmallow() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }

    /**
     * Check permissions in runtime
     *
     * @param activity
     * @param permissions
     * @param reqCode
     * @param notification
     * @return
     */
    public static boolean isGranted(Activity activity, String[] permissions, int reqCode, String notification) {
        boolean granted = true;

        if (isMarshmallow()) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];

                granted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    if (notification != null && notification.length() > 0) {
                        //CommonUtil.message(activity, notification);
                    }
                    break;
                }
            }

            // Ask permissions
            if (!granted) {
                ActivityCompat.requestPermissions(activity, permissions, reqCode);
            }
        }

        return granted;
    }

    /**
     * Check permissions in runtime
     *
     * @param context
     * @param fragment
     * @param permissions
     * @param reqCode
     * @param notification
     * @return
     */
    public static boolean isGranted(Context context, Fragment fragment, String[] permissions, int reqCode, String notification) {
        boolean granted = true;

        if (isMarshmallow()) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];

                granted = ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
                if (!granted) {
                    if (notification != null && notification.length() > 0) {
                        CommonUtil.message(context, notification);
                    }
                    break;
                }
            }

            // Ask permissions
            if (!granted) {
                fragment.requestPermissions(permissions, reqCode);
            }
        }

        return granted;
    }

    /**
     * Check location is granted or not in run time
     *
     * @param activity
     * @param reqCode
     * @param notification [optional] can be null/empty if you don't want to notify user
     * @return true if location is granted
     */
    public static boolean locationIsGranted(Activity activity, int reqCode, String notification) {
        boolean granted = true;

        if (isMarshmallow()) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];

                granted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
                if (granted) {
                    break;
                }
            }

            // Ask permissions
            if (!granted) {
                if (notification != null && notification.length() > 0) {
                    CommonUtil.message(activity, notification);
                }

                ActivityCompat.requestPermissions(activity, permissions, reqCode);
            }
        }

        return granted;
    }

    /**
     * Check CALL_PHONE is granted or not in run time
     *
     * @param activity
     * @param reqCode
     * @param notification [optional] can be null/empty if you don't want to notify user
     * @return true if CALL_PHONE is granted
     */
    public static boolean callPhoneIsGranted(Activity activity, int reqCode, String notification) {
        boolean granted = true;

        if (isMarshmallow()) {
            String[] permissions = new String[]{Manifest.permission.CALL_PHONE};
            String permission = permissions[0];

            granted = ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
            if (!granted) {
                if (notification != null && notification.length() > 0) {
                    CommonUtil.message(activity, notification);
                }
            }

            // Ask permissions
            if (!granted) {
                ActivityCompat.requestPermissions(activity, permissions, reqCode);
            }
        }

        return granted;
    }
}
