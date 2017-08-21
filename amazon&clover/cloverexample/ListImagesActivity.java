package com.texasbrokers.screensaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v3.employees.Employee;
import com.google.gson.Gson;
import com.texasbrokers.screensaver.R;
import com.texasbrokers.screensaver.adapter.ImageListAdapter;
import com.texasbrokers.screensaver.listener.ListViewChangeListener;
import com.texasbrokers.screensaver.model.ImagesModel;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListImagesActivity extends Activity implements ListViewChangeListener {


    private Toolbar toolBar;
    private Context context;
    private AmazonS3Client s3;
    private ArrayList<HashMap<String, Object>> transferRecordMaps;
    private DBAdapter dbAdapter;
    private Cursor cursor;
    private List<ImagesModel> imagesModelList;
    private ListView listViewImages;
    private ImageListAdapter imageListAdapter;
    private Merchant mMerchant;
    private Employee mEmployee;
    private Bundle mBundle;
    private String merchant_id;
    private String strMerchant, strEmployee;
    private TransferUtility transferUtility;
    private PopupWindow popupWindow;
    private View viewMenu;
    private LinearLayout ll_drop_box, ll_google_drive;
    private ImageView iv_arrow_back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_images);
        context = ListImagesActivity.this;
        s3 = Util.getS3Client(context);
        transferUtility = Util.getTransferUtility(this);

        transferRecordMaps = new ArrayList<HashMap<String, Object>>();
        imagesModelList = new ArrayList<>();
        dbAdapter = new DBAdapter(context);
        mMerchant = SplashScreenActivity.mMerchant;
        mEmployee = SplashScreenActivity.mEmployee;
        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        listViewImages = (ListView) findViewById(R.id.listViewImages);
        setUpToolbar();

        /*mBundle = getIntent().getExtras();
        if (mBundle != null) {
            strMerchant = mBundle.getString(Constants.MERCHANT);
            strEmployee = mBundle.getString(Constants.EMPLOYEE);

            mMerchant = new Gson().fromJson(strMerchant, Merchant.class);
            mEmployee = new Gson().fromJson(strEmployee, Employee.class);
        }*/

        dbAdapter.open();
        cursor = dbAdapter.getImagesDetails(mMerchant.getId());
        while (cursor.moveToNext()) {
            ImagesModel imagesModel = new ImagesModel();
            imagesModel.setCursorToModel(cursor);
            imagesModelList.add(imagesModel);

        }

        dbAdapter.close();

        imageListAdapter = new ImageListAdapter(context, imagesModelList);
        listViewImages.setAdapter(imageListAdapter);

