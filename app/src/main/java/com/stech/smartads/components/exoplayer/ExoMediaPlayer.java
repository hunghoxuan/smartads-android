package com.stech.smartads.components.exoplayer;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.LoopingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by phamtuan on 14/06/2017.
 */

public class ExoMediaPlayer {
    public static final int STATE_PREPAIRED = 1;
    private SimpleExoPlayer player;
    private SimpleExoPlayerView simpleExoPlayerView;
    private DefaultBandwidthMeter bandwidthMeterA;
    private DefaultDataSourceFactory dataSourceFactory;
    private DefaultExtractorsFactory extractorsFactory;
    private ExoPlayer.EventListener listener;
    private ImageView imageView;

    private ExoMediaPlayer() {
    }

    public static class Builder {
        private SimpleExoPlayerView simpleExoPlayerView;
        private ExoPlayer.EventListener listener;

        public ExoMediaPlayer build(Context context) {
            return new ExoMediaPlayer(this).initExo(context);
        }

        public Builder setSimpleExoPlayerView(SimpleExoPlayerView simpleExoPlayerView, boolean useController) {
            this.simpleExoPlayerView = simpleExoPlayerView;
            this.simpleExoPlayerView.setUseController(useController);
            return this;
        }

        public Builder setListener(ExoPlayer.EventListener listener) {
            this.listener = listener;
            return this;
        }
    }

    private ExoMediaPlayer(Builder builder) {
        this.simpleExoPlayerView = builder.simpleExoPlayerView;
        this.listener = builder.listener;
    }

    private ExoMediaPlayer initExo(Context context) {
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        simpleExoPlayerView.requestFocus();
        simpleExoPlayerView.setPlayer(player);
        bandwidthMeterA = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(context, Util.getUserAgent(context, "exoplayer2example"), bandwidthMeterA);
        extractorsFactory = new DefaultExtractorsFactory();
        player.addListener(listener);
        return this;
    }

    /**
     * @param url video url
     * @param isLoop true if you want to play the video repetitively
     */
    public void play(String url, boolean isLoop) {
        if (url != null && !url.trim().equals("")) {
            Uri mp4VideoUri = Uri.parse(url);
            MediaSource videoSource;
            if (url.endsWith(".m3u8")) {
                videoSource = new HlsMediaSource(mp4VideoUri, dataSourceFactory, 1, null, null);
            } else {
                videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
            }
            if (isLoop) {
                LoopingMediaSource loopingMediaSource = new LoopingMediaSource(videoSource);
                player.prepare(loopingMediaSource);
            } else {
                player.prepare(videoSource);
            }
            player.setPlayWhenReady(true);
        }
    }

    public void pause() {
        if (player.getPlayWhenReady()) {
            player.setPlayWhenReady(false);
        }
    }

    public void resume() {
        if (!player.getPlayWhenReady()) {
            player.setPlayWhenReady(true);
        }
    }

    public void release() {
        if (player != null && listener != null) {
            player.removeListener(listener);
            player.release();
        }
    }

    public long getCurrentPosition() {
        return player.getCurrentPosition();
    }

    public void seekTo(long millis) {
        player.seekTo(millis);
    }
}
