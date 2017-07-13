package com.screensaver;

import android.accounts.Account;
import android.app.Activity;
import android.app.ProgressDialog;
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
import com.screensaver.model.ImagesModel;
import com.screensaver.util.Common;
import com.screensaver.util.Constants;
import com.screensaver.util.DBAdapter;
import com.screensaver.util.PrefUtils;
import com.screensaver.util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        context = SplashScreenActivity.this;
        dbAdapter = new DBAdapter(context);
        s3 = Util.getS3Client(this);
        transferUtility = Util.getTransferUtility(this);
        account = CloverAccount.getAccount(context);
        appsConnector = new AppsConnector(context, account);
        imagesModelList = new ArrayList<>();
        Common.showProgressDialog(context);
        merchantConnector = new MerchantConnector(context, account, new ServiceConnector.OnServiceConnectedListener() {
            @Override
            public void onServiceConnected(ServiceConnector<? extends IInterface> connector) {
                Log.e("", "");
                new GetMerchantDetails().execute();
            }

            @Override
            public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
                Log.e("", "");
                merchantConnector.connect();
            }
        });
        merchantConnector.connect();


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
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Merchant merchant) {
            super.onPostExecute(merchant);
            mMerchant = merchant;
            if (mMerchant != null) {
                PrefUtils.saveString(context,Constants.PREF_MERCHANT_ID,mMerchant.getId());
            }
            employeeConnector = new EmployeeConnector(context, account, new ServiceConnector.OnServiceConnectedListener() {
                @Override
                public void onServiceConnected(ServiceConnector<? extends IInterface> connector) {
                    Log.e("", "");
                    new EmployeeDetails().execute();
                }

                @Override
                public void onServiceDisconnected(ServiceConnector<? extends IInterface> connector) {
                    Log.e("", "");
                    employeeConnector.connect();
                }
            });
            employeeConnector.connect();

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
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Employee employee) {
            super.onPostExecute(employee);
            mEmployee = employee;
            if (mEmployee != null) {
                PrefUtils.saveString(context,Constants.PREF_ROLE,mEmployee.getRole().name());
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
                e.printStackTrace();
            } catch (ClientException e) {
                e.printStackTrace();
            } catch (ServiceException e) {
                e.printStackTrace();
            } catch (BindingException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(AppBillingInfo appBillingInfo) {
            super.onPostExecute(appBillingInfo);
            mAppBillingInfo = appBillingInfo;
            new GetFileListTask().execute();


        }
    }


    private class GetFileListTask extends AsyncTask<Void, Void, Void> {
        // The list of objects we find in the S3 bucket

        // A dialog to let the user know we are retrieving the files
        private ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(Void... inputs) {
            // Queries files in the bucket from S3.
            if (s3ObjList != null && s3ObjList.size() > 0) {
                s3ObjList.clear();
            }
            s3ObjList = s3.listObjects(Constants.BUCKET_NAME, mMerchant.getId() + "/" + mMerchant.getId() + ".csv").getObjectSummaries();
            if (s3ObjList.size() > 0) {
                s3ObjList = s3.listObjects(Constants.BUCKET_NAME, mMerchant.getId()).getObjectSummaries();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            count = 0;
            if (s3ObjList != null && s3ObjList.size() > 1) {


                boolean isDownload = false;
                filesNames = "";
                String key = "";
                for (int i = 0; i < s3ObjList.size(); i++) {

                    File file = new File(Environment.getExternalStorageDirectory() + "/" + s3ObjList.get(i).getKey().toString());
                    if (!file.getName().contains(".csv")) {
                        filesNames += "'" + file.getName() + "',";
                    } else {
                        key = s3ObjList.get(i).getKey();
                    }
                    if (file.exists() && !file.getName().contains(".csv")) {
                        count++;
                    } else {
                        isDownload = true;
                        beginDownload(s3ObjList.get(i).getKey().toString());
                    }

                }

                if (!isDownload) {
                    downloadCSV(key);
                }


            } else if (s3ObjList != null && s3ObjList.size() == 1) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + mMerchant.getId());
                if (s3ObjList.get(0).getKey().contains(".csv")) {
                    if (file.listFiles() != null && file.listFiles().length > 0) {
                        for (File f : file.listFiles()) {
                            if (f.getName().contains(".csv")) {

                            } else {
                                if (f.exists()) {
                                    f.delete();
                                }
                            }
                        }
                    }
                    dbAdapter.open();
                    dbAdapter.deleteAllScreenSaver(mMerchant.getId());
                    dbAdapter.deleteAllImageDetails(mMerchant.getId());
                    dbAdapter.close();
                    generateCSV();
                } else {

                    if (file.exists()) {
                        file.delete();
                    }
                    goToNext();
                }


            } else {
                srcFile = new File(Environment.getExternalStorageDirectory().toString() + "/" + mMerchant.getId());
                if (srcFile.exists()) {
                    for (File file : srcFile.listFiles()) {
                        if (file.exists()) {
                            file.delete();
                        }

                    }

                }
                dbAdapter.open();
                dbAdapter.deleteAllScreenSaver(mMerchant.getId());
                dbAdapter.deleteAllImageDetails(mMerchant.getId());
                dbAdapter.close();
                goToNext();
            }

        }

    }

    private void goToNext() {
        if (mEmployee != null && mEmployee.getRole().name().equalsIgnoreCase("admin")) {

            Intent intent = new Intent(context, SettingsActivity.class);
                           /* strMerchant = new Gson().toJson(mMerchant);
                            strEmployee = new Gson().toJson(mEmployee);
                            intent.putExtra(Constants.MERCHANT, strMerchant);
                            intent.putExtra(Constants.EMPLOYEE, strEmployee);*/

            startActivity(intent);
            finish();
//                startActivity(new Intent(context, ScreenSaverActivity.class));
//                finish();
        } else if (mEmployee != null && mEmployee.getRole().name().equalsIgnoreCase("employee")) {
            startActivity(new Intent(context, ScreenSaverActivity.class));
            finish();
        } else {
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
                    if (count == s3ObjList.size()) {
                        readCSVFile();
                        if (filesNames != null && !filesNames.equalsIgnoreCase("")) {
                            dbAdapter.open();
                            filesNames = filesNames.substring(0, filesNames.length() - 1);
                            Cursor c = dbAdapter.getImagesDetailsNotInImageName(filesNames, mMerchant.getId());
                            if (c.getCount() > 0) {
                                while (c.moveToNext()) {
                                    File file = new File(c.getString(c.getColumnIndex(DBAdapter.LOCAL_LOCATION)));
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                }
                                dbAdapter.deleteImagesDetailsNotInImageName(filesNames, mMerchant.getId());
                                dbAdapter.close();
                                generateCSV();
                            } else {
                                generateCSV();
                            }
                        } else {
                            generateCSV();
                        }

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
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new DownloadListener());
    }

    private void downloadCSV(String key) {
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
                    readCSVFile();
                    if (filesNames != null && !filesNames.equalsIgnoreCase("")) {
                        dbAdapter.open();
                        filesNames = filesNames.substring(0, filesNames.length() - 1);
                        Cursor c = dbAdapter.getImagesDetailsNotInImageName(filesNames, mMerchant.getId());
                        if (c.getCount() > 0) {
                            while (c.moveToNext()) {
                                File file = new File(c.getString(c.getColumnIndex(DBAdapter.LOCAL_LOCATION)));
                                if (file.exists()) {
                                    file.delete();
                                }
                            }
                            dbAdapter.deleteImagesDetailsNotInImageName(filesNames, mMerchant.getId());
                            dbAdapter.close();
                            generateCSV();
                        }
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
        /*
         * Note that usually we set the transfer listener after initializing the
         * transfer. However it isn't required in this sample app. The flow is
         * click upload button -> start an activity for image selection
         * startActivityForResult -> onActivityResult -> beginUpload -> onResume
         * -> set listeners to in progress transfers.
         */
        // observer.setTransferListener(new DownloadListener());
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
                        if (str[0].equalsIgnoreCase("_id")) {

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
                        if (str[0].equalsIgnoreCase("_id")) {

                        } else {
                            ImagesModel imagesModel = new ImagesModel();
                            imagesModel.setStringArrayToModel(str);
                            imagesModelList.add(imagesModel);
                            dbAdapter.open();
                            dbAdapter.insertUpdateImageDetails(imagesModel);
                            dbAdapter.close();
                        }
                    }

                    Log.e("", "" + row);
                }
           /* if (imagesModelList != null && imagesModelList.size() > 0) {
                for (int i=0;i<imagesModelList.size();i++){

                }
            }*/
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateCSV() {

        File srcFile = new File(Environment.getExternalStorageDirectory() + "/" + SplashScreenActivity.mMerchant.getId() + "/" + SplashScreenActivity.mMerchant.getId() + ".csv");
        String fileName = srcFile.getAbsolutePath();

        try {
            dbAdapter.open();

            Cursor cursor = dbAdapter.getScreenSaverBasedOnMerchantId(SplashScreenActivity.mMerchant.getId());
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

                Cursor imageCursor = dbAdapter.getImagesDetails(SplashScreenActivity.mMerchant.getId());
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

        File file = new File(Environment.getExternalStorageDirectory() + "/" + SplashScreenActivity.mMerchant.getId() + "/" + SplashScreenActivity.mMerchant.getId() + ".csv");

        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + "/" + SplashScreenActivity.mMerchant.getId(), file.getName(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.name().equalsIgnoreCase("COMPLETED")) {

                    readCSVFile();
                    goToNext();
                }
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
}
