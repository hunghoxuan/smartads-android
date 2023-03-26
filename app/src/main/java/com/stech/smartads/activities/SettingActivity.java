package com.stech.smartads.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.core.AppData;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.interfaces.IModelListener;
import com.stech.smartads.utils.PacketUtility;
import com.stech.smartads.R;
import com.stech.smartads.config.APIConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.interfaces.IConfirmation;
import com.stech.smartads.utils.ParseUtility;
import com.stech.smartads.components.network.NetworkUtility;
import com.stech.smartads.models.AppVersionObj;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;
import com.stech.smartads.utils.StringUtil;

public class SettingActivity extends BaseActivity {
    private EditText txtServerIP,txtDeviceName,txtDescription,txtHomepage;
    private TextView lblVersion;
    private Button btnLogin,btnUpdateVersion,btnExit;
    private RelativeLayout mainSettingLayout;

    protected void timeOutAction() {
        gotoMainActivity();
    }

    BroadcastReceiver broadcastOpenMainScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(self!= null){
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //hide NavigationBar
        getDecorView().setSystemUiVisibility(uiOptions);

        //register listener if app is opening main screen
        LocalBroadCastUtil.registerBroadCast(this,broadcastOpenMainScreenReceiver,LocalBroadCastUtil.ACTION_CONNECT_SERVER_SUCCESSFUL);
    }

    @Override
    protected void inflateLayout() {
        getLayoutInflater().inflate(R.layout.activity_setting, getFrameLayout());
    }

