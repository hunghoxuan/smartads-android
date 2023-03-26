package com.stech.smartads.interfaces;

import com.android.volley.VolleyError;

/**
 * Created by Na Pro on 09/04/2015.
 */
public interface IResponse {
    void onResponse(Object response);
    void onError(VolleyError error);
}
