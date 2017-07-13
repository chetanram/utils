package com.screensaver.service;

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
import android.os.Process;
import android.os.SystemClock;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.screensaver.util.Constants;
import com.screensaver.util.DBAdapter;
import com.screensaver.util.PrefUtils;

import java.util.List;

/**
 * Created by chetan on 12/7/17.
 */

public class NewIdle extends Service implements LinearLayout.OnTouchListener {

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
        Log.d("IdleDetectorService", "On StartCommand");
        initTimer();

        return START_STICKY;
    }


    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        Log.d("IdleDetectorService", "Touch detected. Resetting timer");
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
        sendBroadcast(new Intent("YouWillNeverKillMe"));
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
                    Log.d("IdleDetectorService", "Inactivity detected. Sending broadcast to start the app");

                    try {
                        boolean isInForeground = new ForegroundCheckTask().execute(getApplicationContext()).get();

                        if (!isInForeground) {
                            Intent launchIntent = getApplication()
                                    .getPackageManager()
                                    .getLaunchIntentForPackage("com.screensaver");
                            if (launchIntent != null) {
                                long inActiveTime = PrefUtils.getLong(getApplicationContext(), Constants.PREF_LONG_INACTIVE, System.currentTimeMillis() - 10000) / 1000;
                                long currentTime = System.currentTimeMillis() / 1000;
                                long diffSeconds = currentTime - inActiveTime;
                                Log.d("IdleDetectorService", "App started:" + diffSeconds);
                                DBAdapter dbAdapter = new DBAdapter(getApplicationContext());
                                dbAdapter.open();
                                Cursor c = dbAdapter.getScreenSaverBasedOnMerchantId(PrefUtils.getString(getApplicationContext(), Constants.PREF_MERCHANT_ID, "123"));
                                long sec;
                                if (c.getCount() > 0) {
                                    c.moveToFirst();
                                    sec = c.getLong(c.getColumnIndex(DBAdapter.TIMEOUT));
                                } else {
                                    sec = 5;
                                }

                                dbAdapter.close();
                                if (diffSeconds >= sec) {
                                    String role = PrefUtils.getString(getApplicationContext(), Constants.PREF_ROLE, "employee");
                                    if (role.equalsIgnoreCase("admin")) {

                                    } else {
                                        getApplication().startActivity(launchIntent);
                                    }
                                }

                            }
                        }

                        stopSelf();
                    } catch (Exception e) {
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
