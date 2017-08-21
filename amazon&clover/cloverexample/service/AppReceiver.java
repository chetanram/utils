package com.texasbrokers.screensaver.service;

import android.content.Context;
import android.os.Environment;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.clover.sdk.v1.app.AppNotification;
import com.clover.sdk.v1.app.AppNotificationReceiver;
import com.google.gson.Gson;
import com.texasbrokers.screensaver.ScreenSaverActivity;
import com.texasbrokers.screensaver.listener.OnServerDataChange;
import com.texasbrokers.screensaver.model.ImagesModel;
import com.texasbrokers.screensaver.model.PushNotificationModel;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.DBAdapter;
import com.texasbrokers.screensaver.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by chetan on 28/7/17.
 */

public class AppReceiver extends AppNotificationReceiver {

    private OnServerDataChange onServerDataChange;
    private TransferUtility transferUtility;
    private DBAdapter dbAdapter;
    private int count;

    public AppReceiver() {

    }

    public AppReceiver(Context context) {
        super(context);
    }

    @Override
    public void onReceive(Context context, AppNotification notification) {
        final PushNotificationModel pushNotificationModel = new Gson().fromJson(notification.payload, PushNotificationModel.class);
        if (pushNotificationModel != null) {

            transferUtility = Util.getTransferUtility(context);
            dbAdapter = new DBAdapter(context);
            if (notification.appEvent.equalsIgnoreCase(Constants.EVENT_SETTING_SAVE)) {

                File file = new File(Environment.getExternalStorageDirectory() + "/" + pushNotificationModel.getCsv());

                // Initiate the download

                TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, pushNotificationModel.getCsv(), file);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state.name().equalsIgnoreCase("completed")) {
                            readCSVFile(pushNotificationModel);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                    }

                    @Override
                    public void onError(int id, Exception ex) {

                    }
                });

            } else if (notification.appEvent.equalsIgnoreCase(Constants.EVENT_UPLOAD_IMAGE)) {
                count = 0;
                beginDownload(pushNotificationModel.getImage(), pushNotificationModel);
                beginDownload(pushNotificationModel.getCsv(), pushNotificationModel);
            } else if (notification.appEvent.equalsIgnoreCase(Constants.EVENT_REMOVE_IMAGE)) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + pushNotificationModel.getCsv());

                // Initiate the download

                TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, pushNotificationModel.getCsv(), file);
                observer.setTransferListener(new TransferListener() {
                    @Override
                    public void onStateChanged(int id, TransferState state) {
                        if (state.name().equalsIgnoreCase("completed")) {
                            File f = new File(Environment.getExternalStorageDirectory() + "/" + pushNotificationModel.getImage());
                            if (f.exists()) {
                                f.delete();
                            }
                            readCSVFile(pushNotificationModel);
                        }
                    }

                    @Override
                    public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

                    }

                    @Override
                    public void onError(int id, Exception ex) {

                    }
                });
            }
        }
    }

    private void beginDownload(String key, final PushNotificationModel pushNotificationModel) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + key);

        // Initiate the download

        TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, key, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.name().equalsIgnoreCase("completed")) {
                    count++;
                }
                if (count == 2) {
                    readCSVFile(pushNotificationModel);
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

            }

            @Override
            public void onError(int id, Exception ex) {

            }
        });
    }

    public void readCSVFile(PushNotificationModel pushNotificationModel) {
        File file = new File(Environment.getExternalStorageDirectory() + "/" + pushNotificationModel.getCsv());
        if (file.exists()) {
            FileInputStream fIn = null;
            try {
                fIn = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String row = "";
            String aBuffer = "";
            try {
                while ((row = myReader.readLine()) != null) {
//                aBuffer += aDataRow + "\n";
                    String[] str = row.split(",");
                    if (str.length <= 5) {
                        if (str[0].equalsIgnoreCase(DBAdapter._ID)) {

                        } else {
                            int _id = Integer.parseInt(str[0]);
                            String merchant_id = str[1];
                            int status = Integer.parseInt(str[2]);
                            long timeout = Long.parseLong(str[3]);
                            long interval = Long.parseLong(str[4]);

                            dbAdapter.open();
                            dbAdapter.insertUpdateScreenSaver(_id, merchant_id, status, timeout, interval);
                            dbAdapter.close();
                        }
                    } else {
                        if (str[0].equalsIgnoreCase(DBAdapter._ID)) {

                        } else {
                            ImagesModel imagesModel = new ImagesModel();
                            imagesModel.setStringArrayToModel(str);
                            dbAdapter.open();
                            dbAdapter.insertUpdateImageDetails(imagesModel);
                            dbAdapter.close();
                        }
                    }


                }
                if (ScreenSaverActivity.context != null) {
                    onServerDataChange = (OnServerDataChange) ScreenSaverActivity.context;
                    onServerDataChange.onDataChange(null);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
