package com.stech.smartads.components.exoplayer;

import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;


public abstract class SimpleListener extends ListenerPlayer {

//    @Override
//    public void onTimelineChanged(Timeline timeline, Object manifest) {
//
//    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

//    @Override
//    public void onPositionDiscontinuity() {
//
//    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }
}