//        new GetFileListTask().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        imagesModelList = new ArrayList<>();
        dbAdapter = new DBAdapter(context);
        dbAdapter.open();
        cursor = dbAdapter.getImagesDetails(mMerchant.getId());
        while (cursor.moveToNext()) {
            ImagesModel imagesModel = new ImagesModel();
            imagesModel.setCursorToModel(cursor);
            imagesModelList.add(imagesModel);

        }

        dbAdapter.close();

        imageListAdapter = new ImageListAdapter(context, imagesModelList);
        listViewImages.setAdapter(imageListAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void setUpToolbar() {

        iv_arrow_back = (ImageView) toolBar.findViewById(R.id.iv_arrow_back);
        iv_arrow_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ListImagesActivity.super.onBackPressed();
            }
        });

        TextView tv_upload = (TextView) toolBar.findViewById(R.id.tv_upload);
        final TextView tv_download = (TextView) toolBar.findViewById(R.id.tv_download);
        tv_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbAdapter.open();
                Cursor c = dbAdapter.getImagesDetails(mMerchant.getId());

                if (c.getCount() < 10) {
                    startActivity(new Intent(ListImagesActivity.this, UploadActivity.class));
                    finish();
                } else {
                    Common.showToast(context, "You can not upload more than 10 images.");
                }
                dbAdapter.close();
            }
        });
        viewMenu = LayoutInflater.from(context).inflate(R.layout.popup_select_option_download, null);
        ll_drop_box = (LinearLayout) viewMenu.findViewById(R.id.ll_drop_box);
        ll_google_drive = (LinearLayout) viewMenu.findViewById(R.id.ll_google_drive);
        ll_drop_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(ListImagesActivity.this, WebViewActivity.class);
                intent.putExtra(Constants.IS_DROP_BOX, true);
                startActivity(intent);
            }
        });
        ll_google_drive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                Intent intent = new Intent(ListImagesActivity.this, WebViewActivity.class);
                intent.putExtra(Constants.IS_DROP_BOX, false);
                startActivity(intent);
            }
        });

        popupWindow = new PopupWindow(viewMenu, (int) Common.convertDpToPixel(200, context), ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setContentView(viewMenu);
        tv_download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(tv_download, (int) Common.convertDpToPixel(-10, context), (int) Common.convertDpToPixel(10, context));
            }
        });
    }

    @Override
    public void onEnabledDisabled(ImagesModel imagesModel, int position, boolean isChecked) {

        if (isChecked) {
            imagesModel.setIs_enable(0);
            dbAdapter.open();
            dbAdapter.updateImageDetails(imagesModel);
            dbAdapter.close();
            imagesModelList.get(position).setIs_enable(0);

        } else {
            imagesModel.setIs_enable(1);
            dbAdapter.open();
            dbAdapter.updateImageDetails(imagesModel);
            dbAdapter.close();
            imagesModelList.get(position).setIs_enable(1);
        }

        imageListAdapter.notifyDataSetChanged();
        Common.showProgressDialog(context);
        generateCSV(true, imagesModel);


    }

    @Override
    public void onEditImage(ImagesModel imagesModel, int position) {
        Intent intent = new Intent(context, UploadActivity.class);
        String strImageModel = new Gson().toJson(imagesModel);
        intent.putExtra(Constants.IMAGE_MODEL, strImageModel);
        intent.putExtra(Constants.IS_UPDATE, true);
        startActivity(intent);
    }

    @Override
    public void onRemoveImage(ImagesModel imagesModel, int position) {

        new DeleteOldFileFromServer(imagesModel, position).execute();
    }


    public class DeleteOldFileFromServer extends AsyncTask<String, String, String> {
        private ImagesModel imagesModel;
        private int position;

        public DeleteOldFileFromServer(ImagesModel imagesModel, int position) {
            this.imagesModel = imagesModel;
            this.position = position;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Common.showProgressDialog(context);

        }

        @Override
        protected String doInBackground(String... params) {
            dbAdapter.open();
            int id = dbAdapter.deleteImageDetails(imagesModel.getId());
            if (id > 0) {
                File file = new File(Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id(), imagesModel.getImage_name());
                if (file.exists()) {
                    file.delete();
                }
                s3.deleteObject(Constants.BUCKET_NAME, imagesModel.getRemote_location());


            }
            dbAdapter.close();


            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Common.dismissProgressDialog();

            imagesModelList.remove(position);
            imageListAdapter.notifyDataSetChanged();
            generateCSV(false, imagesModel);
        }
    }

    private void generateCSV(boolean isEnableDisable, ImagesModel imagesModel) {


        String fileName = Environment.getExternalStorageDirectory() + "/" + mMerchant.getId() + ".csv";

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

            uploadCSVFileInToServer(isEnableDisable, imagesModel);
//            readeCSVFile();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void uploadCSVFileInToServer(final boolean isEnableDisable, final ImagesModel imagesModel) {
        String fileName = Environment.getExternalStorageDirectory() + "/" + mMerchant.getId() + ".csv";
        File file = new File(fileName);
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + "/" + mMerchant.getId(), file.getName(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.name().equalsIgnoreCase("COMPLETED")) {
                    Common.dismissProgressDialog();
                    if (isEnableDisable) {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(Constants.MERCHANT_ID, mMerchant.getId());
                            jsonObject.put(Constants.CSV, mMerchant.getId() + "/" + mMerchant.getId() + ".csv");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        new SendPushNotification(context, Constants.EVENT_SETTING_SAVE, jsonObject.toString()).execute();
                    } else {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put(Constants.MERCHANT_ID, mMerchant.getId());
                            jsonObject.put(Constants.CSV, mMerchant.getId() + "/" + mMerchant.getId() + ".csv");
                            jsonObject.put(Constants.IMAGE, imagesModel.getImage_name());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        new SendPushNotification(context, Constants.EVENT_REMOVE_IMAGE, jsonObject.toString()).execute();
                    }
                }
                Log.d("", "");
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
