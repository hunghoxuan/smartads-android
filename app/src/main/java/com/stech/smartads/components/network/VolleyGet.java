package com.stech.smartads.components.network;

import android.content.Context;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stech.smartads.config.APIConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.interfaces.IResponse;
import com.stech.smartads.utils.CommonUtil;


public class VolleyGet {

    private static final String TAG = VolleyGet.class.getSimpleName();

    private Context context;
    private boolean isShowWaitingDialog = false;
    private MyProgressDialog dialog;

    public VolleyGet(Context context, boolean showProgress, boolean cancelable) {
        this.context = context;
        this.isShowWaitingDialog = showProgress;

        if (isShowWaitingDialog) {
            try {
                // Show progress bar
                dialog = new MyProgressDialog(context);
                if (!dialog.isShowing()) {
                    dialog.show();
                    dialog.setCancelable(cancelable);
                }
            } catch (Exception ex) {
                // Dismiss the progress bar.
                if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                CommonUtil.error(context, ex);
            }
        }
    }


    public void getStringRequest(Uri.Builder builder, final IResponse listener) {
        final RequestQueue volleyQueue = Volley.newRequestQueue(context);
        final String url = builder.toString();
        //CommonUtil.message(context, url);
        try {
            StringRequest stringRequest = new StringRequest(Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String result) {
                    try {
                        if (result != null) {
                            listener.onResponse(result);

                            // Dismiss the progress bar.
                            if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }

                            // Cancel the request.
                            volleyQueue.cancelAll(url);
                        }
                    } catch (Exception e) {
                        // Dismiss the progress bar.
                        if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        CommonUtil.error(context, e);
                        listener.onError(null); //OLD
                        //listener.onError(new VolleyError(StringUtil.getErrorMessage(e)));
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    // Handle your error types accordingly.For Timeout & No connection error, you can show 'retry' button.
                    // For AuthFailure, you can re login with user credentials.
                    // For ClientError, 400 & 401, Errors happening on client side when sending api request.
                    // In this case you can check how client is forming the api and debug accordingly.
                    // For ServerError 5xx, you can do retry or handle accordingly.
                    if (error instanceof NetworkError ) {
                    } else if (error instanceof ServerError) {
                    } else if (error instanceof AuthFailureError) {
                    } else if (error instanceof ParseError) {
                    }  else if (error instanceof TimeoutError) {
                        CommonUtil.message(context,  "API [" + url + "] "+ Constants.TEXT_ERROR + ": " + CommonUtil.getErrorMessage(error));
                    }


                    // Dismiss the progress bar.
                    if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }

                    listener.onError(error);

                    // Cancel the request.
                    volleyQueue.cancelAll(url);
                }
            });

            //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions. Volley does retry for you if you have specified the policy.
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(APIConfigs.REQUEST_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            stringRequest.setTag(url);
            volleyQueue.add(stringRequest);

        } catch (Exception ex) {
            CommonUtil.error(context, ex);
            // Dismiss the progress bar.
            if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            listener.onResponse(null); //OLD
            //listener.onError(new VolleyError(StringUtil.getErrorMessage(ex)));

            // Cancel the request.
            volleyQueue.cancelAll(url);
        }
    }

}
