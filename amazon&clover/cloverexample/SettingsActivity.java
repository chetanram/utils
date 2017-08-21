package com.texasbrokers.screensaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v3.employees.Employee;

import com.texasbrokers.screensaver.R;
import com.texasbrokers.screensaver.util.Common;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.DBAdapter;
import com.texasbrokers.screensaver.util.SendPushNotification;
import com.texasbrokers.screensaver.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class SettingsActivity extends Activity {

    private Toolbar toolBar;
    private LinearLayout ll_content_screen, ll_screen_saver_images;
    private Switch switchScreenSaver;
    private DBAdapter dbAdapter;
    private Context context;
    private int id, status;
    private long timeout, interval = 3;

    private EditText editTextTimeout, editTextImageInterval;
    private Merchant mMerchant;
    private Employee mEmployee;
    private Bundle mBundle;
    private String merchant_id;
    private String strMerchant, strEmployee;
    private long systemScreenTimeout;
    private File folder;
    private String fileName;
    private TransferUtility transferUtility;
    private AmazonS3Client s3;
    private List<TransferObserver> observers;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setUpToolbar();
        context = SettingsActivity.this;
        mMerchant = SplashScreenActivity.mMerchant;
        mEmployee = SplashScreenActivity.mEmployee;
        s3 = Util.getS3Client(this);
        transferUtility = Util.getTransferUtility(this);

        systemScreenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        switchScreenSaver = (Switch) findViewById(R.id.switchScreenSaver);
        ll_content_screen = (LinearLayout) findViewById(R.id.ll_content_screen);
        ll_screen_saver_images = (LinearLayout) findViewById(R.id.ll_screen_saver_images);

        editTextTimeout = (EditText) findViewById(R.id.editTextTimeout);
        editTextImageInterval = (EditText) findViewById(R.id.editTextImageInterval);

        timeout = (systemScreenTimeout / 1000);
        editTextTimeout.setText("" + timeout);
        editTextImageInterval.setText("" + interval);

        dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        Cursor cursor = dbAdapter.getScreenSaverBasedOnMerchantId(mMerchant.getId());

        int count = cursor.getCount();
        if (count > 0) {
            cursor.moveToFirst();
            merchant_id = cursor.getString(cursor.getColumnIndex(DBAdapter.MERCHANT_ID));

            id = cursor.getInt(cursor.getColumnIndex(DBAdapter._ID));

            status = cursor.getInt(cursor.getColumnIndex(DBAdapter.STATUS));

            timeout = cursor.getLong(cursor.getColumnIndex(DBAdapter.TIMEOUT));
            interval = cursor.getLong(cursor.getColumnIndex(DBAdapter.INTERVAL));

        } else {

            id = (int) dbAdapter.insertScreenSaverSettings(mMerchant.getId(), 0, timeout, interval);
            generateCSV();
        }
        dbAdapter.close();
        editTextTimeout.setText("" + timeout);
        editTextImageInterval.setText("" + interval);


//         Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT,30000);

        ll_screen_saver_images.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SettingsActivity.this, ListImagesActivity.class);
               /* intent.putExtra(Constants.MERCHANT, strMerchant);
                intent.putExtra(Constants.EMPLOYEE, strEmployee);*/
                startActivity(intent);
            }
        });

        switchScreenSaver.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ll_content_screen.setVisibility(View.VISIBLE);
                } else {
                    ll_content_screen.setVisibility(View.GONE);
                }
            }
        });
        if (status == 0) {
            switchScreenSaver.setChecked(true);
        } else {
            switchScreenSaver.setChecked(false);
        }
        editTextTimeout.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.toString().length() > 0) {
                    long seconds = Long.parseLong(s.toString());
                    long systemTimeInSeconds = systemScreenTimeout / 1000;
                    if (seconds < systemTimeInSeconds) {

                    } else {
                        editTextTimeout.setText("");
                        Common.showToast(context, "Please enter screen timeout is less than system timeout.");
                    }
                } else {

                }

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


    }

    private void setUpToolbar() {
        TextView tv_save = (TextView) toolBar.findViewById(R.id.tv_save);
        tv_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setValues();

            }

        });
    }

    private void setValues() {
        boolean flag = switchScreenSaver.isChecked();
        if (flag) {
            status = 0;
        } else {
            status = 1;
        }
        if (flag && editTextTimeout.getText().toString() != null && editTextTimeout.getText().toString().equalsIgnoreCase("")) {
            Common.showToast(context, "Please enter screen timeout.");
        } else if (flag && Long.parseLong(editTextTimeout.getText().toString()) < 5) {

            editTextTimeout.setText("");
            Common.showToast(context, "Please enter screen timeout is more than 5 seconds.");

        } else if (flag && editTextImageInterval.getText().toString() != null && editTextImageInterval.getText().toString().equalsIgnoreCase("")) {
            Common.showToast(context, "Please enter image display interval.");
        } else {
            timeout = Long.parseLong(editTextTimeout.getText().toString());
            interval = Long.parseLong(editTextImageInterval.getText().toString());
            dbAdapter.open();
            boolean isSaved = dbAdapter.updateScreenSaver(mMerchant.getId(), id, status, timeout, interval);
            dbAdapter.close();

            if (isSaved) {
                Common.showToast(context, "Settings Saved Successfully");
                generateCSV();
            } else {
                Common.showToast(context, "Please Try Again");
                finish();
            }
        }
    }

    private void generateCSV() {
        folder = new File(Environment.getExternalStorageDirectory()
                + "/" + mMerchant.getId());
        if (!folder.exists()) {
            folder.mkdir();
        }

        fileName = folder.toString() + "/" + mMerchant.getId() + ".csv";

        try {
            dbAdapter.open();

            Cursor cursor = dbAdapter.getScreenSaverBasedOnMerchantId(mMerchant.getId());
            if (cursor.getCount() > 0) {
                FileWriter fw = new FileWriter(fileName);
                cursor.moveToFirst();

                fw.append(DBAdapter._ID);
                fw.append(",");

                fw.append(DBAdapter.MERCHANT_ID);
                fw.append(",");

                fw.append(DBAdapter.STATUS);
                fw.append(",");

                fw.append(DBAdapter.TIMEOUT);
                fw.append(",");

                fw.append(DBAdapter.INTERVAL);
                fw.append(",");

                fw.append("\n");

                fw.append(cursor.getString(cursor.getColumnIndex(DBAdapter._ID)));
                fw.append(",");

                fw.append(cursor.getString(cursor.getColumnIndex(DBAdapter.MERCHANT_ID)));
                fw.append(",");

                fw.append(cursor.getString(cursor.getColumnIndex(DBAdapter.STATUS)));
                fw.append(",");

                fw.append(cursor.getString(cursor.getColumnIndex(DBAdapter.TIMEOUT)));
                fw.append(",");

                fw.append(cursor.getString(cursor.getColumnIndex(DBAdapter.INTERVAL)));
                fw.append(",");


                fw.append("\n");

                Cursor imageCursor = dbAdapter.getImagesDetails(mMerchant.getId());
                if (imageCursor.getCount() > 0) {
                    fw.append(DBAdapter._ID);
                    fw.append(",");

                    fw.append(DBAdapter.MERCHANT_ID);
                    fw.append(",");

                    fw.append(DBAdapter.IMAGE_NAME);
                    fw.append(",");

                    fw.append(DBAdapter.REMOTE_LOCATION);
                    fw.append(",");

                    fw.append(DBAdapter.LOCAL_LOCATION);
                    fw.append(",");

                    fw.append(DBAdapter.IMAGE_IS_ENABLE);
                    fw.append(",");

                    fw.append(DBAdapter.IMAGE_IS_SCHEDULE);
                    fw.append(",");

                    fw.append(DBAdapter.AUTO_START_DATE_TIME);
                    fw.append(",");

                    fw.append(DBAdapter.AUTO_END_DATE_TIME);
                    fw.append(",");
                    fw.append("\n");
                    while (imageCursor.moveToNext()) {
                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter._ID)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.MERCHANT_ID)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.IMAGE_NAME)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.REMOTE_LOCATION)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.LOCAL_LOCATION)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.IMAGE_IS_ENABLE)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.IMAGE_IS_SCHEDULE)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.AUTO_START_DATE_TIME)));
                        fw.append(",");

                        fw.append(imageCursor.getString(imageCursor.getColumnIndex(DBAdapter.AUTO_END_DATE_TIME)));
                        fw.append(",");
                        fw.append("\n");
                    }
                }

                // fw.flush();
                fw.close();

            }
            dbAdapter.close();

            uploadCSVFileInToServer();
//            readeCSVFile();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void uploadCSVFileInToServer() {
        File file = new File(fileName);

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + "/" + mMerchant.getId(), file.getName(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {

                if (state.name().equalsIgnoreCase("completed")) {
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put(Constants.MERCHANT_ID, mMerchant.getId());
                        jsonObject.put(Constants.CSV, mMerchant.getId() + "/" + mMerchant.getId() + ".csv");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    new SendPushNotification(context, Constants.EVENT_SETTING_SAVE, jsonObject.toString()).execute();
                }

            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.d("", "");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.d("", "");
            }
        });

    }

}
