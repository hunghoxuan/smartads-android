package com.stech.smartads.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.Html;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.stech.smartads.config.AppConfigs;
import com.stech.smartads.config.Constants;
import com.stech.smartads.core.AppData;
import com.stech.smartads.models.LayoutFrameObj;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import java.util.Map;
/**
 * Created by NaPro on 12/06/2016.
 */

public class StringUtil extends BaseUtil {

    public static final String TAG = StringUtil.class.getSimpleName();

    public static String getFullUrl(String url) {
        return AppData.getInstance().getFullUrl(url);
    }

    public static boolean isUrlEndsWith(String url, String ends) {
        return url.toLowerCase().endsWith(ends.toLowerCase()) || url.toLowerCase().contains(ends.toLowerCase() + "?");
    }

    public static boolean isFullUrl(String url) {
        if (url == null)
            return  false;
        return url.toLowerCase().startsWith(Constants.PROTOCOL_HTTP) || url.toLowerCase().startsWith(Constants.PROTOCOL_HTTPS) || isLocalDeviceUrl(url);
    }

    public static boolean isLocalDeviceUrl(String url) {
        if (url == null)
            return  false;
        return url.toLowerCase().startsWith(Constants.PROTOCOL_FILE) || url.toLowerCase().startsWith(Constants.PROTOCOL_IMAGE);
    }

    public static String getStringContentFromAssetFile(Context context, String file, LayoutFrameObj layoutFrameObj) {
        Map<String, String> dictionary = new HashMap<String, String>();
        return getStringContentFromAssetFile(context, file, dictionary);
    }

