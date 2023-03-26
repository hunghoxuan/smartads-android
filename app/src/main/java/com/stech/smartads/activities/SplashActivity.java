package com.stech.smartads.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.stech.smartads.R;
import com.stech.smartads.core.AppData;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.interfaces.IModelListener;
import com.stech.smartads.utils.ParseUtility;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.utils.StringUtil;

public class SplashActivity extends BaseActivity {

    public static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 100;

    String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            //gotoHomeActivity();
            if (AppData.getInstance(self).isHomePageMode()) {
                getSchedulesAndOpenMainActivity();
            } else {
                registerDeviceAndGotoMainActivity();
            }

        } catch (Exception ex) {
            CommonUtil.error(self, ex);
            gotoSettingPage();
        }
    }

    protected boolean isDebugEnabled() {
        return false;
    }

    @Override
    protected void inflateLayout() {
        getLayoutInflater().inflate(R.layout.activity_splash, getFrameLayout());
    }

    @Override
    void initUI() {

    }



    @Override
    void initControl() {
    }

    @Override
    void getExtraValues() {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 100:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    registerDeviceAndGotoMainActivity();

                } else {

                    finish();
                }
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void registerDeviceAndGotoMainActivity() {

        if (CacheManager.getAddressServerIP(self).isEmpty()) {
            gotoSettingPage();
            return;
        }

        //CommonUtil.log(TAG, "Register Device ID: " +PacketUtility.getDeviceUniqueID(self));

        AppData().registerDevice(new IModelListener() {
            @Override
            public void onSuccess(Object obj) {
                String json = ParseUtility.parseJsonFromObject(obj);
                boolean isSuccess = ParseUtility.isSuccess(json);
                String errorMessage = ParseUtility.getErrorMessage(json);

                //HUNG:OFFLINE MODE
                if (isSuccess) {
                    NetworkUtility.getInstance(self).setOffline(false);

                    //process to setup server time to device time
                   long currentTimestamp = ParseUtility.parseServerTimestamp(json);
                   CommonUtil.log(TAG, "RegisterDevice.  Timestamp: " + currentTimestamp + DateTimeUtil.convertTimeStampToDate(currentTimestamp, AppConfigs.FORMAT_SCHEDULE_DATE_TIME));

                    ((MainApplication) self.getApplication()).setCurrentTime(currentTimestamp);

                    AppData.getInstance(self).getSchedules(new IModelListener() {
                        @Override
                        public void onSuccess(Object obj) {
                            gotoMainActivity();
                        }

                        @Override
                        public void onError(Throwable x) {
                            showMessage(Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_ERROR + ": " + StringUtil.getErrorMessage(x) + ". " + Constants.TEXT_CHECK_NETWORK);
                            gotoSettingPage();
                        }
                    });

                } else {
                    //NetworkUtility.getInstance(self).setOffline(true, "Đăng ký thiết bị có lỗi " + errorMessage);
                    gotoSettingPage();

                }
            }

            @Override
            public void onError(Throwable error) {
                //NetworkUtility.getInstance(self).setOffline(true, "Đăng ký thiết bị có lỗi "  + StringUtil.getErrorMessage(error));
                gotoSettingPage();
            }
        });
    }

    private void getSchedulesAndOpenMainActivity()
    {
        AppData.getInstance(self).getSchedules( new IModelListener() {
            @Override
            public void onSuccess(Object obj) {
                gotoMainActivity();
            }

            @Override
            public void onError(Throwable x) {
                showMessage( Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_ERROR + ": " + StringUtil.getErrorMessage(x) + ". " + Constants.TEXT_CHECK_NETWORK);
                gotoSettingPage();
            }
        });
    }

    private void gotoMainActivity() {
        CommonUtil.startActivity(self, MainActivity.class);
    }

    private void gotoSettingPage() {
        CommonUtil.startActivity(self, SettingActivity.class);
    }

}
