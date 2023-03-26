package com.stech.smartads.interfaces;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.squareup.picasso.Picasso;

/**
 * Created by NaPro on 20/03/2017.
 */

public interface IImage {
    void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from);

    void onBitmapFailed(Drawable errorDrawable);

    void onPrepareLoad(Drawable placeHolderDrawable);
}
