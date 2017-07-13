package com.screensaver;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.google.firebase.iid.FirebaseInstanceId;
import com.screensaver.util.Common;
import com.screensaver.util.Constants;
import com.screensaver.util.PrefUtils;
import com.screensaver.util.SNSMobilePush;
import com.screensaver.util.Util;

import java.io.InputStream;

/**
 * Created by chetan on 5/7/17.
 */

public class MyApplication extends Application {
    private static MyApplication myApplication;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
        context = this;
//        new GetNotification().execute();
        PrefUtils.saveLong(context,Constants.PREF_LONG_INACTIVE,System.currentTimeMillis());
        sendBroadcast(new Intent("YouWillNeverKillMe"));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        sendBroadcast(new Intent("YouWillNeverKillMe"));
        Log.d("IdleDetectorService","App terminate");

    }

    public static MyApplication getInstance() {
        return myApplication;
    }

    public class GetNotification extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {


            AmazonSNS sns = null;

            try {

                InputStream inputStream = getResources().openRawResource(R.raw.aws_credentials);


                sns = new AmazonSNSClient(Util.getCredProvider(context));
                sns.setRegion(Region.getRegion(Regions.fromName(Constants.COGNITO_POOL_REGION)));


            } catch (Exception e) {
                e.printStackTrace();
            }

            sns.setEndpoint("https://sns.us-east-1.amazonaws.com");

            try {
                SNSMobilePush sample = new SNSMobilePush(sns, context);
                // TODO: Uncomment the services you wish to use. *//*

                sample.demoAndroidAppNotification(Constants.FIREBASE_WEB_SERVER_API, Constants.APP_NAME, FirebaseInstanceId.getInstance().getToken());
                // sample.demoKindleAppNotification();
                // sample.demoAppleAppNotification();
                // sample.demoAppleSandboxAppNotification();
                // sample.demoBaiduAppNotification();
                // sample.demoWNSAppNotification();
                // sample.demoMPNSAppNotification();
            } catch (AmazonServiceException ase) {
                ase.printStackTrace();
            } catch (AmazonClientException ace) {
                ace.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);


        }
    }
}
