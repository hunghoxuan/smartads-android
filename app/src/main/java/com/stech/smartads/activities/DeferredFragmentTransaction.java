package com.stech.smartads.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.stech.smartads.R;

public abstract class DeferredFragmentTransaction {

    private int contentFrameId;
    private Fragment replacingFragment;

    public abstract void commit();

    public int getContentFrameId() {
        return contentFrameId;
    }

    public void setContentFrameId(int contentFrameId) {
        this.contentFrameId = contentFrameId;
    }

    public Fragment getReplacingFragment() {
        return replacingFragment;
    }

    public void setReplacingFragment(Fragment replacingFragment) {
        this.replacingFragment = replacingFragment;
    }

}