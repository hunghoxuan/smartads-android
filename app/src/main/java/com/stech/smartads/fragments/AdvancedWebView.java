package com.stech.smartads.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.BaseInputConnection;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import com.stech.smartads.R;
import com.stech.smartads.config.AppConfigs;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class AdvancedWebView extends im.delight.android.webview.AdvancedWebView {
    public AdvancedWebView(Context context) {
        super(context);

        init();
    }

    public AdvancedWebView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public AdvancedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    protected void init() {
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

//    @Override
//    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
//        BaseInputConnection baseInputConnection = new BaseInputConnection(this, false);
//        outAttrs.imeOptions = View.IME_ACTION_DONE;
//        outAttrs.inputType = TYPE_CLASS_TEXT;
//        return baseInputConnection;
//    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

}