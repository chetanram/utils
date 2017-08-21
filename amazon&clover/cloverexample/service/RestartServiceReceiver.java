package com.texasbrokers.screensaver.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.PrefUtils;


/**
 * Created by chetan on 12/7/17.
 */

public class RestartServiceReceiver extends BroadcastReceiver {
    private static final String TAG = "RestartServiceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        if (intent.getAction().equalsIgnoreCase(Intent.ACTION_BOOT_COMPLETED)) {
            PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, true);
        } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SHUTDOWN)) {
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, true);
        } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_REBOOT)) {
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, true);
        }
        context.startService(new Intent(context.getApplicationContext(), BackgroundIdleService.class));

    }
}
