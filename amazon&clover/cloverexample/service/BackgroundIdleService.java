package com.texasbrokers.screensaver.service;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.v4.content.IntentCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.DBAdapter;
import com.texasbrokers.screensaver.util.PrefUtils;

import java.util.List;

/**
 * Created by chetan on 12/7/17.
 */

public class BackgroundIdleService extends Service implements LinearLayout.OnTouchListener {

    private Handler mHandler;
    private Runnable mRunnable;
    private final int mTimerDelay = 1000;//inactivity delay in milliseconds
    private LinearLayout mTouchLayout;//the transparent view

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mTouchLayout = new LinearLayout(this);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mTouchLayout.setLayoutParams(lp);

        // set on touch listener
        mTouchLayout.setOnTouchListener(this);

        // fetch window manager object
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        // set layout parameter of window manager

        WindowManager.LayoutParams mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT
        );

        mParams.gravity = Gravity.LEFT | Gravity.TOP;
        windowManager.addView(mTouchLayout, mParams);

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceTask = new Intent(getApplicationContext(), this.getClass());
        restartServiceTask.setPackage(getPackageName());
        PendingIntent restartPendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceTask, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager myAlarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        myAlarmService.set(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 1000,
                restartPendingIntent);
        Log.d("IdleDetectorService", "On Removed");
        startService(rootIntent);
//        super.onTaskRemoved(rootIntent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.d("IdleDetectorService", "On StartCommand");
        initTimer();

        return START_STICKY;
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        Log.d("IdleDetectorService", "Touch detected. Resetting timer");
        PrefUtils.saveLong(getApplicationContext(), Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
        initTimer();
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (windowManager != null && mTouchLayout != null) {
            windowManager.removeView(mTouchLayout);
        }
//        Log.d("IdleDetectorService", "On Destroy");
        sendBroadcast(new Intent(Constants.INTENT_ACTION_IDLE));
    }

    /**
     * (Re)sets the timer to send the inactivity broadcast
     */
    private void initTimer() {
        // Start timer and timer task
        if (mRunnable == null) {

            mRunnable = new Runnable() {
                @Override
                public void run() {
//                    Log.d("IdleDetectorService", "Inactivity detected. Sending broadcast to start the app");

                    try {
                        boolean isInForeground = new ForegroundCheckTask().execute(getApplicationContext()).get();
                        boolean isScreenForeground = PrefUtils.getBoolean(getApplicationContext(), Constants.PREF_IS_SCREEN_FOREGROUND, false);
                        if (!isScreenForeground) {
                            Intent launchIntent = getApplication()
                                    .getPackageManager()
                                    .getLaunchIntentForPackage(getApplicationContext().getPackageName());
                            launchIntent.putExtra(Constants.IS_FROM_SERVICE, true);
//                            if (launchIntent != null) {
                            long inActiveTime = PrefUtils.getLong(getApplicationContext(), Constants.PREF_LONG_INACTIVE, System.currentTimeMillis() - 10000) / 1000;
                            long currentTime = System.currentTimeMillis() / 1000;
                            long diffSeconds = currentTime - inActiveTime;
                            Log.d("IdleDetectorService", "App started:" + diffSeconds);
                            DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                            dbAdapter.open();
                            Cursor c = dbAdapter.getScreenSaverBasedOnMerchantId(PrefUtils.getString(getApplicationContext(), Constants.PREF_MERCHANT_ID, "123"));
                            int status = 0;
                            long sec;
                            if (c.getCount() > 0) {
                                c.moveToFirst();
                                sec = c.getLong(c.getColumnIndex(DBAdapter.TIMEOUT));
                                status = c.getInt(c.getColumnIndex(DBAdapter.STATUS));
                            } else {
                                sec = 5;
                                status = 1;
                            }
                            long systemScreenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
                            systemScreenTimeout = systemScreenTimeout / 1000;
                            if (systemScreenTimeout < sec) {
                                int time = (int) ((sec + 1) * 1000);
                                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, time);
                            }
                            dbAdapter.close();
                            if (diffSeconds >= sec) {

                                String role = PrefUtils.getString(getApplicationContext(), Constants.PREF_ROLE, "employee");
                                   /* if (role.equalsIgnoreCase("admin")) {

                                    } else {*/
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                                if (status == 0) {
                                    PrefUtils.saveBoolean(getApplicationContext(), Constants.PREF_IS_SCREEN_FOREGROUND, true);
                                    getApplication().startActivity(launchIntent);
                                }
//                                    }
                            }

//                            }
                        }


                        stopSelf();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
        }

        if (mHandler == null) {
            mHandler = new Handler();
        }

        mHandler.removeCallbacks(mRunnable);
        mHandler.postDelayed(mRunnable, mTimerDelay);
    }

    private class ForegroundCheckTask extends AsyncTask<Context, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Context... params) {
            final Context context = params[0];
            return isAppOnForeground(context);
        }

        private boolean isAppOnForeground(Context context) {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appProcesses = null;

            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                appProcesses = activityManager.getRunningAppProcesses();
            } else {
                //for devices with Android 5+ use alternative methods
//                appProcesses = Process.getRunningAppProcessInfo(getApplication());
                appProcesses = activityManager.getRunningAppProcesses();
            }

            if (appProcesses == null) {
                return false;
            }

            final String packageName = context.getPackageName();

            for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
                if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
                        appProcess.processName.equals(packageName)) {
                    return true;
                }
            }

            return false;
        }
    }
}
