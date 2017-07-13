package com.screensaver;

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
import com.screensaver.util.Common;
import com.screensaver.util.Constants;
import com.screensaver.util.DBAdapter;
import com.screensaver.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class SettingsActivity extends Activity {

    private Toolbar toolBar;
    private LinearLayout ll_content_screen, ll_screen_saver_images;
    private Switch switchScreenSaver;
    private DBAdapter dbAdapter;
    private Context context;
    private Cursor cursor;

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

       /* observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
        TransferListener listener = new UploadListener();
        for (TransferObserver observer : observers) {

            // For each transfer we will will create an entry in
            // transferRecordMaps which will display
            // as a single row in the UI
            HashMap<String, Object> map = new HashMap<String, Object>();
            Util.fillMap(map, observer, false);


            // Sets listeners to in progress transfers
            if (TransferState.WAITING.equals(observer.getState())
                    || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                    || TransferState.IN_PROGRESS.equals(observer.getState())) {
                observer.setTransferListener(listener);
            }
        }*/
//        observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);


       /* mBundle = getIntent().getExtras();
        if (mBundle != null) {
            strMerchant = mBundle.getString(Constants.MERCHANT);
            strEmployee = mBundle.getString(Constants.EMPLOYEE);

            mMerchant = new Gson().fromJson(strMerchant, Merchant.class);
            mEmployee = new Gson().fromJson(strEmployee, Employee.class);
        }*/
        systemScreenTimeout = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        switchScreenSaver = (Switch) findViewById(R.id.switchScreenSaver);
        ll_content_screen = (LinearLayout) findViewById(R.id.ll_content_screen);
        ll_screen_saver_images = (LinearLayout) findViewById(R.id.ll_screen_saver_images);

        editTextTimeout = (EditText) findViewById(R.id.editTextTimeout);
        editTextImageInterval = (EditText) findViewById(R.id.editTextImageInterval);

        timeout = (systemScreenTimeout / 1000) - 5;
        editTextTimeout.setText("" + timeout);
        editTextImageInterval.setText("" + interval);

        dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        cursor = dbAdapter.getScreenSaverBasedOnMerchantId(mMerchant.getId());

//        cursor = dbAdapter.getScreenSaver();
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
//            switchScreenSaver.setSelected(true);
        } else {
            switchScreenSaver.setChecked(false);
//            switchScreenSaver.setSelected(false);
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
                        Common.showToast(context, "Please enter screen timeout is less then system timeout.");
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
            Common.showToast(context, "Enter Timeout");
        } else if (flag && editTextImageInterval.getText().toString() != null && editTextImageInterval.getText().toString().equalsIgnoreCase("")) {
            Common.showToast(context, "Enter Interval");
        } else {
            timeout = Long.parseLong(editTextTimeout.getText().toString());
            interval = Long.parseLong(editTextImageInterval.getText().toString());
            dbAdapter.open();
            boolean isSaved = dbAdapter.updateScreenSaver(id, status, timeout, interval);
            dbAdapter.close();

            if (isSaved) {
                Common.showToast(context, "Settings Saved Successfully");
                generateCSV();
            } else {
                Common.showToast(context, "Please Try Again");
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
                Log.e("", "");
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                Log.e("", "");
            }

            @Override
            public void onError(int id, Exception ex) {
                Log.e("", "");
            }
        });

    }

    public void readeCSVFile() {
        FileInputStream fIn = null;
        try {
            fIn = new FileInputStream(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
        String aDataRow = "";
        String aBuffer = "";
        try {
            while ((aDataRow = myReader.readLine()) != null) {
//                aBuffer += aDataRow + "\n";
                Log.e("", "" + aDataRow);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goForScreenSaver(View view) {
        startActivity(new Intent(context, ScreenSaverActivity.class));
    }

    private class UploadListener implements TransferListener {

        // Simply updates the UI list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("Settings", "Error during upload: " + id, e);

        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("Settings", String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));

        }

        @Override
        public void onStateChanged(int id, TransferState newState) {
            Log.d("Settings", "onStateChanged: " + id + ", " + newState);

        }
    }
}
