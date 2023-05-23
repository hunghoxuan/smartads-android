package com.stech.smartads.components;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.stech.smartads.core.MainApplication;
import com.stech.smartads.config.Constants;
import com.stech.smartads.core.AppData;
import com.stech.smartads.utils.CacheManager;
import com.stech.smartads.utils.CommonUtil;
import com.stech.smartads.utils.DateTimeUtil;
import com.stech.smartads.utils.LocalBroadCastUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class DownloadService extends AsyncTask<String,String,Boolean> {

    public static final String STATUS_FILE_EXISTED = "STATUS_FILE_EXISTED";
    public static final String STATUS_DOWNLOAD_COMPLETED = "STATUS_DOWNLOAD_COMPLETED";
    public static final String STATUS_ERROR_SERVER = "STATUS_ERROR_SERVER";


    private String fullPathCacheFile = "";
    private String urlFrom="";
    private String folderCache="";
    private Context context;
    private long startTime = 0;
    private long downloadedFileLength = 0;
    private long totalFileSize = 0;
    private int requestTime = 0;
    public static boolean isDownloading = false;


    public DownloadService(Context context, String folderCache, int requestTime){
        this.context = context;
        this.folderCache = folderCache;
        this.requestTime = requestTime;

        //create folder cache if it is not available
        File dataFolder = new File(folderCache);
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        isDownloading = true;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        urlFrom = params[0];

        if(urlFrom == null || urlFrom.isEmpty())return false;

        int count;

        try {
            //this time for calculate download time
            startTime = DateTimeUtil.getCurrentTime(((MainApplication) context.getApplicationContext()).getCurrentCalendar(),DateTimeUtil.SECOND);

            BufferedInputStream inputStream = null;
            BufferedOutputStream outputStream = null;

            URL urlFile = new URL(urlFrom);
            HttpURLConnection connection = (HttpURLConnection) urlFile.openConnection();
            connection.setRequestProperty("Accept-Encoding", "identity");

            //totalFileSize = connection.getContentLength();

            //Process Check file
            //process to get external file name
            String[] name = urlFrom.trim().replace(" ","%20").split("/");
            String nameFile = name[name.length-1];

            fullPathCacheFile=folderCache+File.separator+nameFile;

            //check to delete file if existed
            File file = new File(fullPathCacheFile);

            if(file.exists()){
                downloadedFileLength = file.length();

//                if(downloadedFileLength == totalFileSize){
//                    return true;
//                }

                connection.setRequestProperty("Range", "bytes=" + downloadedFileLength + "-");
                outputStream = new BufferedOutputStream(new FileOutputStream(file, true));

            } else{
                outputStream = new BufferedOutputStream(new FileOutputStream(file));
            }

            connection.connect();

            // Get the response code
            int statusCode = connection.getResponseCode();

            InputStream is = null;

            if (statusCode >= 200 && statusCode < 400) {
                // Create an InputStream in order to extract the response object
                is = connection.getInputStream();
            }
            else {
                is = connection.getErrorStream();
            }

            inputStream = new BufferedInputStream(is);


            byte buffer[] = new byte[1024];
            while ((count = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, count);
                downloadedFileLength+= count;
            }

            // flushing output
            inputStream.close();
            outputStream.flush();
            outputStream.close();

        } catch (Exception e) {
            CommonUtil.error(e);
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        long endTime = DateTimeUtil.getCurrentTime(((MainApplication)context.getApplicationContext()).getCurrentCalendar(),DateTimeUtil.SECOND);

        //save to cache preferences
        if(result) {
            CacheManager.storeCacheFile(context,urlFrom, fullPathCacheFile);
            CommonUtil.error("DownloadUtil","Download success :" + fullPathCacheFile + ". Download success time :"+(endTime - startTime) + " seconds - file size :" + downloadedFileLength/(1024*1024) + " mb");
            //send notification to app
            Bundle bundle = new Bundle();
            bundle.putString(Constants.PARAM_FILEURL, urlFrom);
            bundle.putString(Constants.PARAM_STATUS, DownloadService.STATUS_DOWNLOAD_COMPLETED);
            LocalBroadCastUtil.sendBroadcastListener(context,LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED,bundle);

            //send to server
            AppData.getInstance(context).downloadFile(urlFrom,true,null);

            isDownloading = false;

        }else{

            //download again
            if(requestTime < 5) {
                CommonUtil.error("DownloadUtil","Download failed :"+urlFrom + ". Try again " + requestTime);
                CacheManager.downloadFile(context, urlFrom,requestTime++);
            } else {
                isDownloading = false;
                CommonUtil.error("DownloadUtil","Download failed :"+urlFrom +" - requested more than 5 times");
                //send notification to app
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PARAM_FILEURL,urlFrom);
                bundle.putString(Constants.PARAM_STATUS,STATUS_ERROR_SERVER);
                LocalBroadCastUtil.sendBroadcastListener(context,LocalBroadCastUtil.ACTION_DOWNLOAD_FILE_COMPLETED, bundle);
            }
        }
    }
}
