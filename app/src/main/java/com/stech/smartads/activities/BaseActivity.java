package com.stech.smartads.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.bumptech.glide.Glide;
import com.splunk.mint.Mint;
import com.squareup.picasso.Picasso;
import com.stech.smartads.core.AppData;
import com.stech.smartads.core.ExceptionHandler;
import com.stech.smartads.R;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.fragments.BaseFragment;
import com.stech.smartads.fragments.ImageFragment;
import com.stech.smartads.fragments.WebViewFragment;
import com.stech.smartads.interfaces.IConfirmation;
import com.stech.smartads.models.LayoutFrameObj;
import com.stech.smartads.models.ServerSetting;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;
import com.stech.smartads.components.SoftInputAssist;
import com.stech.smartads.utils.StringUtil;

import java.util.ArrayDeque;
import java.util.Queue;
import android.os.Handler;
import android.view.MotionEvent;
import android.widget.ImageView;


public abstract class BaseActivity extends AppCompatActivity implements View.OnClickListener{

    protected int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |View.SYSTEM_UI_FLAG_IMMERSIVE
            | View.SYSTEM_UI_FLAG_FULLSCREEN;

    public String TAG;
    protected BaseActivity self;
    private FrameLayout mFrlMain;
    private View decorView;
    //In your Activity, Add a flag to know when an Activity has been paused or resumed in your Activity.
    private boolean isRunning;
    Queue<DeferredFragmentTransaction> deferredFragmentTransactions = new ArrayDeque<>();
    private Handler mHandler;

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            timeOutAction();
        }
    };

    protected int timeCounter = 0;
    protected SoftInputAssist softInputAssist = createSoftInputAssist();

    protected SoftInputAssist createSoftInputAssist() {
        return null;
    }
    protected ImageView imgTopLogo;

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.VIBRATE,
            Manifest.permission.RECORD_AUDIO,
    };

    protected void timeOutAction() {
    }

    public boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                CommonUtil.error("Permission", "Permission is granted");
                return true;
            } else {

                CommonUtil.error("Permission", "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            CommonUtil.error("Permission", "Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // do something
            }
            return;
        }
    }

    protected void initLogo() {
        final boolean isWebsiteMode = AppData().isWebsiteMode();
        final float opacity = isWebsiteMode ? 1 : ((float) Setting().getLogoOpacity() / 100); //always show back button if in website mode
        final String mode = AppData().getAppMode();
        String logoUrl = AppData().isWebsiteMode() ? AppConfigs.ASSET_ROOT_FOLDER + "/common/back.png" : Setting().getLogoUrl();
        int logoWidth = AppData().isWebsiteMode() ? AppConfigs.BACK_BUTTON_WIDTH : AppConfigs.SHOW_LOGO_WIDTH;
        int logoHeight = AppData().isWebsiteMode() ? AppConfigs.BACK_BUTTON_WIDTH : AppConfigs.SHOW_LOGO_HEIGHT;

        imgTopLogo = addImage(CommonUtil.getLayoutParams(self, Setting().getLogoPosition(), logoWidth, logoHeight), logoUrl, opacity, -1);
        imgTopLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isWebsiteMode) {
                    timeOutAction(); //goback
                } else {
                    CommonUtil.showConfirmationDialog(self, Constants.TEXT_SELECT_APP_MODE, mode.equalsIgnoreCase(Constants.APP_MODE_API) ? Constants.TEXT_MEDIA : Constants.TEXT_HOMEPAGE, Constants.TEXT_MENU, true, new IConfirmation() {
                        @Override
                        public void onPositive() {
                            switchAppMode(mode);
                        }

                        @Override
                        public void onNegative() {
                            switchAppMode(Constants.APP_MODE_DEFAULT);
                        }
                    });
                }
            }
        });
        imgTopLogo.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                gotoSettingScreen();
                return false;
            }
        });
    }

    protected void setImageUrl(ImageView img, String url, int resourceId) {
        if (url != null && url.length() > 0) {
            if (resourceId == 0)
                Glide.with(this)
                        .load(url) // or url
                        .into(img);
            else
                Picasso.with(this).load(url).into(img);
        } else if (resourceId > 0) {
//            Glide.with(this)
//                    .load(resourceId) // or url
//                    .override(500,400)
//                    .into(img);
            img.setImageResource(resourceId != 0 ? resourceId : R.drawable.ic_logo_108_tron);
        } else {
            img.setImageResource(R.drawable.ic_logo_108_tron);
        }
    }

    protected void setImageUrl(ImageView img, String url) {
        setImageUrl(img, url, 0);
    }

    protected int getTimeOut() {
        return AppData().getTimeOut();
    }

    protected void setTimeOut(int timeout) {
        AppData().setTimeOut(timeout);
        resetHandler();
    }

    protected View getDecorView() {
        if (decorView == null)
            decorView = getWindow().getDecorView();
        return decorView;
    }

    protected FrameLayout getFrameLayout() {
        if (mFrlMain == null)
            mFrlMain = (FrameLayout) findViewById(R.id.fr_main);

        return mFrlMain;
    }

    protected void resetHandler() {
        //timeCounter = 0;
        mHandler.removeCallbacks(mRunnable);
        int t = getTimeOut();
        if (t > 0)
            mHandler.postDelayed(mRunnable, t);
    }

    protected void prepareScreen() {
        AppData().setCurrentUrl("");
        AppData().cleanup();
        AppData().clearTimeOut();
    }

    public void gotoSettingScreen() {
        try {
            Bundle bundle = new Bundle();
            AppData.isUserSetting = true;
            CommonUtil.startActivityRTL(self, SettingActivity.class, bundle);

            removeAllViewsFromMainLayout();
            //finish();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    protected void removeAllViewsFromMainLayout() {
        try {
            //need to remove VideoFragment firstly
            LocalBroadCastUtil.sendBroadcastListener(this, LocalBroadCastUtil.ACTION_STOP_LOAD_HIS);
            LocalBroadCastUtil.sendBroadcastListener(this, LocalBroadCastUtil.ACTION_STOP_VIDEO_PLAYER);

            if (getFrameLayout() != null) {
                getFrameLayout().removeAllViewsInLayout(); //kiemdv
            }

            //remove all old fragment
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                //getSupportFragmentManager().beginTransaction().remove(fragment).commit(); // avoid bug: Can not perform this action after onSaveInstanceState with ViewPager
                getSupportFragmentManager().beginTransaction().remove(fragment).commitAllowingStateLoss();
                fragment.onDestroy();
            }

        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    public void switchAppMode(String mode) {
        prepareScreen();
        if (mode.equalsIgnoreCase(Constants.APP_MODE_HOMEPAGE) || mode.equalsIgnoreCase(Constants.APP_MODE_API)) {
            CacheManager.storeAppMode(self, mode);
            resetHandler();
        } else if (mode.equalsIgnoreCase(Constants.APP_MODE_SETTINGS)) {
            gotoSettingScreen();
        } else {
            gotoWebScreen(mode);
        }
    }

    public void gotoWebScreen(String url) {
//        if (true) {
//            int h = AppData().getScreenHeight();
//            addWebView(0, 0, 1, (int) 0.5 * h, url);
//            return;
//        }

        if (StringUtil.isUrlEndsWith(url, ".zip")) {
            AppData().downloadApp(self, AppConfigs.APPS_FOLDER, url);
            return;
        }

        if (url.equalsIgnoreCase(Constants.APP_MODE_API) || url.equalsIgnoreCase(Constants.APP_MODE_HOMEPAGE)) {
            switchAppMode(url);
            return;
        }

        prepareScreen();
        String tmp = url.toLowerCase();
        if (tmp.contains(Constants.PARAM_TIMEOUT)) {
            tmp = tmp.substring(tmp.indexOf(Constants.PARAM_TIMEOUT) + Constants.PARAM_TIMEOUT.length() + 1);
            int timeout = 0;
            if (tmp.contains("&"))
                tmp = tmp.substring(0, tmp.indexOf("&"));
            if (tmp.length() > 0) {
                timeout = (int) StringUtil.convertStringToDecimalNumber(tmp);
                if (timeout > Constants.TIMEOUT_INIT_VALUE)
                    AppData().setTimeOut(timeout);
            }
        }

        AppData.getInstance(self).setCurrentUrl(url);
        resetHandler();
    }

    public void gotoWebScreen(String url, int timeout) {
        setTimeOut(timeout);
        gotoWebScreen(url);
    }

    public void goBack() {
        String mode = CacheManager.getAppMode(self);
        showMessage( Constants.TEXT_BACK_TO_SCREEN + ": " + mode);
        switchAppMode(mode);
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            isRunning = true;
            if (softInputAssist != null)
                softInputAssist.onResume();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    protected void onPostResume() {
        try {
            super.onPostResume();

            while (deferredFragmentTransactions != null && !deferredFragmentTransactions.isEmpty()) {
                deferredFragmentTransactions.remove().commit();
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }


    @Override
    protected void onPause() {
        try {
            super.onPause();
            isRunning = false;
            if (softInputAssist != null)
                softInputAssist.onPause();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            isRunning = false;
            if (softInputAssist != null )
                softInputAssist.onDestroy();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    public void onUserInteraction() {
        resetHandler();
    }


    //In your Activity
    public void replaceFragment(int contentFrameId, android.support.v4.app.Fragment replacingFragment) {
        if (!isRunning) {
            DeferredFragmentTransaction deferredFragmentTransaction = new DeferredFragmentTransaction() {
                @Override
                public void commit() {
                    replaceFragmentInternal(getContentFrameId(), getReplacingFragment());
                }
            };

            deferredFragmentTransaction.setContentFrameId(contentFrameId);
            deferredFragmentTransaction.setReplacingFragment(replacingFragment);

            deferredFragmentTransactions.add(deferredFragmentTransaction);
        } else {
            replaceFragmentInternal(contentFrameId, replacingFragment);
        }
    }

    private void replaceFragmentInternal(int contentFrameId, Fragment replacingFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(contentFrameId, replacingFragment)
                .commit();
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);

            mHandler = new Handler();
            mHandler.postDelayed(mRunnable, getTimeOut());
            myCustomOnCreate(savedInstanceState);

            self = this;

            //init debug (MINT)
            if (isDebugEnabled()) {
                if (AppData.getInstance().getServerSetting().getAutoRestartServerWithUnCaughtError()) {
                    //error handler. override Mint
                    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
                }

                Mint.initAndStartSession(this.getApplication(), AppData.getInstance().getServerSetting().getMintAPIKey());
            }

            TAG = this.getClass().getSimpleName();

            setContentView(R.layout.activity_base);

            // Base views
            mFrlMain = (FrameLayout) findViewById(R.id.frl_main);

            // Base listeners

            // Base methods

            // Abstract methods for extended classes
            inflateLayout();
            getExtraValues();
            initUI();
            initControl();
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        resetHandler();
        return false;
    }

    @Override
    public void onClick(View view) {
        resetHandler();
        myCustomOnClick(view);
    }

    protected void myCustomOnCreate(Bundle savedInstanceState) {

    }

    protected void myCustomOnClick(View view) {

    }

    protected boolean isDebugEnabled() {
        return AppConfigs.isDebug;
    }

    protected abstract void inflateLayout();

    abstract void initUI();

    abstract void initControl();

    abstract void getExtraValues();

    protected ImageView addImage(ImageView image, int x, int y, int w, int h, float opacity ) {
        return addImage(image, CommonUtil.getLayoutParams(w,h,x,y), opacity);
    }

    protected ImageView addImage(ImageView image, FrameLayout.LayoutParams params) {
        return addImage(image, params, 1);
    }

    protected ImageView addImage(ImageView image, FrameLayout.LayoutParams params, float opacity ) {
        if (image == null) {
            image = new ImageView(self);
        }

        image.setId(CommonUtil.getRandom(100, 999));
        image.setAlpha(opacity);
        if (image.getParent() != null) {
            ((FrameLayout)image.getParent()).removeView(image); // <- fix
        }

//        FrameLayout frame = new FrameLayout(self);
//        frame.addView(image);
//        frame.setId(CommonUtil.getRandom(100, 999));
//        getFrameLayout().addView(frame, params);
        //addFragment(params, image);

        getFrameLayout().addView(image, params);
        return image;
    }

    protected ImageView addImage(FrameLayout.LayoutParams params, String url, float opacity, int resourceId) {
        ImageView image = null;
        if (image == null) {
            image = new ImageView(self);
        }

        image.setAlpha(opacity);
        //addFragment(params, image);
        setImageUrl(image, url, resourceId);
        getFrameLayout().addView(image, params);

        return image;
    }

    protected void addWebView(int x, int y, int w, int h, String url) {
        Fragment fragment = WebViewFragment.getInstance(new LayoutFrameObj(url));
        getFrameLayout().addView(fragment.getView(), CommonUtil.getLayoutParams(w, h, x, y));
    }

    protected void addWebView(FrameLayout.LayoutParams params, String url) {
        Fragment fragment = WebViewFragment.getInstance(new LayoutFrameObj(url));

        getFrameLayout().addView(fragment.getView(), params);
    }

    protected ImageView addImage(FrameLayout.LayoutParams params, String url, float opacity) {
        return addImage(params, url, opacity, 0);
    }

    protected ImageView addImage(FrameLayout.LayoutParams params, float opacity) {
        ImageFragment fragment = new ImageFragment();
        ImageView image = fragment.getImageView();
        if (image != null) {
            image.setAlpha(opacity);
        }

        //fragment.setImageView(imageView);
        FrameLayout frame = new FrameLayout(self);
        //frame.setTag(image.toString());
        frame.setId(CommonUtil.getRandom(100, 999));
        addFragment(frame, fragment);
        getFrameLayout().addView(frame, params);

        return image;
    }


    protected ServerSetting Setting() {
        return AppData.getInstance(self).getServerSetting();
    }
    protected AppData AppData() { return AppData.getInstance(self); }

    protected boolean isLicensed() {
        return AppData.getInstance().getIsLicense();
    }

    protected void showMessage(String msg) {
        CommonUtil.message(self, msg);
    }

    // add fragment to a frame layout
    protected void addFragment(FrameLayout frame, BaseFragment fragment) {
        try {
            if (fragment != null) {
                fragment.setBaseActivity(this);
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.add(frame.getId(), fragment);
                //transaction.commit(); // avoid bug: Can not perform this action after onSaveInstanceState with ViewPager
                CommonUtil.commitTransaction(transaction);
            }
        } catch (Exception ex) {
            CommonUtil.error(self, ex);
        }
    }

    protected void addFragment(FrameLayout.LayoutParams params, ImageView imageView) {
        ImageFragment fragment = new ImageFragment();
        fragment.setImageView(imageView);
        FrameLayout frame = new FrameLayout(self);
        frame.setTag(imageView.toString());
        frame.setId(CommonUtil.getRandom(100, 999));
        addFragment(frame, fragment);
        getFrameLayout().addView(frame, params);
    }

    protected void addFragment(BaseFragment fragment) {
        addFragment(getFrameLayout(), fragment);
    }
}
