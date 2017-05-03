package com.agc.report.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.agc.report.R;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Common {

    private static Dialog dialog;
    private static Dialog progressDialog;
    private static ProgressBar progressBar;


    public static boolean isEmail(String email) {

        Pattern emailPattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");

        if ((emailPattern.matcher(email)).find())
            return true;

        return false;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static String imageToString(Bitmap BitmapData) {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        BitmapData.compress(Bitmap.CompressFormat.PNG, 100, bos);
        byte[] byte_arr = bos.toByteArray();

        String file = Base64.encodeToString(byte_arr, Base64.DEFAULT);
        //appendLog(file);
        return file;
    }

    public static boolean checkIsMarshMallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? true : false;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isPermissionNotGranted(Context context, String[] permissions) {
        boolean flag = false;
        for (int i = 0; i < permissions.length; i++) {
            if (context.checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                flag = true;
                break;
            }


        }
        return flag;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestPermissions(Activity activity, String[] permissions, int resultCode) {
        activity.requestPermissions(permissions, resultCode);
    }

    public static void whichPermisionNotGranted(Context context, String[] permissions, int[] grantResults) {
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                showToast(context, "Authentication Permission Not Enabled");
                break;
            }
        }
    }

    public static String getConvertDate(String sourceFormat, String destFormat, String strDate) {
        String finalDate = "";
        try {

            DateFormat srcDf = new SimpleDateFormat(sourceFormat);
            // parse the birthDate string into time object
            Date date = srcDf.parse(strDate);
            DateFormat destDf = new SimpleDateFormat(destFormat);
            // format the birthDate into another format
            finalDate = destDf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;

    }


    public static void showProgressDialog(Context context) {

        try {


            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            dialog = new Dialog(context, R.style.AppTheme);
//           dialog.getWindow().getAttributes().windowAnimations = R.style.ProgressDialogAnimation;

            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setContentView(R.layout.custom_progress_dialog);
            dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissProgressDialog() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isInternetAvailable(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();

        if (netInfo != null && netInfo.isConnected() && netInfo.isAvailable())
            return true;

        return false;
    }


    public static long getDateTimeStamp(String format, String date) {
        long timeStamp = 0;
        DateFormat formatter = new SimpleDateFormat(format);
        Date mDate = null;
        try {
            mDate = (Date) formatter.parse(date);
            timeStamp = mDate.getTime();
        } catch (ParseException e) {
            timeStamp = 0;
            e.printStackTrace();
        }
        return timeStamp;
    }

    public static long getCurrentTimeStamp() {

        Calendar c = Calendar.getInstance();
        String date = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DAY_OF_MONTH);
        return getDateTimeStamp("yyyy-MM-dd", date);
    }

    public static boolean isAutomaticTimeZoneOff(Context context) {
        int autoTime = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 0);
        int autoTimeZone = Settings.Global.getInt(context.getContentResolver(), Settings.Global.AUTO_TIME_ZONE, 0);

        if (autoTime == 0 && autoTimeZone == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static String getDateFromTimeStamp(long timeStamp, String dateFormat) {
        DateFormat objFormatter = new SimpleDateFormat(dateFormat);

        Calendar objCalendar = Calendar.getInstance();
        objCalendar.setTimeInMillis(timeStamp);
        String result = objFormatter.format(objCalendar.getTime());
        objCalendar.clear();
        return result;
    }

    public static Typeface getTypeFace(Context context) {
        Typeface font = Typeface.createFromAsset(context.getAssets(), "fontawesome-webfont.ttf");
        return font;
    }

    public static String getTime(int hours, int minutes) {
        String time = hours + ":" + minutes;
        String strTime = "";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date dateObj = sdf.parse(time);
            strTime = new SimpleDateFormat("hh:mm aa").format(dateObj);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return strTime;

    }

    public static int getHours(String time) {

        String strTime = "";
        int mTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            Date dateObj = sdf.parse(time);
            strTime = new SimpleDateFormat("HH").format(dateObj);
            mTime = Integer.parseInt(strTime);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return mTime;

    }

    public static int getMinutes(String time) {

        String strTime = "";
        int mTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            Date dateObj = sdf.parse(time);
            strTime = new SimpleDateFormat("mm").format(dateObj);
            mTime = Integer.parseInt(strTime);
        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return mTime;

    }

    public static String getDiffTime(String startTime, String endTime) {

        String strTime = "";
        long diffTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            diffTime = endDate.getTime() - startDate.getTime();
            int m = (int) (diffTime / (60 * 1000));
            int h = m / 60;
            if (m > 0) {
                if (m % 60 == 0) {
                    strTime = h + ":00";
                } else {
                    m = m % 60;
                    strTime = h + ":" + m;
                }
            } else {
                strTime = "";
            }


        } catch (final ParseException e) {
            e.printStackTrace();
        }
        return strTime;

    }

    public static boolean isValidTimeSelected(Context context, String startTime, String endTime) {

        boolean strTime = false;
        long diffTime = 0;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa");
            Date startDate = sdf.parse(startTime);
            Date endDate = sdf.parse(endTime);
            diffTime = endDate.getTime() - startDate.getTime();
            int m = (int) (diffTime / (60 * 1000));
            if (m > 0) {
                strTime = true;
            } else {
                Common.showToast(context, context.getString(R.string.end_time_should_be_more));
                strTime = false;
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        return strTime;

    }

    public static String getReportTableFormatedDate(String strDate) {
        String finalDate = "";
        try {

            DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
            // parse the birthDate string into time object
            Date date = srcDf.parse(strDate);


            Calendar c = Calendar.getInstance();
            c.setTime(date);
//            c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            String dayNumberSuffix = getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH));
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d'" + dayNumberSuffix + "' 'of' MMMM yyyy");
            finalDate = dateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getReportTableFormatSubmitedDate(String strDate) {
        String finalDate = "";
        try {

            DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
            // parse the birthDate string into time object
            Date date = srcDf.parse(strDate);


            Calendar c = Calendar.getInstance();
            c.setTime(date);
//            c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            String dayNumberSuffix = getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            finalDate = dateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getBirthDayFormatedDate(String strDate) {
        String finalDate = "";
        try {

            DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
            // parse the birthDate string into time object
            Date date = srcDf.parse(strDate);


            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            String dayNumberSuffix = getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH));
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, d'" + dayNumberSuffix + "' MMMM yyyy");
            finalDate = dateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getBirthDayOnlyDate(String strDate) {
        String finalDate = "";
        try {

            DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
            // parse the birthDate string into time object
            Date date = srcDf.parse(strDate);


            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            String dayNumberSuffix = getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH));
            SimpleDateFormat dateFormat = new SimpleDateFormat("d'" + dayNumberSuffix + "' MMMM yyyy");
            finalDate = dateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getBirthDayNameOfDay(String strDate) {
        String finalDate = "";
        try {

            DateFormat srcDf = new SimpleDateFormat("yyyy-MM-dd");
            // parse the birthDate string into time object
            Date date = srcDf.parse(strDate);


            Calendar c = Calendar.getInstance();
            c.setTime(date);
            c.set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));

            String dayNumberSuffix = getDayNumberSuffix(c.get(Calendar.DAY_OF_MONTH));
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            finalDate = dateFormat.format(c.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getNextPreviousDate(Calendar calendar) {
        String finalDate = "";
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            finalDate = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getDateDatabseFormatFromCalender(Calendar calendar) {
        String finalDate = "";
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            finalDate = dateFormat.format(calendar.getTime());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return finalDate;
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "<sup>th</sup>";
        }
        switch (day % 10) {
            case 1:
                return "<sup>st</sup>";
            case 2:
                return "<sup>nd</sup>";
            case 3:
                return "<sup>rd</sup>";
            default:
                return "<sup>th</sup>";
        }
    }

    public static String getDigit(int digit) {
        if (digit < 10) {
            return "0" + digit;
        } else {
            return "" + digit;
        }
    }

    public static void copyToClipboard(Context context, String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
    }

    /**
     * Turn drawable resource into byte array.
     *
     * @param context parent context
     * @param id      drawable resource id
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, int id) {
        Drawable drawable = ContextCompat.getDrawable(context, id);
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Turn drawable into byte array.
     *
     * @param drawable data
     * @return byte array
     */
    public static byte[] getFileDataFromDrawable(Context context, Drawable drawable) {
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
    public static void startInstalledAppDetailsActivity(Context context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }

}
