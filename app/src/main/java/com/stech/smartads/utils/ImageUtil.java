package com.stech.smartads.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.stech.smartads.R;
import com.stech.smartads.interfaces.IImage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by NaPro on 12/25/2015.
 */
public class ImageUtil extends BaseUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    private static int getOrientation(Context context, Uri photoUri) {
    /* it's on the external media. */
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{MediaStore.Images.ImageColumns.ORIENTATION}, null, null, null);

        try {
            if (cursor.getCount() != 1) {
                return -1;
            }

            cursor.moveToFirst();
            return cursor.getInt(0);
        } catch (NullPointerException ex) {
            CommonUtil.error(ex);
        }

        return -1;
    }

    public static Bitmap getCorrectOrientationImage(Context context, Uri photoUri, int maxDimensions) throws IOException {
        InputStream is = context.getContentResolver().openInputStream(photoUri);
        BitmapFactory.Options dbo = new BitmapFactory.Options();
        dbo.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, dbo);
        if (is != null) {
            is.close();
        }

        int rotatedWidth, rotatedHeight;
        int orientation = getOrientation(context, photoUri);

        if (orientation == 90 || orientation == 270) {
            rotatedWidth = dbo.outHeight;
            rotatedHeight = dbo.outWidth;
        } else {
            rotatedWidth = dbo.outWidth;
            rotatedHeight = dbo.outHeight;
        }

        Bitmap srcBitmap;
        is = context.getContentResolver().openInputStream(photoUri);
        if (rotatedWidth > maxDimensions || rotatedHeight > maxDimensions) {
            float widthRatio = ((float) rotatedWidth) / ((float) maxDimensions);
            float heightRatio = ((float) rotatedHeight) / ((float) maxDimensions);
            float maxRatio = Math.max(widthRatio, heightRatio);

            // Create the bitmap from file
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = (int) maxRatio;
            srcBitmap = BitmapFactory.decodeStream(is, null, options);
        } else {
            srcBitmap = BitmapFactory.decodeStream(is);
        }
        if (is != null) {
            is.close();
        }

    /*
     * if the orientation is not 0 (or -1, which means we don't know), we
     * have to do a rotation.
     */
        if (orientation > 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);

            srcBitmap = Bitmap.createBitmap(srcBitmap, 0, 0, srcBitmap.getWidth(),
                    srcBitmap.getHeight(), matrix, true);
        }

        return srcBitmap;
    }

    public static String getFilePathFromUri(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(uri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (NullPointerException ex) {
            CommonUtil.error(ex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return "";
    }

    public static String convertBitmapToBase64(Bitmap bitmap) {
        if (bitmap != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream);
            byte[] imageToByte = stream.toByteArray();
            return Base64.encodeToString(imageToByte, Base64.DEFAULT);
        }

        return "";
    }

    public static Bitmap convertBase64ToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    // Decode bitmap
    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeBitmapFromFile(String imageFile, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imageFile, options);
    }

    public static Bitmap decodeBitmapFromBitmap(Bitmap bitmap, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;

        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, blob);
        byte[] bitmapData = blob.toByteArray();

        BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(bitmapData, 0, bitmapData.length, options);
    }

    /**
     * Show original quality image
     *
     * @param ctx
     * @param imageView
     * @param imageUrl
     */
    public static void setImage(Context ctx, ImageView imageView, String imageUrl) {
        if (imageUrl != null && !imageUrl.equals("")) {

            Picasso.with(ctx).load(imageUrl).error(R.mipmap.ic_launcher)
                    .placeholder(R.drawable.placeholder).into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * Show original quality image
     *
     * @param ctx
     * @param imageView
     * @param imageUrl
     */
    public static void setImage(Context ctx, final ImageView imageView, String imageUrl, final IImage iImage) {
        if (imageUrl != null && !imageUrl.equals("")) {
            CommonUtil.log(TAG, "Url: " + imageUrl);
            Picasso.with(ctx).load(imageUrl).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    CommonUtil.log(TAG, "Bitmap loaded");
                    imageView.setImageBitmap(bitmap);
                    iImage.onBitmapLoaded(bitmap, from);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    CommonUtil.log(TAG, "Failed to load bitmap");
                    imageView.setImageResource(R.mipmap.ic_launcher);
                    iImage.onBitmapFailed(errorDrawable);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    CommonUtil.log(TAG, "Loading bitmap");
                    imageView.setImageResource(R.drawable.placeholder);
                    iImage.onPrepareLoad(placeHolderDrawable);
                }
            });
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    /**
     * Show resized image if it's too big(great than targetW or targetH)
     *
     * @param ctx
     * @param imageView
     * @param imageUrl
     * @param targetW
     * @param targetH
     */
    public static void setImage(Context ctx, ImageView imageView, String imageUrl, int targetW, int targetH) {
        if (imageUrl != null && !imageUrl.equals("")) {
            Picasso.with(ctx).load(imageUrl).error(R.mipmap.ic_launcher)
                    .resize(targetW, targetH)
                    .onlyScaleDown()
                    .centerCrop()
                    .placeholder(R.drawable.placeholder).into(imageView);
        } else {
            imageView.setImageResource(R.mipmap.ic_launcher);
        }
    }

    public static Intent getPickImageIntent(Context context) {
        Intent chooserIntent = null;

        List<Intent> intentList = new ArrayList<>();

        Intent pickIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePhotoIntent.putExtra("return-data", true);
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(context)));
        intentList = addIntentsToList(context, intentList, pickIntent);
        intentList = addIntentsToList(context, intentList, takePhotoIntent);

        if (intentList.size() > 0) {
            chooserIntent = Intent.createChooser(intentList.remove(intentList.size() - 1),
                    context.getString(R.string.pick_image_intent_text));
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentList.toArray(new Parcelable[]{}));
        }

        return chooserIntent;
    }

    private static File getTempFile(Context context) {
        String TEMP_IMAGE_NAME = "tempImage";
        File imageFile = new File(context.getExternalCacheDir(), TEMP_IMAGE_NAME);
        imageFile.getParentFile().mkdirs();
        return imageFile;
    }

    private static List<Intent> addIntentsToList(Context context, List<Intent> list, Intent intent) {
        List<ResolveInfo> resInfo = context.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo resolveInfo : resInfo) {
            String packageName = resolveInfo.activityInfo.packageName;
            Intent targetedIntent = new Intent(intent);
            targetedIntent.setPackage(packageName);
            list.add(targetedIntent);
        }
        return list;
    }

    public static Bitmap getBitmapFromIntentResult(Context context, Intent imageReturnedIntent) {
        Bitmap bm = null;
        File imageFile = getTempFile(context);
        Uri selectedImage;
        boolean isCamera = (imageReturnedIntent == null ||
                imageReturnedIntent.getData() == null ||
                imageReturnedIntent.getData().toString().contains(imageFile.toString()));
        if (isCamera) {     /** CAMERA **/
            selectedImage = Uri.fromFile(imageFile);
        } else {            /** ALBUM **/
            selectedImage = imageReturnedIntent.getData();
        }

        if (selectedImage != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(context.getContentResolver(), selectedImage);
            } catch (IOException e) {
                CommonUtil.error(e);
            }
        }

        return bm;
    }

    public static void pickImage(Activity activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        activity.startActivityForResult(intent, requestCode);
    }

    public static void pickImage(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * very simple get data from drawable. you can get drawable from imageview.
     *
     * @param drawable data
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    public static Uri getUriFromBitmap(Context context, Bitmap bmp) {
        Uri bmpUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            CommonUtil.error(e);
        }
        return bmpUri;
    }

    public static void shareImage(Activity activity, Uri uri) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        sendIntent.setType("image/*");
        activity.startActivity(Intent.createChooser(sendIntent, activity.getString(R.string.sharing)));
    }

    public static void saveImage(Context context, Bitmap finalBitmap, String imageFolder, String fileName) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/" + imageFolder);
        myDir.mkdirs();
        File file = new File(myDir, fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

            CommonUtil.log(TAG, "Save file successfully !" + imageFolder + "/" + fileName);
        } catch (Exception e) {
            CommonUtil.log(TAG, "Save file fail !" + imageFolder + "/" + fileName);
            CommonUtil.error(e);
        }
    }
}

