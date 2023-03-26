package com.stech.smartads.core;

import android.app.Activity;

import com.stech.smartads.utils.CommonUtil;


public class ExceptionHandler implements
		Thread.UncaughtExceptionHandler {
	private final Activity myContext;
	private final String LINE_SEPARATOR = "\n";
	private final String TAG = "ExceptionHandler";

	public ExceptionHandler(Activity context) {
		myContext = context;
	}

	public void uncaughtException(Thread thread, Throwable exception) {
		try {
			CommonUtil.error(myContext, exception);
			//Hung: restart app
			if (AppData.getInstance().getServerSetting().getAutoRestartServerWithUnCaughtError()) {
				CommonUtil.reloadApp(myContext);
			}

//			StringWriter stackTrace = new StringWriter();
//			exception.printStackTrace(new PrintWriter(stackTrace));
//			StringBuilder errorReport = new StringBuilder();
//			errorReport.append("************ CAUSE OF ERROR ************\n\n");
//			errorReport.append(stackTrace.toString());
//
//			errorReport.append("\n************ DEVICE INFORMATION ***********\n");
//			errorReport.append("Brand: ");
//			errorReport.append(Build.BRAND);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("Device: ");
//			errorReport.append(Build.DEVICE);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("Model: ");
//			errorReport.append(Build.MODEL);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("Id: ");
//			errorReport.append(Build.ID);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("Product: ");
//			errorReport.append(Build.PRODUCT);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("\n************ FIRMWARE ************\n");
//			errorReport.append("SDK: ");
//			errorReport.append(Build.VERSION.SDK);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("Release: ");
//			errorReport.append(Build.VERSION.RELEASE);
//			errorReport.append(LINE_SEPARATOR);
//			errorReport.append("Incremental: ");
//			errorReport.append(Build.VERSION.INCREMENTAL);
//			errorReport.append(LINE_SEPARATOR);

			//Log.d(TAG, errorReport.toString());

			//send error log to server
//			if (NetworkUtility.getInstance(myContext).checkNetwork()) {
//				AppData.sendLogToServer(myContext, new IModelListener() {
//					@Override
//					public void onSuccess(Object obj) {
//
//					}
//
//					@Override
//					public void onError() {
//
//					}
//				}, stackTrace.toString());
//			}


		} catch (Exception ex) {
			CommonUtil.error(myContext, ex);
			if (AppData.getInstance().getServerSetting().getAutoRestartServerWithUnCaughtError()) {
				CommonUtil.reloadApp(myContext);
			}
		}
	}
}