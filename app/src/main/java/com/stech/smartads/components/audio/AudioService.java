package com.stech.smartads.components.audio;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

import com.stech.smartads.R;
import com.stech.smartads.activities.MainActivity;
import com.stech.smartads.utils.CommonUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


public class AudioService extends Service implements
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {

    //media player
    private MediaPlayer player;
    //song list
    private LinkedList<Audio> listAudios;
    //current position
    private int songPosn;
    //binder
    private final IBinder musicBind = new MusicBinder();
    //title of current song
    private String songTitle="";
    //notification id
    private static final int NOTIFY_ID=1;
    //shuffle flag and random
    private boolean shuffle=false;
    private Random rand;

    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        songPosn=0;
        //random
        rand=new Random();
        //create player
        player = new MediaPlayer();
        //initialize
        initMusicPlayer();

        listAudios = new LinkedList<Audio>();
    }

    public void initMusicPlayer(){
        //set player properties
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //set listeners
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    //pass song list
    public void setAudios(List<Audio> theAudios){
        if (theAudios == null)
            return;
        listAudios = new LinkedList<Audio>();
        listAudios.addAll(theAudios);
    }

    public void addAudios(List<Audio> theAudios) {
        if (theAudios == null)
            return;
        listAudios.addAll(theAudios);
    }

    public void addAudios(String url) {
        listAudios.add(new Audio(url));
    }

    public void playAudio(String url) {
        addAudios(url);
        playLast();
    }

    //check Song list is ready to play
    public boolean isReadyToPlay(){
        return listAudios !=null && listAudios.size()>0;
    }


    //binder
    public class MusicBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    //activity will bind to service
    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    //release resources when unbind
    @Override
    public boolean onUnbind(Intent intent){
        player.stop();
        player.release();
        return false;
    }

    //play a song
    public void playSong() {
        if (!isReadyToPlay())
            return;

        //play
        player.reset();
        //get song
        Audio playAudio = listAudios.get(songPosn);

        //get title
        songTitle= playAudio.getTitle();
        String songUrl = playAudio.getUrl();

        //set the data source
        try {
            if (!songUrl.toLowerCase().startsWith("http")) {
                Uri trackUri = ContentUris.withAppendedId(
                        android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        getCurrentPosition());
                player.setDataSource(getApplicationContext(), trackUri);
                player.prepare();

            } else {
                player.setDataSource(playAudio.getUrl());
                player.prepareAsync();
            }

        } catch (IllegalArgumentException e) {
            CommonUtil.error(e);
        } catch (SecurityException e) {
            CommonUtil.error(e);
        } catch (IllegalStateException e) {
            CommonUtil.error(e);
        } catch (IOException e) {
            CommonUtil.error(e);
        } catch(Exception e) {
            CommonUtil.error(e);
        }
    }

    //set the song
    public void setAudio(int songIndex){
        songPosn=songIndex;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        //check if playback has reached the end of a track
        if(player.getCurrentPosition()>0){
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        CommonUtil.error("MUSIC PLAYER", "Playback Error");
        mp.reset();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //start playback
        mp.start();
        //notification

    }

    public void showNotification(){
        Intent notIntent = new Intent(this, MainActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendInt = PendingIntent.getActivity(this, 0,
                notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendInt)
                .setSmallIcon(R.drawable.exo_controls_play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle("Playing")
                .setContentText(songTitle);
        Notification not = builder.build();
        startForeground(NOTIFY_ID, not);
    }

    public void removeNotification(){
        stopForeground(true);
    }

    //playback methods
    public int getCurrentPosition(){
        return player.getCurrentPosition();
    }

    public int getDuration(){
        return player.getDuration();
    }

    public boolean isPng(){
        return player.isPlaying();
    }

    public void pausePlayer(){
        player.pause();
    }

    public void seek(int posn){
        player.seekTo(posn);
    }

    public void go(){
        player.start();
    }

    //skip to previous track
    public void playPrev(){
        songPosn--;
        if(songPosn<0) songPosn= listAudios.size()-1;
        playSong();
    }

    //skip to next
    public void playNext(){
        if(shuffle){
            int newSong = songPosn;
            while(newSong==songPosn){
                newSong=rand.nextInt(listAudios.size());
            }
            songPosn=newSong;
        }
        else{
            songPosn++;
            if(songPosn>= listAudios.size()) songPosn=0;
        }
        playSong();
    }

    public void playLast() {
        songPosn = listAudios.size() - 1;
        playSong();
    }

    @Override
    public void onDestroy() {
        removeNotification();
    }

    //toggle shuffle
    public void setShuffle(){
        if(shuffle) shuffle=false;
        else shuffle=true;
    }

}
