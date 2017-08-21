package com.texasbrokers.screensaver;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.IInterface;
import android.os.RemoteException;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.clover.sdk.util.CloverAccount;


import com.clover.sdk.v1.BindingException;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceConnector;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v1.merchant.MerchantConnector;
import com.clover.sdk.v3.apps.AppBillingInfo;
import com.clover.sdk.v3.apps.AppsConnector;
import com.clover.sdk.v3.employees.Employee;
import com.clover.sdk.v3.employees.EmployeeConnector;
import com.texasbrokers.screensaver.R;
import com.texasbrokers.screensaver.model.ImagesModel;
import com.texasbrokers.screensaver.util.Common;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.DBAdapter;
import com.texasbrokers.screensaver.util.PrefUtils;
import com.texasbrokers.screensaver.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SplashScreenActivity extends Activity {

    private AppsConnector appsConnector;
    private Account account;
    private Context context;
    private MerchantConnector merchantConnector;
    private EmployeeConnector employeeConnector;
    public static Merchant mMerchant;
    public static Employee mEmployee;
    private AppBillingInfo mAppBillingInfo;
    private String strMerchant, strEmployee;
    private DBAdapter dbAdapter;
    private TransferUtility transferUtility;
    private AmazonS3Client s3;
    private List<TransferObserver> observers;
    private File srcFile;
    private int count;
    private List<ImagesModel> imagesModelList;
    private List<S3ObjectSummary> s3ObjList;
    private String filesNames = "";
    private boolean IS_FROM_SERVICE = false;
    private boolean isMerchantChange = true;
    private boolean isBootCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        context = SplashScreenActivity.this;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            IS_FROM_SERVICE = bundle.getBoolean(Constants.IS_FROM_SERVICE, false);
        }
        dbAdapter = new DBAdapter(context);
        s3 = Util.getS3Client(this);
        transferUtility = Util.getTransferUtility(this);
        account = CloverAccount.getAccount(context);
        appsConnector = new AppsConnector(context, account);
        imagesModelList = new ArrayList<>();
       /* try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 15000);
        } catch (Exception e) {

        }*/
        Common.showProgressDialog(context);
        merchantConnector = new MerchantConnector(context, account, new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector<? extends IInterface> connector) {
                Log.d("", "");
                new GetMerchantDetails().execute();
            }

            @Override
            public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
                Log.d("", "");
//                merchantConnector.connect();
            }
        });
        employeeConnector = new EmployeeConnector(context, account, new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector<? extends IInterface> connector) {
                Log.d("", "");
                new EmployeeDetails().execute();
            }

            @Override
            public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
                Log.d("", "");