    public static String getStringContentFromAssetFile(Context context, String file, Map<String, String> dictionary) {
        //get data from asset
        AssetManager assetManager = context.getAssets();
        String data = "";
        try {
            InputStream istr = assetManager.open(file);
            data = FileUtility.readStringFromIS(istr);
            data = data.replaceAll("<AUTHOR>", AppConfigs.AUTHOR);
            data = data.replaceAll("<LICENSE>", AppConfigs.APP_NAME);
            data = data.replaceAll("<HOMEPAGE>", AppData(context).getWebsiteUrl());
            data = data.replaceAll("<URL>", AppData(context).getWebsiteUrl());

            if (dictionary != null) {
                for (Map.Entry<String, String> entry : dictionary.entrySet()) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    data = data.replaceAll(key, value);
                }
            }

        } catch (Exception e) {
            //CommonUtil.message(context, "Error: " + e.getMessage());
            CommonUtil.error(e);
        }
        return data;
    }

    public static String convertToUrl(String url, Context context) {

        if (isFullUrl(url))
            return url;

        if (url.startsWith("/")) {
            url = AppData(context).getConfiguredAddressIp() + url;
        } else {
            url = AppData(context).getConfiguredAddressIp() + "/" + url;
        }
        return url;
    }

    public static String convertToHtml(String content, String type)  {
        content = content.trim();
        String fontSize = "4vh";
        String fontSizeBig = "30vh";
        String background = AppData.getInstance().getServerSetting().getBackground();
        String color = AppData.getInstance().getServerSetting().getFontColor();
        String speed = "30";

        if (content.startsWith("<html")) {
            return content;
        } else if (type.equals(Constants.TYPE_TEXT)) {
            return "<html style='height:100%;width:100%'> <head><meta charset='UTF-8'/></head> <style> @keyframes marquee { 0% { transform: translate(0, 0); } 100% { transform: translate(-100%, 0); } } </style> <body style='background-color:" + background + ";height:100%;width:100%'> <div class='outer' style='display: table;position: absolute;top: 0;left: 0;height: 100%;width: 100%;background-color:" + background + "'> <div class='middle' style='display: table-cell;vertical-align: middle;'> <div class='inner' style='text-align:center;margin-left: auto;margin-right: auto;'> <div class='marquee' style=\"width: 100%;height:100%;font-family:'';background-color:" + background + ";margin:0px;padding: 0px;color:" + color + ";font-size:" + fontSizeBig + ";white-space: nowrap;overflow: hidden;box-sizing: border-box;transform:scale(1,1);\"> <p style=\"display: inline-block;padding-left:100%;animation: marquee " + speed + "s linear infinite;\"><b>" +
                    content +
                    "</b></p></div> </div> </div> </div> </body> </html>";
        } else if (type.equals(Constants.TYPE_HTML)) {
            return "<html style='height:100%;width:100%'> <head><meta charset='UTF-8'/></head> <style> h1{font-size:12vh} h2 {font-size:10vh} h3 {font-size:8vh} h4 {font-size: 6vh} body {font-size:4vh;} </style> <body style='background-color:" + background + ";height:100%;width:100%;padding:50px'> <div class='outer' style='display: table;position: absolute;top: 0;left: 0;padding:20px;height: 100%;width: 100%;background-color:" + background + "'> <div class='middle' style='display: table-cell;vertical-align: middle;'> <div class='inner' style='text-align:center;margin-left: auto;margin-right: auto;'> <div class='marquee' style=\"width: 100%;height:100%;font-family:'';background-color:" + background + ";margin:0px;padding: 0px;color:" + color + ";font-size:" + fontSize + ";white-space: nowrap;overflow: hidden;box-sizing: border-box;transform:scale(1,1);\">" +
                    content +
                    "</div> </div> </div> </div> </body> </html>";
        } else if (type.equals(Constants.TYPE_IMAGE)) {
            return "<html style='height:100%;width:100%'><body style='height:100%;width:100%;background:black'> <img style='margin-left:-8px;margin-top:-8px;width:100%;height:100%;object-fit:contain' src='" +
                    content +
                    "' /> </body> </html>";
        }  else if (type.equals(Constants.TYPE_VIDEO)) {
            return "<html style='height:100%;width:100%'><style> video::-webkit-media-controls-overlay-play-button {" +
                    "            display: none !important;" +
                    "            opacity: 0;" +
                    "        }" +
                    "" +
                    "        video::-webkit-media-controls-play-button {" +
                    "            display: none !important;" +
                    "        }" +
                    "" +
                    "        video::-webkit-media-controls{" +
                    "            display: none !important;" +
                    "            -webkit-appearance: none !important;" +
                    "            opacity: 0;" +
                    "        }" +
                    "" +
                    "        *::-webkit-media-controls-panel {" +
                    "            display: none!important;" +
                    "            -webkit-appearance: none;" +
                    "        }" +
                    "" +
                    "        /* Old shadow dom for play button */" +
                    "" +
                    "        *::-webkit-media-controls-play-button {" +
                    "            display: none !important;" +
                    "            -webkit-appearance: none;" +
                    "        }" +
                    "" +
                    "        /* New shadow dom for play button */" +
                    "" +
                    "        /* This one works! */" +
                    "" +
                    "        *::-webkit-media-controls-start-playback-button {" +
                    "            display: none !important;" +
                    "            -webkit-appearance: none;" +
                    "        }</style><body style='height:100%;width:100%;background:black'> <video id='video' poster='" + AppConfigs.ASSET_ROOT_FOLDER + "/common/img_progress.jpg' loop controls=false autoplay=true style='width:100%;height:100%;'><source src='" +
                    content +
                    "' type='video/mp4' > </video> <script> var video = document.getElementById('video'); video.autoplay=true; video.play(); </script> </body> </html>";
        }

        return content;
    }

    public static String toJson(Object obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(obj);
    }

    public static Object fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Object.class);
    }

    public static String formatJson(String jsonString)
    {
        JsonParser parser = new JsonParser();
        JsonObject obj = parser.parse(jsonString).getAsJsonObject();

        return toJson(obj);
    }

    public static String getErrorMessage(Throwable error) {
        if (error == null)
            return "";
        StringBuilder builder = new StringBuilder();
        builder.append(error.toString()).append("\n--TRACE--\n");
        int j = 0;
        for (int i = 0; i < error.getStackTrace().length ; i ++) {
            if (!error.getStackTrace()[i].getClassName().startsWith(AppConfigs.SOURCE_PACKAGE))
                continue;
            j += 1;
            builder.append(error.getStackTrace()[i].getFileName() + ":" + error.getStackTrace()[i].getLineNumber() + "\n");
            if (j > AppConfigs.debugTraceLevel)
                break;
        }
        return builder.toString();
    }

    public static String convertNumberToString(float number, int numberAfterDecimal) {
        return String.format(Locale.getDefault(), "%,.0" + numberAfterDecimal + "f", number);
    }

    public static String convertNumberToString(double number, int numberAfterDecimal) {
        return String.format(Locale.getDefault(), "%,.0" + numberAfterDecimal + "f", number);
    }

    // This method is not working
    /*public static String convertNumberToString(long number, int numberAfterDecimal) {
        return String.format(Locale.US, "%,.0" + numberAfterDecimal + "f", number);
    }*/

    public static double convertStringToDecimalNumber(String strNumber) {
        if (strNumber == null || strNumber.isEmpty())
            return 0;
        DecimalFormat decimalFormat = new DecimalFormat();
        String decimalSeparator = String.valueOf(decimalFormat.getDecimalFormatSymbols().getDecimalSeparator());

        NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
        try {
            if (decimalSeparator.equals(".")) {
                if (strNumber.contains(",")) {
                    strNumber = strNumber.replace(",", "");
                }
            } else if (decimalSeparator.equals(",")) {
                if (strNumber.contains(".")) {
                    strNumber = strNumber.replace(".", "");
                }
            }

            Number number = format.parse(strNumber);
            return number.doubleValue();
        } catch (ParseException e) {
            CommonUtil.error(e);
        }

        return 0;
    }

    /**
     * Check format of email
     *
     * @param email is a email need checking
     */
    public static boolean isValidEmail(String email) {
        return !email.trim().isEmpty() && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Password is minimum 8 characters and 1 Number and not contain special character
     * exp: trang123
     */
    public static boolean isValidPassword(String password) {
        Pattern p = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(password);
        return m.find();
    }

    /**
     * Check Edit Text input string
     *
     * @param editText
     * @return
     */
    public static boolean isEmpty(EditText editText) {
        if (editText == null || editText.getEditableText() == null
                || editText.getEditableText().toString().trim().equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    public static String replaceImagePathInHtml(String htmlFileName, String imagePath, Context context) {
        String result = "";
        InputStream is;

        ArrayList<String> listUrlImage = new ArrayList<String>();
        try {
            is = context.getResources().getAssets().open(htmlFileName);

            String textfile = convertStreamToString(is);

            Pattern titleFinder = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.DOTALL
                    | Pattern.CASE_INSENSITIVE);
            Matcher regexMatcher = titleFinder.matcher(textfile);
            while (regexMatcher.find()) {
                CommonUtil.log("==== Image Src", regexMatcher.group(1));
                listUrlImage.add(regexMatcher.group(1));
            }
            for (String string : listUrlImage) {
                String fileName = string.substring(string.lastIndexOf("/") + 1, string.length());
                CommonUtil.log("Lemon", "File name :" + fileName);
                textfile = textfile.replace(string, AppConfigs.ASSET_ROOT_FOLDER  + "/" + imagePath + fileName);

            }
            result = textfile;
        } catch (IOException e) {
            CommonUtil.error(e);
        }

        return result;
    }

    public static String replaceImagePathInHtml2(String htmlInput, String imagePath, Context context) {
        String result = "";

        ArrayList<String> listUrlImage = new ArrayList<String>();
        String textfile = htmlInput.replace("\"\"", "\"");
        Pattern titleFinder = Pattern.compile("<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>", Pattern.DOTALL
                | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(textfile);
        while (regexMatcher.find()) {
            CommonUtil.log("==== Image Src", regexMatcher.group(1));
            listUrlImage.add(regexMatcher.group(1));
        }
        for (String string : listUrlImage) {
            String fileName = string.substring(string.lastIndexOf("/") + 1, string.length());
            CommonUtil.log("Lemon", "File name :" + fileName);
            textfile = textfile.replace(string, AppConfigs.ASSET_ROOT_FOLDER + imagePath + fileName);
        }
        result = textfile;
        return result;
    }

    public static String removeHtmlTags(String strHtml){

        String spanned = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(strHtml, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            spanned = Html.fromHtml(strHtml).toString();
        }

        return spanned;
    }

    public static String convertStreamToString(InputStream is) throws IOException {
        Writer writer = new StringWriter();

        char[] buffer = new char[2048];
        try {
            Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            int n;
            while ((n = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, n);
            }
        } finally {
            is.close();
        }
        String text = writer.toString();
        return text;
    }

    /**
     * Check input string
     *
     * @param editText
     * @return
     */
    public static boolean isEmpty(String editText) {
        if (editText == null || "".equals(editText.trim())) {
            return true;
        }
        return false;
    }

    public static String getSubString(String input, int maxLength) {
        String temp = input;
        if (input.length() < maxLength)
            return temp;
        else
            return input.substring(0, maxLength - 1) + "...";
    }

    /**
     * Merge all elements of a string array into a string
     *
     * @param strings
     * @param separator
     * @return
     */
    public static String join(String[] strings, String separator) {
        StringBuffer sb = new StringBuffer();
        int max = strings.length;
        for (int i = 0; i < max; i++) {
            if (i != 0)
                sb.append(separator);
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    /**
     * Initial sync date string
     *
     * @return
     */
    public static String initDateString() {
        return "1900-01-01 09:00:00";
    }

    /**
     * Convert a string divided by ";" to multiple xmpp users
     *
     * @param userString
     * @return
     */
    public static String[] convertStringToXmppUsers(String userString) {
        return userString.split(";");
    }

    /**
     * get Unique Random String
     *
     * @return
     */
    public static String getUniqueRandomString() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    /**
     * Check mail valid
     * @param email
     * @return
     */

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

}
