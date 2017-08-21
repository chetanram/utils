package com.texasbrokers.screensaver;

import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.PrefUtils;

/**
 * Created by chetan on 5/7/17.
 */

public class MyApplication extends MultiDexApplication {
    private static MyApplication myApplication;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        context = this;
//        new GetNotification().execute();
        PrefUtils.saveLong(context,Constants.PREF_LONG_INACTIVE,System.currentTimeMillis());
        sendBroadcast(new Intent(Constants.INTENT_ACTION_IDLE));
    }
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
    @Override
    public void onTerminate() {
        super.onTerminate();
        sendBroadcast(new Intent(Constants.INTENT_ACTION_IDLE));
        Log.d("IdleDetectorService","App terminate");

    }

    public static MyApplication getInstance() {
        return myApplication;
    }


}
