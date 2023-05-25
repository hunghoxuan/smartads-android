package com.stech.smartads.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.NetworkOnMainThreadException;
import android.os.StatFs;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.splunk.mint.Mint;
import com.stech.smartads.BuildConfig;
import com.stech.smartads.R;
import com.stech.smartads.activities.BaseActivity;
import com.stech.smartads.core.AppData;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.interfaces.IConfirmation;
import com.stech.smartads.models.AppVersionObj;
import com.stech.smartads.components.textview.TextViewRegular;
import com.stech.smartads.components.toast.BadTokenListener;
import com.stech.smartads.components.toast.ToastCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.NoRouteToHostException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommonUtil extends BaseUtil {
    public static void log(String tag, String msg) {
        if (!AppConfigs.isDebug)
            return;

        Log.i(tag, msg + " [ " + DateTimeUtil.showCurrentTime() + " ]");
    }

    public static String getErrorMessage(Throwable ex) {
        if (ex == null)
            return "";
        return ex.getMessage();
    }

    public static void error(String tag, String msg, Throwable e) {
        if (!isValidException(e))
            return;

        if (!AppConfigs.isDebug)
            return;
        Log.e(tag,  "[ " + DateTimeUtil.showCurrentTime() + " ] " + msg + StringUtil.getErrorMessage(e));
    }

    public static void error(String tag, String msg) {
        if (!AppConfigs.isDebug)
            return;
        Log.e(tag, "[ " + DateTimeUtil.showCurrentTime() + " ] " + msg);
    }

    public static void error(Context context, String msg) {
        error(context.getClass().getSimpleName(), msg);
        message(context, msg);
    }

    public static void error(BaseActivity activity, String msg) {
        error(activity.TAG, msg);
        message(activity, msg);
    }

    public static void error(Context context, Throwable exception) {
        if (!isValidException(exception))
            return;

        processThrowable(exception, context);
    }

    public static void error(Activity activity, Throwable exception) {
        if (!isValidException(exception))
            return;

        processThrowable(exception, (BaseActivity) activity);
    }

    public static void error(Throwable exception) {
        if (!isValidException(exception))
            return;

        processThrowable(exception);
    }

    public static void startActivity(final Activity currentActivity, final Class<?> nextActivityClass) {
        log("START ACTIVITY", currentActivity.getLocalClassName() + " - " + nextActivityClass.getSimpleName());
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    CommonUtil.startActivityLTR(currentActivity, nextActivityClass);
                    //finish();
                }
            }, 1000);
        } catch (Exception ex) {
            CommonUtil.error(ex);
            return;
        }
    }

    public static void openScreen(final Activity currentActivity, final Class<?> nextActivityClass) {
        startActivity(currentActivity, nextActivityClass);
    }

    public static void processThrowable(Throwable ex, BaseActivity activity) {
        if (ex == null)
            return;
        processThrowable(ex);
        message(activity, Constants.TEXT_ERROR + ": " + StringUtil.getErrorMessage(ex));
    }

    public static void processThrowable(Throwable ex, Context context) {
        if (ex == null)
            return;

        processThrowable(ex);
        message(context, Constants.TEXT_ERROR + ": " + StringUtil.getErrorMessage(ex));
    }

    public static void processThrowable(Throwable ex) {
        if (ex == null)
            return;

        if (AppConfigs.isDebug)
            ex.printStackTrace();

        error(Constants.TEXT_ERROR + ": ", StringUtil.getErrorMessage(ex));

//        if (AppData.getInstance().getServerSetting().isDebug()) {
//            Mint.logException(new Exception(ex));
//            Mint.flush();
//        }
    }

    public static boolean isValidException(Throwable ex) {
        if (ex == null)
            return false;
        if (ex instanceof WindowManager.BadTokenException)
            return false;

//        if (ex instanceof NullPointerException)
//            return false;

        if (ex instanceof ExoPlaybackException)
            return false;

        if (ex instanceof NetworkOnMainThreadException)
            return false;

        if (ex instanceof NoRouteToHostException)
            return false;

        if (ex instanceof NoConnectionError)
            return false;

        if (ex instanceof ParseError)
            return false;

        if (ex instanceof TimeoutError)
            return false;

        if (ex instanceof ClientError)
            return false;

        if (ex instanceof NetworkError)
            return false;

        if (ex instanceof ServerError)
            return false;

        if (ex instanceof AuthFailureError)
            return false;

        return true;
    }

    public static void warning(String tag, String msg) {
        if (!AppConfigs.isDebug)
            return;

        Log.w(tag, msg + " [ " + DateTimeUtil.showCurrentTime() + " ]");
    }

    public static void message(Context context, final String msg) {
        try {
            String tag;
            if (context == null)
                tag = "MESSAGE";
            else
                tag = context.getClass().getSimpleName();
            if (AppConfigs.isDebug)
                error(tag, msg + " [ " + DateTimeUtil.showCurrentTime() + " ]");

            if (isContextValid(context, null)) {
                try {

                    if (android.os.Build.VERSION.SDK_INT == 25) {
                        ToastCompat.makeText(context, msg, Toast.LENGTH_SHORT)
                                .setBadTokenListener(new BadTokenListener() {
                                    @Override
                                    public void onBadTokenCaught(@NonNull Toast toast) {
                                        /*ignore */
                                    }
                                }).show();
                    } else {
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                    }
                } catch (WindowManager.BadTokenException e) {
                    /* ignore */
                }
            }
        } catch (Exception ex) {
            /* ignore */
        }
    }

    public static void message(Activity activity, String msg) {
        try {
            if (msg == null || msg.isEmpty())
                return;
            msg = msg.substring(0,1).toUpperCase() + msg.substring(1);
            String tag;
            if (activity == null)
                tag = "MESSAGE";
            else
                tag = activity.getClass().getSimpleName();
            Log.e(tag, msg + " [ " + DateTimeUtil.showCurrentTime() + " ]");
            if (isContextValid(activity, null)) {
                if (android.os.Build.VERSION.SDK_INT == 25) {
                    ToastCompat.makeText(activity, msg, Toast.LENGTH_SHORT)
                            .setBadTokenListener(new BadTokenListener() {
                                @Override
                                public void onBadTokenCaught(@NonNull Toast toast) {
                                    /*ignore */
                                }
                            }).show();
                } else {
                    Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception ex) {
            /* ignore */
        }
    }

    public static void commitTransaction(FragmentTransaction transaction) {
        try {
            if (transaction != null)
                transaction.commitAllowingStateLoss();
            //transaction.commit();
        } catch (IllegalStateException e) {
            /* ignore */
        }
    }

    public static void showConfirmationDialog(Context context, String msg, String positive, String negative,
                                              boolean isCancelable, final IConfirmation iConfirmation) {
        if (context != null) {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_confirmation);

            TextViewRegular lblMsg = (TextViewRegular) dialog.findViewById(R.id.lbl_msg);
            TextViewRegular lblNegative = (TextViewRegular) dialog.findViewById(R.id.lbl_negative);
            TextViewRegular lblPositive = (TextViewRegular) dialog.findViewById(R.id.lbl_positive);

            lblMsg.setText(msg);
            lblNegative.setText(negative);
            lblPositive.setText(positive);

            lblNegative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        iConfirmation.onNegative();
                    } catch (final IllegalArgumentException e) {
                        // Do nothing.
                    } catch (final Exception e) {
                        // Do nothing.
                    } finally {
                    }
                }
            });

            lblPositive.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        if (dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        iConfirmation.onPositive();
                    } catch (final IllegalArgumentException e) {
                        // Do nothing.
                    } catch (final Exception e) {
                        // Do nothing.
                    } finally {
                    }
                }
            });

            dialog.setCancelable(isCancelable);

            if (!dialog.isShowing()) {
                dialog.show();
            }
        }
    }

    public static void enableStrictMode() {
        // Allow strict mode
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //disable trick  mode file uri
        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }catch(Exception e){
                //CommonUtil.error(e);
            }
        }
    }

    public static void startActivityLTR(Activity act, Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtras(bundle);
        act.startActivity(intent);
        act.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public static void startActivityLTR(Activity act, Class<?> clz) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivity(intent);
        act.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }

    public static void startActivityRTL(Activity act, Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtras(bundle);
        act.startActivity(intent);
        act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public static void startActivityRTL(Activity act, Class<?> clz) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivity(intent);
        act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    public static void startActivityWithoutAnimation(Context act, Class<?> clz) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivity(intent);
    }

    public static void startActivityWithoutAnimation(Context act, Class<?> clz, Bundle bundle) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtras(bundle);
        act.startActivity(intent);
    }

    public static void startActivityAsNewTask(Context act, Class<?> clz) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        act.startActivity(intent);
    }

    public static void startActivityForResult(Activity act, Class<?> clz, int reqCode, Bundle bundle) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtras(bundle);
        act.startActivityForResult(intent, reqCode);
    }

    public static void startActivityForResult(Activity act, Class<?> clz, int reqCode) {
        Intent intent = new Intent(act, clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivityForResult(intent, reqCode);
    }

    public static void startActivityForResult(Fragment act, Class<?> clz, int reqCode) {
        Intent intent = new Intent(act.getContext(), clz);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivityForResult(intent, reqCode);
    }

    public static void startActivityForResult(Fragment act, Class<?> clz, int reqCode, Bundle bundle) {
        Intent intent = new Intent(act.getContext(), clz);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        act.startActivityForResult(intent, reqCode);
    }

    /**
     * Close activity with right-to-left animation
     *
     * @param act
     */
    public static void finishActivity(Activity act) {
        act.finish();
        act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }

    /**
     * Force close keyboard of the given editText
     *
     * @param activity
     */
    public static void closeKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
        } catch (NullPointerException ex) {
            //CommonUtil.error(ex);
        }
    }

    public static void showKeyboard(Context ctx, EditText editText) {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
    }

    public static void closeKeyboardWhenClickingOutOfKeyboard(final View view) {
        if (view == null)
            return;

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    closeKeyboard((Activity) view.getContext());
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                closeKeyboardWhenClickingOutOfKeyboard(innerView);
            }
        }
    }

    /**
     * @param act
     * @return Width of screen by pixel
     */
    public static int getScreenWidthAsPixel(Activity act) {
        DisplayMetrics dm = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenWidth(Activity act) {
        return getScreenWidthAsPixel(act) - getScreenMarginLeft() - getScreenMarginRight();
    }

    public static int getScreenHeight(Activity act) {
        return getScreenHeightAsPixel(act) - getScreenMarginTop() - getScreenMarginBottom();
    }

    public static int getScreenMarginLeft() {
        return AppConfigs.SCREEN_MARGIN_LEFT;
    }

    public static int getScreenMarginRight() {
        return AppConfigs.SCREEN_MARGIN_RIGHT;
    }

    public static int getScreenMarginTop() {
        return AppConfigs.SCREEN_MARGIN_TOP;
    }

    public static int getScreenMarginBottom() {
        return AppConfigs.SCREEN_MARGIN_BOTTOM;
    }

    /**
     * @param act
     * @return Width of screen by pixel
     */
    public static int getScreenHeightAsPixel(Activity act) {
        if(AppData.getInstance().getScreenHeight()==0) {
            DisplayMetrics dm = new DisplayMetrics();
            act.getWindowManager().getDefaultDisplay().getMetrics(dm);
            return dm.heightPixels;
        }else{
            return AppData.getInstance().getScreenHeight();
        }
    }

    public static FrameLayout.LayoutParams getLayoutParams(int w, int h, int x, int y, int gravity) {
        FrameLayout.LayoutParams params =
                new FrameLayout.LayoutParams(w, h); //width and height
        if (gravity == Gravity.LEFT)
            params.leftMargin = x;
        else if (gravity == Gravity.RIGHT)
            params.rightMargin = x;

        params.topMargin = y;
        params.gravity = gravity;
        return params;
    }

    public static FrameLayout.LayoutParams getLayoutParams(int w, int h, int x, int y) {
        return getLayoutParams(w, h, x, y, Gravity.NO_GRAVITY);
    }

    public static FrameLayout.LayoutParams getLayoutParams(Activity context, String position, double w, double h) {
        return getLayoutParams(context, position, w, h, AppConfigs.SCREEN_PADDING_TOP_BOTTOM, AppConfigs.SCREEN_PADDING_LEFT_END);
    }

    public static FrameLayout.LayoutParams getLayoutParams(Activity context, String position, double w, double h, int margin_top, int margin_left) {
        int screenHeight = CommonUtil.getScreenHeightAsPixel(context);
        int screenWidth = CommonUtil.getScreenWidthAsPixel(context);
        return getLayoutParams(context, position,  w < 1 ? (int) w * screenWidth : (int) w, h < 1 ? (int) h * screenHeight : (int) h, margin_top, margin_left);
    }

    public static FrameLayout.LayoutParams getLayoutParams(Activity context, String position, int w, int h, int margin_top, int margin_left) {
        int screenHeight = CommonUtil.getScreenHeightAsPixel(context);
        int screenWidth = CommonUtil.getScreenWidthAsPixel(context);
        int x, y;
        int height = FrameLayout.LayoutParams.WRAP_CONTENT;
        int width = FrameLayout.LayoutParams.WRAP_CONTENT;
        int gravity = Gravity.NO_GRAVITY;

        if (w > 0 && w <= 1)
            width = (int) screenWidth * w;
        else if (w != 0)
            width = w;

        if (h > 0 && h <= 1)
            height = (int) screenHeight * h;
        else if (h != 0)
            height = h;

        position = position.trim().replace(" ", "").toLowerCase();

        String[] arr = new String[] {"top", "left"};
        if (position.contains("x"))
            arr = position.split("x");
        else if (position.contains("_"))
            arr = position.split("_");
        else if (position.contains(":"))
            arr = position.split(":");

        if (arr[0].equalsIgnoreCase("top") || arr[1].equalsIgnoreCase("top")) {
            y = margin_top;
            //height = 0;
        } else if (arr[0].equalsIgnoreCase("bottom") || arr[1].equalsIgnoreCase("bottom")) {
            if (height > 0)
                y = screenHeight - margin_top - height;
            else
                y = screenHeight - margin_top - 100;
        } else {
            y = (int) StringUtil.convertStringToDecimalNumber(arr[0]);
            //height = 0;
        }


        if (arr[1].equalsIgnoreCase("left") || arr[0].equalsIgnoreCase("left")) {
            x = margin_left;
            gravity = Gravity.LEFT;
            //width = 0;
        } else if (arr[1].equalsIgnoreCase("right") || arr[1].equalsIgnoreCase("right")) {
            x = margin_left; //screenWidth - margin - width;
            gravity = Gravity.RIGHT;
        } else {
            x = (int) StringUtil.convertStringToDecimalNumber(arr[1]);
            //width = 0;
        }

        return getLayoutParams(width, height, x, y, gravity);
    }

    public static FrameLayout.LayoutParams getLayoutParams(Activity context, String description, int w, int h) {
        return getLayoutParams(context, description, w, h, AppConfigs.SCREEN_PADDING_TOP_BOTTOM, AppConfigs.SCREEN_PADDING_LEFT_END);
    }

    public static FrameLayout.LayoutParams getLayoutParams(Activity context, String description) {
        return getLayoutParams(context, description, 0, 0, AppConfigs.SCREEN_PADDING_TOP_BOTTOM, AppConfigs.SCREEN_PADDING_LEFT_END);
    }

    /**
     * @param act
     * @return Width of screen by inch
     */
    public static double getScreenWidthAsInch(Activity act) {
        DisplayMetrics dm = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels;
        int height = dm.heightPixels;
        int dens = dm.densityDpi;
        double wi = (double) width / (double) dens;
        double hi = (double) height / (double) dens;
        double x = Math.pow(wi, 2);
        double y = Math.pow(hi, 2);
        return Math.sqrt(x + y);
    }

    public static int convertDpToPixel(Context context, int... dimensionId) {
        int px = 0;
        for (int i = 0; i < dimensionId.length; i++) {
            px += (int) context.getResources().getDimension(dimensionId[i]);
        }

        return px;
    }

    public static int getDpFromDimens(Context context, int... dimensionId) {
        int result = 0;

        for (int i = 0; i < dimensionId.length; i++) {
            result = result + (int) (context.getResources().getDimension(dimensionId[i]) / context.getResources().getDisplayMetrics().density);
        }

        return result;
    }

    /**
     * Send an email to the (one)given email, you can customize to send to multiple
     *
     * @param ctx
     * @param email
     */
    public static void sendEmail(Context ctx, String email) {
        if (!email.isEmpty() && !email.equals("null")) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
            i.putExtra(Intent.EXTRA_SUBJECT, "");
            i.putExtra(Intent.EXTRA_TEXT, "");
            try {
                ctx.startActivity(Intent.createChooser(i, ctx.getString(R.string.send_email)));
            } catch (android.content.ActivityNotFoundException ex) {
                CommonUtil.message(ctx, ctx.getString(R.string.no_email_client));
            }
        } else {
            CommonUtil.message(ctx, ctx.getString(R.string.no_email));
        }
    }


    /**
     * Generating random string
     *
     * @param length Length of code
     * @return Random string
     */
    public static String generateCode(int length) {
        String str = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0123456789";
        String code = "";
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * str.length());
            code += str.charAt(index);
        }

        return code;
    }

    /**
     * Generating random string
     *
     * @param context Context
     * @return Random string
     */
    public static String generateHashKey(Context context) {
        return "";
//        String key = AppConfigs.APP_NAME + PacketUtility.getDeviceUniqueID(context);
//        String encodeString = "";
//        try {
//            MessageDigest md = MessageDigest.getInstance("MD5");
//            encodeString = String.format("%040x", new BigInteger(1, md.digest(key.getBytes())));
//        } catch (Exception ex){
//
//        }
//
//        return encodeString;
    }

    /**
     * @param act
     * @param view     View which need to be calculated ratio
     * @param x        Horizontal ratio
     * @param y        Vertical ratio
     * @param subtract
     */
    public static void calViewRatio(Activity act, View view, float x, float y, int subtract) {
        int w = getScreenWidthAsPixel(act) - subtract;
        view.getLayoutParams().width = w;
        view.getLayoutParams().height = (int) Math.floor(w * y / x);
    }

    public static void openBrowser(Context context, String url) {
        if (url != null && !url.equals("")) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } else {
            CommonUtil.message(context, "Url is invalid: " + url);
        }
    }

    public static void rotateView(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "rotation", 0, 360);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.setDuration(1000);
        animator.start();
    }

    // This method allow closing keyboard when users click out-side
    public static void setupUI(final AppCompatActivity context, View view) {
        //Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {

            view.setOnTouchListener(new View.OnTouchListener() {

                public boolean onTouch(View v, MotionEvent event) {
                    closeKeyboard(context);
                    return false;
                }

            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(context, innerView);
            }
        }
    }

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 return ipv4 if true, else ipv6
     * @return address or empty string
     */
    public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress();
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        boolean isIPv4 = sAddr.indexOf(':') < 0;

                        if (useIPv4) {
                            if (isIPv4)
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 zone suffix
                                return delim < 0 ? sAddr.toUpperCase() : sAddr.substring(0, delim).toUpperCase();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

//    public static String getApkName(Context activity) {
//        return activity.getString(R.string.app_name).replace(" ","_")+ ".apk";
//    }
//
//    public static String getApkName(Activity activity) {
//        return activity.getString(R.string.app_name).replace(" ","_")+ ".apk";
////        String packageName = activity.getPackageName();
////        PackageManager pm = activity.getPackageManager();
////        try {
////            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
////            String apk = ai.publicSourceDir;
////            return apk;
////        } catch (Throwable x) {
////        }
////        return null;
//    }
//
//    public static boolean updateApk(final Activity activity, final AppVersionObj versionObj) {
//        String apkurl = versionObj.getFileUrl();
//
//        final boolean override = AppConfigs.DELETE_APP_BEFORE_INSTALL; // delete before or after download successfully !!
//        String folder = CacheManager.getCacheFolder() + "/";
//        String fileName = getApkName(activity); // activity.getString(R.string.app_name).replace(" ","_")+ ".apk";
//        //CommonUtil.message(activity, "app name: " + fileName);
//
//        String fileNameNew = override ? fileName : activity.getString(R.string.app_name).replace(" ","_") + DateTimeUtil.getCurrentTime(DateTimeUtil.SECOND) + ".apk";
//
//        final String finalDestination = folder + fileName;
//        final String finalDestinationNew = folder + fileNameNew;
//
//        //Delete update file if exists
//        final File file = new File(finalDestination);
//        final File fileNew = new File(finalDestinationNew);
//        boolean result = true;
//        //CommonUtil.message(activity,  finalDestination + (file.exists() ? " EXISTED " : "NOT EXISTED"));
//
//        if (override) {
//            if (file.exists() && file.delete()) {
//                CommonUtil.message(activity,  Constants.TEXT_DELETE_APP + " " + Constants.TEXT_SUCCESSFUL);
//            }
//        } else {
//
//        }
//
//        CommonUtil.message(activity,  Constants.TEXT_DOWNLOAD_AND_OVERRIDE + folder + fileName);
//
//        final Uri uri = Uri.fromFile(new File( folder + fileNameNew));
//        //set download manager
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkurl));
//        request.setDescription("Update new APK");
//        request.setTitle(activity.getString(R.string.app_name));
//
//        //set destination
//        request.setDestinationUri(uri);
//
//        // get download service and enqueue file
//        final DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
//        try {
//            final long downloadId = manager.enqueue(request);
//
//            //set BroadcastReceiver to install app when .apk is downloaded
//            BroadcastReceiver onComplete = new BroadcastReceiver() {
//                public void onReceive(Context ctxt, Intent intent) {
//
//                    CommonUtil.message(activity, Constants.TEXT_DOWNLOAD + " " + Constants.TEXT_SUCCESSFUL);
//
//                    if (!override && file.exists() && file.delete()) {
//                        CommonUtil.message(activity, Constants.TEXT_DELETE_APP + " " + Constants.TEXT_SUCCESSFUL);
//                    }
//
//                    boolean ok = !override ? fileNew.renameTo(file) : true;
//                    if (!ok)  {
//                        CommonUtil.message(activity, Constants.TEXT_INSTALL_APP + " " + Constants.TEXT_ERROR);
//                    } else {
//                        CommonUtil.message(activity, Constants.TEXT_INSTALL_APP + " " + Constants.TEXT_SUCCESSFUL);
//
//                        Intent install;
//
//                        CommonUtil.message(activity, activity.getPackageName());
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            Uri contentUri = FileProvider.getUriForFile(
//                                    activity,
//                                    activity.getPackageName() + ".provider",
//                                    new File(finalDestination)
//                            );
//
//                            install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
//                            install.setDataAndType(contentUri, manager.getMimeTypeForDownloadedFile(downloadId));
//                            install.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                            install.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true);
//                        } else {
//                            Uri contentUri = Uri.fromFile(new File(finalDestination));
//
//                            install = new Intent(Intent.ACTION_VIEW);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//
//                            install.setDataAndType(contentUri, manager.getMimeTypeForDownloadedFile(downloadId));
//                        }
//
//                        activity.startActivity(install);
//                        activity.unregisterReceiver(this);
//                        activity.finish();
//
//                        CacheManager.saveLastUpdateApp(activity, versionObj);
//                    }
//                }
//            };
//            activity.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
//            //register receiver for when .apk download is compete
//
//        } catch (SecurityException ex) {
//            CommonUtil.error(ex);
//        }
//
//        return result;
//    }



    public boolean isApplicationSentToBackground(final Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if(am != null) {
            List<ActivityManager.AppTask> tasks = am.getAppTasks();
            if (tasks != null && tasks.size() > 0) {
                tasks.get(0).setExcludeFromRecents(true);
                return true;
            }
        }

        return false;
    }

    public static void reloadApp(Activity activity) {
        try {
            CommonUtil.message(activity, Constants.TEXT_RESTART_APP + "...");

            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            activity.startActivity(startMain);
            activity.finish();

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);

        } catch (Exception ex) {
            CommonUtil.error(activity, ex);
        }
    }

    public static boolean isContextValid(Context context, Fragment fragment) {
        if (context == null)
            return false;
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing() || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
                return false;
            }
            return true;
        }

        return context != null && (fragment == null || (fragment.isAdded() && !fragment.isRemoving()));
    }

    public static int getRandom(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    public static String getUrlType(String url) {
        if (url.toLowerCase().endsWith(".jpg") || url.toLowerCase().endsWith(".png") || url.toLowerCase().endsWith(".gif") || url.toLowerCase().endsWith(".jpeg"))
            return Constants.TYPE_IMAGE;
        else if (url.toLowerCase().endsWith(".mp4") || url.toLowerCase().endsWith(".avi") || url.toLowerCase().endsWith(".vid"))
            return  Constants.TYPE_VIDEO;
        else
            return  Constants.TYPE_URL;
    }
}
