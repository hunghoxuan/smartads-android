package com.stech.smartads.components;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

public class SoftInputAssist {
    private View rootView;
    private ViewGroup contentContainer;
    private ViewTreeObserver viewTreeObserver;
    private Activity activity;

    private ViewTreeObserver.OnGlobalLayoutListener listener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            possiblyResizeChildOfContent();
        }
    };
    private Rect contentAreaOfWindowBounds = new Rect();
    private FrameLayout.LayoutParams rootViewLayout;
    private int usableHeightPrevious = 0;

    public SoftInputAssist(Activity activity) {
        this.activity = activity;

        try {
            contentContainer = (ViewGroup) this.activity.findViewById(android.R.id.content);
            if (contentContainer != null) {
                rootView = contentContainer.getChildAt(0);
                rootViewLayout = (FrameLayout.LayoutParams) rootView.getLayoutParams();
            }
        } catch (NullPointerException ex) {
            //CommonUtil.message(activity, "Null SoftInputAssist");
        }
    }

    public void onPause() {
        //CommonUtil.message(activity, "onPause");
        if (rootView == null)
            return;
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.removeOnGlobalLayoutListener(listener);
        }
    }

    public void onResume() {
        //CommonUtil.message(activity, "onResume");
        if (rootView == null)
            return;

        if (viewTreeObserver == null || !viewTreeObserver.isAlive()) {
            viewTreeObserver = rootView.getViewTreeObserver();
        }

        viewTreeObserver.addOnGlobalLayoutListener(listener);
    }

    public void onDestroy() {
        rootView = null;
        contentContainer = null;
        viewTreeObserver = null;
    }

    private void possiblyResizeChildOfContent() {
        contentContainer.getWindowVisibleDisplayFrame(contentAreaOfWindowBounds);
        int usableHeightNow = contentAreaOfWindowBounds.height();

        if (usableHeightNow != usableHeightPrevious) {
            rootViewLayout.height = usableHeightNow;
            rootView.layout(contentAreaOfWindowBounds.left, contentAreaOfWindowBounds.top, contentAreaOfWindowBounds.right, contentAreaOfWindowBounds.bottom);
            rootView.requestLayout();

            usableHeightPrevious = usableHeightNow;
        }
    }
}
