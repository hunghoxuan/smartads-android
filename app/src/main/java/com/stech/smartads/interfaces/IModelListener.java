package com.stech.smartads.interfaces;

/**
 * Created by NaPro on 24/02/2017.
 */

public interface IModelListener {

    void onSuccess(Object obj);

    //void onError();

    void onError(Throwable error);
}