    @Override
    void initUI() {

        mainSettingLayout = (RelativeLayout) findViewById(R.id.main_setting_layout);
        CommonUtil.closeKeyboardWhenClickingOutOfKeyboard(mainSettingLayout);

        txtServerIP = (EditText) findViewById(R.id.txtAddressIp);
        txtDeviceName = (EditText) findViewById(R.id.txtDeviceName);
        txtDescription = (EditText) findViewById(R.id.txtDeviceDescription);
        txtHomepage = (EditText) findViewById(R.id.txtHomepage);

        lblVersion = (TextView) findViewById(R.id.lblVersion);
        lblVersion.setText(
                AppData().getDebugInfo());

        btnLogin = (Button) findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //currentCount = 0;
                resetHandler();
                onClickLogin();
            }
        });

        btnUpdateVersion = (Button) findViewById(R.id.btnUpdateVersion);
        btnExit = (Button) findViewById(R.id.btnExit);

        btnUpdateVersion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNewVersion();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CommonUtil.showConfirmationDialog(self, Constants.TEXT_DO_YOU_WANT_TO_EXIT + "?", Constants.TEXT_OK, Constants.TEXT_CANCEL, true, new IConfirmation() {
                    @Override
                    public void onPositive() {
                        CommonUtil.reloadApp(self);
                    }

                    @Override
                    public void onNegative() {
                        gotoMainActivity();
                    }
                });
            }
        });

        txtDeviceName.setText(CacheManager.getSettingDeviceName(self));
        txtDescription.setText(CacheManager.getSettingDeviceDescription(self));

        //check auto login when ip is configured
        if(!CacheManager.getFullAddressIP(self).isEmpty()) {
            txtServerIP.setText(CacheManager.getFullAddressIP(this));
            //registerDevice();
        } else {
            txtServerIP.setText(APIConfigs.SERVER_URL);
        }

        if(!CacheManager.getHomePage(self).isEmpty()) {
            txtHomepage.setText(CacheManager.getHomePage(self));
        } else {
            txtHomepage.setText(APIConfigs.HOMEPAGE_URL);
        }

        initLogo();

    }



    @Override
    protected void onResume() {
        resetHandler();
        super.onResume();
    }

    @Override
    protected void onPause() {
        resetHandler();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadCastUtil.unRegisterBroadCast(this,broadcastOpenMainScreenReceiver);
    }

    private void onClickLogin(){
        resetHandler();

        String serverAddress = txtServerIP.getText().toString();
        //check if ip is empty
        if(serverAddress.isEmpty()) {
            showMessage(Constants.TEXT_SERVER_REQUIRED);
            return;
        }

        //check valid url
        String url = StringUtil.getFullUrl(serverAddress);
        url = url.trim();

        if(!Patterns.WEB_URL.matcher(url).matches()) {
            showMessage(Constants.TEXT_SERVER + " [ " + url + " ] " + Constants.TEXT_INVALID);
            return;
        }

        //process to get cache Ip address & Port
        if (txtServerIP != null && !txtServerIP.getText().toString().isEmpty())
            cacheConfigureIP(txtServerIP.getText().toString());

        if (txtHomepage != null && !txtHomepage.getText().toString().isEmpty())
            cacheHomePage(txtHomepage.getText().toString());

        CommonUtil.showConfirmationDialog(self, Constants.TEXT_SELECT_APP_MODE, Constants.TEXT_SERVER, Constants.TEXT_HOMEPAGE, true, new IConfirmation() {
            @Override
            public void onPositive() {
                //finish setting
                AppData.isUserSetting = false;
                CacheManager.storeAppMode(self, Constants.APP_MODE_API);
                if (NetworkUtility.getInstance(self).isNetworkAvailable())
                    showMessage( Constants.TEXT_START_CONNECTING + " " + Constants.TEXT_SERVER + ": " + txtServerIP.getText().toString());

                registerDevice();
            }

            @Override
            public void onNegative() {
                //finish setting
                AppData.isUserSetting = false;
                CacheManager.storeAppMode(self, Constants.APP_MODE_HOMEPAGE);
                if (NetworkUtility.getInstance(self).isNetworkAvailable())
                    showMessage( Constants.TEXT_START_DISPLAY + " " + Constants.TEXT_WEBSITE + ": " + txtHomepage.getText().toString());
                getSchedulesAndGotoMain();
            }
        });
    }


    @Override
    void initControl() {
        txtServerIP.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                resetHandler();
                return false;
            }
        });

        txtHomepage.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                resetHandler();
                return false;
            }
        });
        txtDeviceName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                resetHandler();
                return false;
            }
        });

        txtDescription.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                resetHandler();
                return false;
            }
        });
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
                    registerDevice();
                } else {
                    finish();
                }
                break;

        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void registerDevice() {
        if (AppData.isUserSetting) return;
        CommonUtil.error(TAG, "Register Device ID: " + PacketUtility.getDeviceUniqueID(self));

        //save name & description
        CacheManager.storeSettingDeviceName(self, txtDeviceName.getText().toString());
        CacheManager.storeSettingDeviceDescription(self, txtDescription.getText().toString());


        AppData().registerDevice(new IModelListener() {
            @Override
            public void onSuccess(Object obj) {
                //HUNG:OFFLINE:
                String json = ParseUtility.parseJsonFromObject(obj);

                if (json.isEmpty()) {
                    NetworkUtility.getInstance(self).setOffline(true, Constants.TEXT_REGISTER_DEVICE + " " + Constants.TEXT_FAIL);
                    getSchedulesAndGotoMain();
                    return;
                } else {
                    //process to get cache Ip address & Port
                    cacheConfigureIP(txtServerIP.getText().toString());

                    String license = ParseUtility.parseLicense(json);
                    CacheManager.storeLicenseKey(self, license);

                    //process to setup server time to device time
                    long currentTimestamp = ParseUtility.parseServerTimestamp(obj.toString());
                    ((MainApplication) self.getApplication()).setCurrentTime(currentTimestamp);

                    NetworkUtility.getInstance(self).setOffline(false, Constants.TEXT_REGISTER_DEVICE + " " + Constants.TEXT_SUCCESSFUL);
                    //CacheManager.clearSchedulesJson(self);

                    getSchedulesAndGotoMain();
                }
            }

            @Override
            public void onError(Throwable ex) {
                CommonUtil.error(self, ex);
                NetworkUtility.getInstance(self).setOffline(true, Constants.TEXT_REGISTER_DEVICE + " " + Constants.TEXT_ERROR + ": " + ex.getMessage() + ". ");
                getSchedulesAndGotoMain();
            }
        });
    }

    private void cacheHomePage(String homepage) {
        CacheManager.storeHomePage(this, homepage);
    }

    private void cacheConfigureIP(String serverAddress){
        CacheManager.storeAddressServerIP(this, serverAddress);
        CacheManager.storeServerPort(this, "");
    }


    private void getSchedulesAndGotoMain()
    {
        AppData.getInstance(self).getSchedules(new IModelListener() {
            @Override
            public void onSuccess(Object obj) {
                gotoMainActivity();
            }

            @Override
            public void onError(Throwable error) {
                NetworkUtility.getInstance(self).setOffline(true, Constants.TEXT_DOWNLOAD_DATA + " " + Constants.TEXT_ERROR + ": " + StringUtil.getErrorMessage(error) + ". ");
                gotoMainActivity();
            }
        });
    }

    private void gotoMainActivity() {
        CommonUtil.startActivity(self, MainActivity.class);
    }

    // call api to get new app version
    private void requestNewVersion() {
        CommonUtil.log(TAG, "Request New Version ! ");
        checkStoragePermission();
        AppData().requestNewAppVersion(new IModelListener() {
            @Override
            public void onSuccess(Object obj) {
                final AppVersionObj versionObj = ParseUtility.parseAppVersion(obj.toString());
                String info = CacheManager.getAppVersionInfo(versionObj);
                String lastUpdate = CacheManager.getLastUpdateApp(self);
                String compared = (!info.isEmpty() && lastUpdate.startsWith(info)) ? "Device and server has same version." : "Server has different version.";

                CommonUtil.showConfirmationDialog(self, "[App installed on device]: \n" + lastUpdate + ".\n\n [App found on server]:\n" + info + "\n\n" + compared + " " + getString(R.string.message_update_new_version), getString(R.string.yes), getString(R.string.no), true, new IConfirmation() {
                    @Override
                    public void onPositive() {
                        AppData().updateApk(self, versionObj);
                    }

                    @Override
                    public void onNegative() {
                    }
                });
            }

            @Override
            public void onError(Throwable error) {
                showMessage(Constants.TEXT_UPDATE_DEVICE + ": " + StringUtil.getErrorMessage(error));
            }
        });
    }
}