//                    employeeConnector.connect();
            }
        });
        try {
            merchantConnector.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class GetMerchantDetails extends AsyncTask<String, String, Merchant> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Merchant doInBackground(String... params) {
            try {
                return merchantConnector.getMerchant();
            } catch (RemoteException e) {
                return null;
            } catch (ClientException e) {
                return null;
            } catch (ServiceException e) {
                return null;
            } catch (BindingException e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(Merchant merchant) {
            super.onPostExecute(merchant);
            merchantConnector.disconnect();
            mMerchant = merchant;
            if (mMerchant != null) {
                srcFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + mMerchant.getId());
                if (!srcFile.exists()) {
                    srcFile.mkdir();
                }
                String mID = PrefUtils.getString(context, Constants.PREF_MERCHANT_ID, "");
                isBootCompleted = PrefUtils.getBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, false);
                isMerchantChange = !mID.equalsIgnoreCase(mMerchant.getId()) ? true : false;
                PrefUtils.saveString(context, Constants.PREF_MERCHANT_ID, mMerchant.getId());
            }

            try {
                employeeConnector.connect();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public class EmployeeDetails extends AsyncTask<String, String, Employee> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Employee doInBackground(String... params) {
            try {
                return employeeConnector.getEmployee();
            } catch (RemoteException e) {
                return null;
            } catch (ClientException e) {
                return null;
            } catch (ServiceException e) {
                return null;
            } catch (BindingException e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(Employee employee) {
            super.onPostExecute(employee);
            employeeConnector.disconnect();
            mEmployee = employee;
            if (mEmployee != null) {
                PrefUtils.saveString(context, Constants.PREF_ROLE, mEmployee.getRole().name());
            }
            new GetBillingInfo().execute();

            /*if (mEmployee.getRole().name().equalsIgnoreCase("admin")){

                startActivity(new Intent(context,SettingsActivity.class));
            }
            else if (mEmployee.getRole().name().equalsIgnoreCase("employee")){
                startActivity(new Intent(context,ScreenSaverActivity.class));
            }
            else {

            }*/


        }
    }

    public class GetBillingInfo extends AsyncTask<String, String, AppBillingInfo> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected AppBillingInfo doInBackground(String... params) {


            try {
                return appsConnector.getAppBillingInfo();
            } catch (RemoteException e) {
                return null;
            } catch (ClientException e) {
                return null;
            } catch (ServiceException e) {
                return null;
            } catch (BindingException e) {
                return null;
            }

        }

        @Override
        protected void onPostExecute(AppBillingInfo appBillingInfo) {
            super.onPostExecute(appBillingInfo);
            appsConnector.disconnect();
            mAppBillingInfo = appBillingInfo;

            if (mMerchant != null) {
                new GetFileListTask().execute();
//                goToNext();
            } else {
                Common.dismissProgressDialog();
                finish();
            }


        }
    }


    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        // The list of objects we find in the S3 bucket

        // A dialog to let the user know we are retrieving the files


        @Override
        protected void onPreExecute() {
            PrefUtils.saveBoolean(getApplicationContext(), Constants.PREF_IS_SCREEN_FOREGROUND, true);
        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Queries files in the bucket from S3.
            if (s3ObjList != null && s3ObjList.size() > 0) {
                s3ObjList.clear();
            }
            s3ObjList = s3.listObjects(Constants.BUCKET_NAME, mMerchant.getId()).getObjectSummaries();
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            count = 0;
            if (s3ObjList != null && s3ObjList.size() > 0) {


                filesNames = "";
                for (int i = 0; i < s3ObjList.size(); i++) {
                    File file = new File(Environment.getExternalStorageDirectory() + "/" + s3ObjList.get(i).getKey().toString());
                    if (!file.getName().contains(".csv")) {
                        filesNames += "'" + file.getName() + "',";
                    }
                    if (file.exists() && !file.getName().contains(".csv")) {
                        count++;
                        if (s3ObjList.size() == 1) {
                            dbAdapter.open();
                            dbAdapter.deleteAllScreenSaver(mMerchant.getId());
                            dbAdapter.deleteAllImageDetails(mMerchant.getId());
                            dbAdapter.close();
                            goToNext();
                        }
                    } else {
                        beginDownload(s3ObjList.get(i).getKey().toString());
                    }

                }


            } else {
                goToNext();
            }
        }

    }

    private void goToNext() {
        Common.dismissProgressDialog();
        if (mEmployee != null && mEmployee.getRole().name().equalsIgnoreCase("admin") && !IS_FROM_SERVICE) {
            PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
            PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, false);
            Intent intent = new Intent(context, SettingsActivity.class);

            startActivity(intent);
            finish();
        } else if (mEmployee != null && mEmployee.getRole().name().equalsIgnoreCase("admin") && IS_FROM_SERVICE) {
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, false);
            Intent intent = new Intent(context, ScreenSaverActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else if (mEmployee != null && mEmployee.getRole().name().equalsIgnoreCase("employee")) {
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, false);
            PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, true);

            Intent intent = new Intent(context, ScreenSaverActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
        } else {
            PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
            PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
            PrefUtils.saveBoolean(context, Constants.PREF_IS_BOOT_COMPLETED, false);
            finish();
        }
    }


    private void beginDownload(String key) {
        // Location to download files from S3 to. You can choose any accessible
        // file.
        srcFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + mMerchant.getId());
        if (!srcFile.exists()) {
            srcFile.mkdir();
        }
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + key);

        // Initiate the download

        TransferObserver observer = transferUtility.download(Constants.BUCKET_NAME, key, file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.name().equalsIgnoreCase("completed")) {
                    count++;

                } else if (state.name().equalsIgnoreCase("failed")) {
                    count++;
                }
                if (count == s3ObjList.size()) {
                    readCSVFile();
                    if (filesNames != null && !filesNames.equalsIgnoreCase("")) {
                        dbAdapter.open();
                        filesNames = filesNames.substring(0, filesNames.length() - 1);
                        Cursor c = dbAdapter.getImagesDetailsNotInImageName(filesNames, mMerchant.getId());
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                File file = new File(Environment.getExternalStorageDirectory() + "/" + c.getString(c.getColumnIndex(DBAdapter.MERCHANT_ID)) + "/" + c.getString(c.getColumnIndex(DBAdapter.IMAGE_NAME)));
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            dbAdapter.deleteImagesDetailsNotInImageName(filesNames, mMerchant.getId());
                            dbAdapter.close();
                            goToNext();
                        } else {
                            goToNext();
                        }
                    } else {
                        goToNext();
                    }

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

    public void readCSVFile() {
        imagesModelList.clear();
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + mMerchant.getId() + "/" + mMerchant.getId() + ".csv");
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
                            imagesModelList.add(imagesModel);
                            dbAdapter.open();
                            dbAdapter.insertUpdateImageDetails(imagesModel);
                            dbAdapter.close();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
