package com.stech.smartads.components.exoplayer;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;

/**
 * Created by phamtuan on 14/06/2017.
 */

public abstract class ListenerPlayer implements ExoPlayer.EventListener {

    public abstract void onPlayerStateChanged(boolean playWhenReady, int playbackState);


    public abstract void onPlayerError(ExoPlaybackException error);
}
