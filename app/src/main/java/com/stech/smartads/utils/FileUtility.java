package com.stech.smartads.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;

import com.android.volley.Cache;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.core.AppData;
import com.stech.smartads.core.MainApplication;
import com.stech.smartads.models.DataContentObj;

public final class FileUtility extends Activity {

	public Bitmap getBitmapFromAssets(Context context, String fileName)
			throws IOException {
		AssetManager assetManager = context.getAssets();
		InputStream istr = assetManager.open(fileName);
		Bitmap bitmap = BitmapFactory.decodeStream(istr);

		return bitmap;
	}

	public static String getStringFromFile (String filePath) throws Exception {
		File fl = new File(filePath);
		FileInputStream fin = new FileInputStream(fl);
		String ret = convertStreamToString(fin);
		//Make sure you close all streams.
		fin.close();
		return ret;
	}

	public static String convertStreamToString(InputStream is) throws Exception {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			sb.append(line).append("\n");
		}
		reader.close();
		return sb.toString();
	}

//	private void loadArchive(){
//		String rawData = null;
//		try {
//			rawData =   getStringFromFile(Environment.getExternalStorageDirectory()
//					+ File.separator+"myArchive"+".mht");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		webView.loadDataWithBaseURL(null, rawData, "application/x-webarchive-xml", "UTF-8", null);
//	}

	public static String getCachedFileNameFromURL(String url) {
		return url.trim().toLowerCase().replace("/", "_")
				.replace(".", "_")
				.replace(":", "_")+".mht";
	}

	public static boolean downloadFile(Context context,String urlFrom,String localPathFolder) {
		int count;
		String fullPathCacheFile = "";
		try {
			URL urlFile = new URL(urlFrom);
			URLConnection ucon = urlFile.openConnection();
			InputStream input = ucon.getInputStream();

			File dataFolder = new File(localPathFolder);
			if (!dataFolder.exists()) dataFolder.mkdir();

			//process to get external file name
			String[] name = urlFrom.split(".");
			String externalFileName = name[name.length-1];

			//process to get generate random file name
			String cacheFileName = DateTimeUtil.getCurrentTime(((MainApplication)context.getApplicationContext()).getCurrentCalendar(),DateTimeUtil.SECOND)+"."+externalFileName;
			fullPathCacheFile=localPathFolder+cacheFileName;

			//check to delete file if existed
			File file = new File(fullPathCacheFile);
			file.deleteOnExit();

			OutputStream output = new FileOutputStream(fullPathCacheFile);

			byte data[] = new byte[1024];
			while ((count = input.read(data)) != -1) {
				output.write(data, 0, count);
			}
			// flushing output
			output.flush();
			output.close();
			input.close();

			//save to cache preferences
			CacheManager.storeCacheFile(context,urlFrom,fullPathCacheFile);

		} catch (Exception e) {
			//delete file if error
			File file = new File(fullPathCacheFile);
			file.deleteOnExit();

			return false;
		}

		return true;
	}

	public static String readStringFromAsset(Context ctx, String fileName, boolean encodeBase64) {
		String datax = "";
		try {
			InputStream inputStream = ctx.getAssets().open(fileName);
			datax = readStringFromIS(inputStream, encodeBase64);
		} catch (IOException ioe) {
			CommonUtil.error(ioe);
		}
		return datax;
	}

	public static String readStringFromIS(InputStream inputStream) {
		return readStringFromIS(inputStream, false);
	}

	public static String readStringFromIS(InputStream inputStream, boolean encodeBase64) {
		String datax = "";
		try {
			if (encodeBase64) {
				byte[] buffer = new byte[inputStream.available()];
				inputStream.read(buffer);
				inputStream.close();
				datax = Base64.encodeToString(buffer, Base64.NO_WRAP);
			} else {
				InputStreamReader isr = new InputStreamReader(inputStream);
				BufferedReader buffreader = new BufferedReader(isr);
				String readString = buffreader.readLine();
				while (readString != null) {
					datax = datax + readString;
					readString = buffreader.readLine();
				}
				isr.close();
			}

		} catch (IOException ioe) {
			CommonUtil.error(ioe);
		}
		return datax;
	}

	public static String getSDCardFolder() {
		return getExternalStorageDirectory();
	}

	public static String getExternalStorageDirectory() {
		String folder = Environment.getExternalStorageDirectory().getAbsolutePath();
		return folder == null ? "" : folder;
	}

	public static ArrayList<DataContentObj> allImages = new ArrayList<DataContentObj>();
	public static ArrayList<DataContentObj> getImagesFromDevice(Activity activity)
	{
		if (allImages == null)
			allImages = new ArrayList<DataContentObj>();

		if (true || allImages.size() == 0) {
			allImages.clear();
			List<String> images = getImagesFromAndroidAssets(activity, AppConfigs.GALLERY_FOLDER);
			for (String file : images) {
				allImages.add(new DataContentObj(AppConfigs.GALLERY_FOLDER, file, Constants.TYPE_IMAGE));
			}

			images = getImagesFromDownloadsFolder(activity);
			for (String file : images) {
				allImages.add(new DataContentObj("Schedules", file, Constants.TYPE_IMAGE));
			}

			images = getImagesFromCamera(activity);
			for (String file : images) {
				allImages.add(new DataContentObj("SD", file, Constants.TYPE_IMAGE));
			}
		}

		return allImages;
	}

	public static ArrayList<String> getImagesFromMediaStore(Activity activity)
	{
		//Remove older images to avoid copying same image twice
		Uri uri;
		Cursor cursor;
		int column_index_data, column_index_folder_name;

		String absolutePathOfImage = null, imageName;
		ArrayList<String> result = new ArrayList<String>();

		//get all images from external storage

		uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		String[] projection = { MediaStore.MediaColumns.DATA,
				MediaStore.Images.Media.DISPLAY_NAME };

		cursor = activity.getContentResolver().query(uri, projection, null,
				null, null);

		column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

		column_index_folder_name = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

		while (cursor.moveToNext()) {
			absolutePathOfImage = StringUtil.getFullUrl(cursor.getString(column_index_data));
			imageName = cursor.getString(column_index_folder_name);
			result.add(absolutePathOfImage);
		}

		// Get all Internal storage images

		uri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

		cursor = activity.getContentResolver().query(uri, projection, null,
				null, null);

		column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

		column_index_folder_name = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);

		while (cursor.moveToNext()) {
			absolutePathOfImage = StringUtil.getFullUrl(cursor.getString(column_index_data));

			imageName = cursor.getString(column_index_folder_name);
			result.add(absolutePathOfImage);
		}

		return result;
	}

	public static ArrayList<String> getImagesFromAndroidAssets(Activity activity, String folderPath) {
		ArrayList<String> pathList = new ArrayList<String>();
		try {
			String[] files = activity.getAssets().list(folderPath);
			for (String name : files) {
				pathList.add(StringUtil.getFullUrl(folderPath + File.separator + name));
			}
		} catch (IOException e) {
			CommonUtil.error("getImagesFromFolder", e.getMessage());
		}
		return pathList;
	}

	public static List<String> getImagesFromCamera(Context context) {
		final String CAMERA_IMAGE_BUCKET_NAME = Environment.getExternalStorageDirectory().toString()+ "/DCIM/Camera";
		final String CAMERA_IMAGE_BUCKET_ID = String.valueOf(CAMERA_IMAGE_BUCKET_NAME.toLowerCase().hashCode());

		final String[] projection = { MediaStore.Images.Media.DATA };
		final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
		final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };
		final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection,
				selection,
				selectionArgs,
				null);

		ArrayList<String> result = new ArrayList<String>(cursor.getCount());
		if (cursor.moveToFirst()) {
			final int dataColumn =
					cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			do {
				final String data = cursor.getString(dataColumn);
				//CommonUtil.message(context, StringUtil.getFullUrl(data));
				result.add( StringUtil.getFullUrl(data));
			} while (cursor.moveToNext());
		}
		cursor.close();

		return result;
	}

	public static List<String> getImagesFromDownloadsFolder(Context context) {
		return getImagesFromDownloadsFolder(context, "");
	}

	public static List<String> getImagesFromDownloadsFolder(Context context, String subFolder) {
		final String folder = CacheManager.getCacheFolder() + File.separator + subFolder;
		final String CAMERA_IMAGE_BUCKET_ID = String.valueOf(folder.toLowerCase().hashCode());

		final String[] projection = { MediaStore.Images.Media.DATA };
		final String selection = MediaStore.Images.Media.BUCKET_ID + " = ?";
		final String[] selectionArgs = { CAMERA_IMAGE_BUCKET_ID };

		final Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection,
				selection,
				selectionArgs,
				null);

		ArrayList<String> result = new ArrayList<String>(cursor.getCount());
		if (cursor.moveToFirst()) {
			final int dataColumn =
					cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			do {
				final String data = cursor.getString(dataColumn);
				//CommonUtil.message(context, StringUtil.getFullUrl(data));
				result.add( StringUtil.getFullUrl(data));
			} while (cursor.moveToNext());
		}
		cursor.close();

		return result;
	}


	public static void downloadFile(Context context, String url, String downloadFolder, int requestTime){
		if(!DownloadUtil.isDownloading && url != null && !url.isEmpty())
			new DownloadUtil(context, downloadFolder,requestTime).execute(url);
	}

	public static void downloadListFile(Context context,List<String> urls, String downloadFolder) {

		for (String url:urls) {
			if(!url.isEmpty()) {
				String localUrl = CacheManager.getCacheFileUrl(context, url);
				if (localUrl.isEmpty()) {
					new DownloadUtil(context, downloadFolder,1).execute(url);
				} else {
					File file = new File(localUrl);
					if (!file.exists()) {
						new DownloadUtil(context, downloadFolder,1).execute(url);
					} else {
						CommonUtil.log("CacheManager", "Download success (file is existed): " + localUrl);
					}
				}
			}
		}
	}


	public static void unzip(File zipFile, File targetDirectory) throws IOException {
		ZipInputStream zis = new ZipInputStream(
				new BufferedInputStream(new FileInputStream(zipFile)));
		try {
			ZipEntry ze;
			int count;
			byte[] buffer = new byte[8192];
			while ((ze = zis.getNextEntry()) != null) {
				File file = new File(targetDirectory, ze.getName());
				File dir = ze.isDirectory() ? file : file.getParentFile();
				if (!dir.isDirectory() && !dir.mkdirs())
					throw new FileNotFoundException("Failed to ensure directory: " +
							dir.getAbsolutePath());
				if (ze.isDirectory())
					continue;
				FileOutputStream fout = new FileOutputStream(file);
				try {
					while ((count = zis.read(buffer)) != -1)
						fout.write(buffer, 0, count);
				} finally {
					fout.close();
				}
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
			}

		} finally {
			zis.close();
		}
	}

	public static long getAvailableDiskSize() {
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		long bytesAvailable;

		bytesAvailable = stat.getBlockSizeLong() * stat.getAvailableBlocksLong();

//        long megAvailable = bytesAvailable / (1024 * 1024);
//        CommonUtil.log("","Available MB : "+megAvailable);

		return bytesAvailable;
	}

	public static File getFolder(String folderPath) {
		File folder = new File(folderPath);
		if (!folder.exists()) {
			File wallpaperDirectory = new File(folderPath);
			wallpaperDirectory.mkdirs();
		}
		return folder;
	}

	public static File getFile(String filePath) {
		File file = new File(filePath);
		return file;
	}

	public static List<File> getSubFiles(String dirPath, String type) {
		List<File> result = new LinkedList<File>();

		File dir = new File(dirPath);
		File[] firstLevelFiles = dir.listFiles();
		if (firstLevelFiles != null && firstLevelFiles.length > 0) {
			for (File aFile : firstLevelFiles) {
				if (type.contains(Constants.TYPE_FOLDER) && aFile.isDirectory())
					result.add(aFile);
				else if (type.contains(Constants.TYPE_FILE) && aFile.isFile())
					result.add(aFile);

			}
		}
		return result;
	}

	public static List<File> getSubFolders(String dirPath) {
		return getSubFiles(dirPath, Constants.TYPE_FOLDER);
	}

	public static List<File> getSubFiles(String dirPath) {
		return getSubFiles(dirPath, Constants.TYPE_FILE);
	}

	public static String getFilePath(String url) {
		String separator = url.startsWith(File.separator) ? "" : File.separator;
		String rootAsset = AppConfigs.ASSET_ROOT_FOLDER + separator;
		String rootDownloads = Constants.PROTOCOL_FILE + File.separator + CacheManager.getCacheFolder() + separator;
		if (isExisted(rootDownloads + url))
			return rootDownloads + url;
		else
			return rootAsset + url;
	}

	public static boolean isAssetExisted(Context context, String path) {
		boolean bAssetOk = false;
		try {
			if (!path.contains(AppConfigs.ASSET_FOLDER))
				path = AppConfigs.ASSET_ROOT_FOLDER + File.separator + path;
			InputStream stream = context.getAssets().open(path);
			stream.close();
			bAssetOk = true;
		} catch (FileNotFoundException e) {
			//Log.w("IOUtilities", "assetExists failed: "+e.toString());
		} catch (IOException e) {
			//Log.w("IOUtilities", "assetExists failed: "+e.toString());
		}
		return bAssetOk;
	}

	public static boolean isExisted(Context context, String file) {
		file = file.replace(Constants.PROTOCOL_FILE, "");

		if (file.contains(AppConfigs.ASSET_FOLDER)) {
			return isAssetExisted(context, file);
		}
		File f = new File(file);
		return f.exists();
	}

	public static boolean isExisted(String file) {
		file = file.replace(Constants.PROTOCOL_FILE, "");
		File f = new File(file);
		return f.exists();
	}

}
