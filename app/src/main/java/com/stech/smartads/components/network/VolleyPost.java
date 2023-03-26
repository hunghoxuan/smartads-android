package com.stech.smartads.components.network;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.stech.smartads.R;
import com.stech.smartads.config.APIConfigs;
import com.stech.smartads.interfaces.IResponse;
import com.stech.smartads.models.DataPart;
import com.stech.smartads.utils.CommonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Na Pro on 09/04/2015.
 */
public class VolleyPost {

    private static final String TAG = VolleyPost.class.getSimpleName();

    private Context context;
    private boolean isShowWaitingDialog;
    private MyProgressDialog dialog;

    public VolleyPost(Context context, boolean showProgress, boolean cancelable) {
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
                CommonUtil.error(ex);
            }
        }
    }

    public void request(String url, final Map<String, String> params, final IResponse listener) {
        CommonUtil.log(TAG, url);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            CommonUtil.log(TAG, entry.getKey() + "=" + entry.getValue());
        }
        final RequestQueue volleyQueue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    listener.onResponse(new JSONObject(response));
                } catch (JSONException e) {
                    CommonUtil.error(e);
                }
                // Dismiss the progress bar.
                if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
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
                if (error instanceof NetworkError) {
                } else if (error instanceof ServerError) {
                } else if (error instanceof AuthFailureError) {
                } else if (error instanceof ParseError) {
                } else if (error instanceof NoConnectionError) {
                } else if (error instanceof TimeoutError) {
                    CommonUtil.message(context, context.getString(R.string.msg_request_time_out));
                }

                // Dismiss the progress bar.
                if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };

        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions. Volley does retry for you if you have specified the policy.
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(APIConfigs.REQUEST_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        stringRequest.setTag(url);
        volleyQueue.add(stringRequest);
    }

    /***************
     * post file to server.
     *
     * @param url
     * @param params
     * @param files
     * @param listener
     */
    public void postFiles(String url, final Map<String, String> params,
                          final Map<String, DataPart> files, final IResponse listener) {
        CommonUtil.log(TAG, url);
        final RequestQueue volleyQueue = Volley.newRequestQueue(context);

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String data = new String(response.data);
                        listener.onResponse(data);
                        // Dismiss the progress bar.
                        if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof NetworkError) {
                } else if (error instanceof ServerError) {
                } else if (error instanceof AuthFailureError) {
                } else if (error instanceof ParseError) {
                } else if (error instanceof NoConnectionError) {
                } else if (error instanceof TimeoutError) {
                    CommonUtil.message(context, context.getString(R.string.msg_request_time_out));
                }

                // Dismiss the progress bar.
                if (isShowWaitingDialog && dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                return files;
            }
        };
        //Set a retry policy in case of SocketTimeout & ConnectionTimeout Exceptions. Volley does retry for you if you have specified the policy.
        multipartRequest.setRetryPolicy(new DefaultRetryPolicy(APIConfigs.REQUEST_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        multipartRequest.setTag(url);
        volleyQueue.add(multipartRequest);
    }
}
