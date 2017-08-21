package com.texasbrokers.screensaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v3.employees.Employee;
/*import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.TokenPair;*/

import com.google.gson.Gson;
import com.texasbrokers.screensaver.model.ImagesModel;
import com.texasbrokers.screensaver.util.Common;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.DBAdapter;
import com.texasbrokers.screensaver.util.SendPushNotification;
import com.texasbrokers.screensaver.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;

public class UploadActivity extends Activity {

    private ImageView imageView;
    private TextView tv_choose_image;
    private TextView textRemoteViewLocation;
    private EditText editTextAutoStartDate, editTextAutoStartTime, editTextAutoEndDate, editTextAutoEndTime;
    private Spinner spinnerGlobalAction;
    private Switch switchAutoDeploy;
    private Switch switchAutoDeployAction;
    private Context context;
    private File srcFile, destFile;

    private int d, m, y;
    private int hr, min;
    private boolean isDateSet = false;
    private Button btn_upload;
    private DBAdapter dbAdapter;
    private Merchant mMerchant;
    private Employee mEmployee;
    private Bundle mBundle;
    private ImagesModel imagesModel;
    private boolean isUpdate = false;
    private boolean isUploaded = false;
    private TransferUtility transferUtility;
    private AmazonS3Client s3;
    private List<TransferObserver> observers;
    private String name = "";
    private LinearLayout ll_set_schedule;
    private Toolbar toolBar;
    private ImageView iv_arrow_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        setUpToolbar();

        context = UploadActivity.this;
        name = Constants.FILE_NAME + "_" + System.currentTimeMillis();
        mMerchant = SplashScreenActivity.mMerchant;
        mEmployee = SplashScreenActivity.mEmployee;
        s3 = Util.getS3Client(this);

        transferUtility = Util.getTransferUtility(this);


        mBundle = getIntent().getExtras();
        if (mBundle != null) {
            String strImageModel = mBundle.getString(Constants.IMAGE_MODEL);
            isUpdate = mBundle.getBoolean(Constants.IS_UPDATE);
            imagesModel = new Gson().fromJson(strImageModel, ImagesModel.class);
        }
        dbAdapter = new DBAdapter(context);
        destFile = new File(Environment.getExternalStorageDirectory() + "/" + mMerchant.getId());
        if (!destFile.exists()) {
            destFile.mkdir();
        }
        imageView = (ImageView) findViewById(R.id.imageView);
        tv_choose_image = (TextView) findViewById(R.id.tv_choose_image);
        textRemoteViewLocation = (TextView) findViewById(R.id.textRemoteViewLocation);

        editTextAutoStartDate = (EditText) findViewById(R.id.editTextAutoStartDate);
        editTextAutoStartTime = (EditText) findViewById(R.id.editTextAutoStartTime);
        editTextAutoEndDate = (EditText) findViewById(R.id.editTextAutoEndDate);
        editTextAutoEndTime = (EditText) findViewById(R.id.editTextAutoEndTime);

        spinnerGlobalAction = (Spinner) findViewById(R.id.spinnerGlobalAction);
        switchAutoDeploy = (Switch) findViewById(R.id.switchAutoDeploy);
        ll_set_schedule = (LinearLayout) findViewById(R.id.ll_set_schedule);
        switchAutoDeployAction = (Switch) findViewById(R.id.switchAutoDeployAction);
        btn_upload = (Button) findViewById(R.id.btn_upload);

        switchAutoDeploy.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ll_set_schedule.setVisibility(View.VISIBLE);
                } else {
                    ll_set_schedule.setVisibility(View.GONE);
                }
            }
        });

        if (isUpdate) {
//            btn_upload.setText("Update");
            File srcFile1 = new File(Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id() + "/" + imagesModel.getImage_name());
            Bitmap bitmap = decodeFileLocal(srcFile1, false);

            /*BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            options.inSampleSize = 8;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id() + "/" + imagesModel.getImage_name());
            } catch (OutOfMemoryError e) {
                try {
                    bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
                } catch (OutOfMemoryError e1) {
                    options.inSampleSize = 16;
                    bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
                }
            }*/

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            }
            if (imagesModel.getIs_schedule() == 0) {
                switchAutoDeploy.setChecked(true);
            } else {
                switchAutoDeploy.setChecked(false);
            }
            if (imagesModel.getAuto_start_date_time() > 0) {

                editTextAutoStartDate.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_start_date_time(), "dd/MM/yyyy"));
                editTextAutoStartTime.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_start_date_time(), "HH:mm"));
            }
            if (imagesModel.getAuto_end_date_time() > 0) {
                editTextAutoEndDate.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_end_date_time(), "dd/MM/yyyy"));
                editTextAutoEndTime.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_end_date_time(), "HH:mm"));
            }
            textRemoteViewLocation.setText(Constants.BUCKET_NAME + "/" + imagesModel.getRemote_location());
            srcFile = new File(Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id() + "/" + imagesModel.getImage_name());

        } else {
//            btn_upload.setText("Upload");
            textRemoteViewLocation.setText(Constants.BUCKET_NAME + "/" + mMerchant.getId() + "/");
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 500);
//                    startActivityForResult(new avtImageChooser().create(), 500);
                  /*  Intent intent = new Intent(UploadActivity.this, WebViewActivity.class);
                    startActivity(intent);*/
                } catch (Exception e) {
                    Common.showToast(context, "" + e.toString());
                }
            }
        });
        tv_choose_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 500);
//                    startActivityForResult(new avtImageChooser().create(), 500);
                  /*  Intent intent = new Intent(UploadActivity.this, WebViewActivity.class);
                    startActivity(intent);*/
                } catch (Exception e) {
                    Common.showToast(context, "" + e.toString());
                }
            }
        });

        editTextAutoStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDateSet = false;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(System.currentTimeMillis());
                if (isUpdate && imagesModel.getAuto_start_date_time() > 0) {
                    c.setTimeInMillis(imagesModel.getAuto_start_date_time());
                }
                if (!editTextAutoStartDate.getText().toString().equalsIgnoreCase("")) {
                    c.setTimeInMillis(Common.getDateTimeStamp("dd/MM/yyyy", editTextAutoStartDate.getText().toString()));
                }
                DatePickerDialog datePicker = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (isDateSet) {
                            String startDate = Common.getDigits(dayOfMonth) + "/" + Common.getDigits(monthOfYear + 1) + "/" + year;
                            if (editTextAutoEndDate != null && !editTextAutoEndDate.getText().toString().equalsIgnoreCase("")) {
                                long startDateTimeStamp = Common.getDateTimeStamp("dd/MM/yyyy", startDate);
                                long endDateTimeStamp = Common.getDateTimeStamp("dd/MM/yyyy", editTextAutoEndDate.getText().toString());
                                if (startDateTimeStamp > endDateTimeStamp) {
                                    Common.showToast(context, "The start date must be less than the end date.");
                                    editTextAutoStartDate.setText("");
                                } else {
                                    editTextAutoStartDate.setText(startDate);
                                }

                            } else {
                                editTextAutoStartDate.setText(startDate);
                            }

                        }
                    }

                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

                datePicker.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = true;
                    }
                });
                datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = false;
                    }
                });
                datePicker.show();
            }
        });
        editTextAutoStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDateSet = false;
                Calendar c = Calendar.getInstance();
                if (isUpdate && imagesModel.getAuto_start_date_time() > 0) {
                    c.setTimeInMillis(imagesModel.getAuto_start_date_time());
                }
                if (!editTextAutoStartTime.getText().toString().equalsIgnoreCase("")) {
                    c.setTimeInMillis(Common.getDateTimeStamp("HH:mm", editTextAutoStartTime.getText().toString()));
                }
                TimePickerDialog timePickerDialog = new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (isDateSet) {
                            editTextAutoStartTime.setText(Common.getDigits(hourOfDay) + ":" + Common.getDigits(minute));
                        }
                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);

                timePickerDialog.setCancelable(false);
                timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = true;
                    }
                });
                timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = false;
                    }
                });
                timePickerDialog.show();
            }
        });
        editTextAutoEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDateSet = false;
                Calendar c = Calendar.getInstance();
                if (isUpdate && imagesModel.getAuto_end_date_time() > 0) {
                    c.setTimeInMillis(imagesModel.getAuto_end_date_time());
                }
                if (!editTextAutoEndDate.getText().toString().equalsIgnoreCase("")) {
                    c.setTimeInMillis(Common.getDateTimeStamp("dd/MM/yyyy", editTextAutoEndDate.getText().toString()));
                }
                DatePickerDialog datePicker = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (isDateSet) {
                            String endDate = Common.getDigits(dayOfMonth) + "/" + Common.getDigits(monthOfYear + 1) + "/" + year;
                            if (editTextAutoStartDate != null && !editTextAutoStartDate.getText().toString().equalsIgnoreCase("")) {
                                long startDateTimeStamp = Common.getDateTimeStamp("dd/MM/yyyy", editTextAutoStartDate.getText().toString());
                                long endDateTimeStamp = Common.getDateTimeStamp("dd/MM/yyyy", endDate);
                                if (endDateTimeStamp < startDateTimeStamp) {
                                    Common.showToast(context, "The end date must be greater than the start date.");
                                    editTextAutoEndDate.setText("");
                                } else {
                                    editTextAutoEndDate.setText(endDate);
                                }

                            } else {
                                editTextAutoEndDate.setText(endDate);
                            }
                        }
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                datePicker.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = true;
                    }
                });
                datePicker.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = false;
                    }
                });
                datePicker.show();
            }
        });
        editTextAutoEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDateSet = false;
                Calendar c = Calendar.getInstance();
                if (isUpdate && imagesModel.getAuto_end_date_time() > 0) {
                    c.setTimeInMillis(imagesModel.getAuto_end_date_time());
                }
                if (!editTextAutoEndTime.getText().toString().equalsIgnoreCase("")) {
                    c.setTimeInMillis(Common.getDateTimeStamp("HH:mm", editTextAutoEndTime.getText().toString()));
                }
                final TimePickerDialog timePickerDialog = new TimePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        if (isDateSet) {
                            editTextAutoEndTime.setText(Common.getDigits(hourOfDay) + ":" + Common.getDigits(minute));
                        }

                    }
                }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true);
                timePickerDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = true;

                    }
                });
                timePickerDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isDateSet = false;
                    }
                });


                timePickerDialog.show();
            }
        });

        btn_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadNewImage();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void uploadNewImage() {
        if (srcFile == null || (srcFile != null && srcFile.getAbsolutePath().equalsIgnoreCase(""))) {
            Common.showToast(context, "Please select image");
        } else if (editTextAutoStartDate.getText().toString() != null && editTextAutoStartDate.getText().toString().equalsIgnoreCase("") && switchAutoDeploy.isChecked()) {
            Common.showToast(context, "Please select start date");

        } else if (editTextAutoStartTime.getText().toString() != null && editTextAutoStartTime.getText().toString().equalsIgnoreCase("") && switchAutoDeploy.isChecked()) {
            Common.showToast(context, "Please select start time");
        } else if (editTextAutoEndDate.getText().toString() != null && editTextAutoEndDate.getText().toString().equalsIgnoreCase("") && switchAutoDeploy.isChecked()) {
            Common.showToast(context, "Please select end date");

        } else if (editTextAutoEndTime.getText().toString() != null && editTextAutoEndTime.getText().toString().equalsIgnoreCase("") && switchAutoDeploy.isChecked()) {
            Common.showToast(context, "Please select end time");
        } else {
            String startDateTime = editTextAutoStartDate.getText().toString() + " " + editTextAutoStartTime.getText().toString();
            String endDateTime = editTextAutoEndDate.getText().toString() + " " + editTextAutoEndTime.getText().toString();

            long startDateTimeTimestamp = 0;
            long endDateTimeTimestamp = 0;

            if (editTextAutoStartDate.getText().toString() != null && editTextAutoStartDate.getText().toString().equalsIgnoreCase("") && editTextAutoStartTime.getText().toString() != null && editTextAutoStartTime.getText().toString().equalsIgnoreCase("")) {
                startDateTimeTimestamp = 0;

            } else {
                startDateTimeTimestamp = Common.getDateTimeStamp("dd/MM/yyyy HH:mm", startDateTime);
            }
            if (editTextAutoEndDate.getText().toString() != null && editTextAutoEndDate.getText().toString().equalsIgnoreCase("") && editTextAutoEndTime.getText().toString() != null && editTextAutoEndTime.getText().toString().equalsIgnoreCase("")) {
                endDateTimeTimestamp = 0;
            } else {
                endDateTimeTimestamp = Common.getDateTimeStamp("dd/MM/yyyy HH:mm", endDateTime);
            }

            String imageFileName = "";
            if (isUpdate && isUploaded) {
                String[] str = srcFile.getName().split("\\.");
                String extension = str[str.length - 1];

                imageFileName = name + "." + extension;
            } else if (isUpdate && !isUploaded) {
                imageFileName = srcFile.getName();
            } else {
                String[] str = srcFile.getName().split("\\.");
                String extension = str[str.length - 1];

                imageFileName = name + "." + extension;
            }
            String remoteLocation = mMerchant.getId() + "/" + imageFileName;
            String localLocation = destFile.getAbsolutePath() + "/" + imageFileName;
            int is_schedule = switchAutoDeploy.isChecked() ? 0 : 1;
            int is_enable = 0;
            if (imagesModel != null) {
                is_enable = imagesModel.getIs_enable();
            }

            destFile = new File(destFile, imageFileName);
            if (isUpdate) {
                ImagesModel imagesModel = new ImagesModel();
                imagesModel.setId(this.imagesModel.getId());
                imagesModel.setMerchant_id(mMerchant.getId());
                imagesModel.setImage_name(imageFileName);
                imagesModel.setLocal_location(localLocation);
                imagesModel.setRemote_location(remoteLocation);
                imagesModel.setIs_enable(is_enable);
                imagesModel.setIs_schedule(is_schedule);
                imagesModel.setAuto_start_date_time(startDateTimeTimestamp);
                imagesModel.setAuto_end_date_time(endDateTimeTimestamp);
                dbAdapter.open();
                boolean b = dbAdapter.updateImageDetails(imagesModel);
                dbAdapter.close();
                if (b) {
                    if (srcFile.getName().equalsIgnoreCase(this.imagesModel.getImage_name())) {

                        generateCSV();
                    } else {
                        File file = new File(Environment.getExternalStorageDirectory() + "/" + this.imagesModel.getMerchant_id() + "/" + this.imagesModel.getImage_name());
                        if (file.exists()) {
                            file.delete();
                        }

                        new CopyFile(srcFile.getAbsolutePath(), imageFileName, destFile.getParent()).execute();


                        new DeleteOldFileFromServer().execute();
                    }


                }
            } else {
                dbAdapter.open();
                int id = (int) dbAdapter.insertImageDetails(mMerchant.getId(), imageFileName, remoteLocation, localLocation, is_enable, is_schedule, startDateTimeTimestamp, endDateTimeTimestamp);
                dbAdapter.close();
                if (id > 0) {

//                    copyFile(srcPath, fileName, destPath);
                    new CopyFile(srcFile.getAbsolutePath(), imageFileName, destFile.getParent()).execute();


                }
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && data != null) {
            srcFile = new File(getPath(data.getData()));
            Bitmap bitmap = decodeFileLocal(srcFile, true);

            /*isUploaded = true;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            options.inSampleSize = 8;
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath());
            } catch (OutOfMemoryError e) {
                try {
                    bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
                } catch (OutOfMemoryError e1) {
                    options.inSampleSize = 16;
                    try {
                        bitmap = BitmapFactory.decodeFile(srcFile.getAbsolutePath(), options);
                    } catch (OutOfMemoryError e2) {

                    }
                }
            }*/
            if (bitmap != null) {
                isUploaded = true;
                imageView.setImageBitmap(bitmap);
//                btn_upload.setText("Add Image");
            } else {
                if (isUpdate) {
                    File srcFile1 = new File(Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id() + "/" + imagesModel.getImage_name());
                    Bitmap bitmap1 = decodeFileLocal(srcFile1, false);
                    if (bitmap1 != null) {
                        imageView.setImageBitmap(bitmap1);
                    }
                    textRemoteViewLocation.setText(Constants.BUCKET_NAME + "/" + imagesModel.getRemote_location());
                    srcFile = new File(Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id() + "/" + imagesModel.getImage_name());
                } else {
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image));
                    srcFile = null;
                }
            }
            if (srcFile != null) {
                textRemoteViewLocation.setText(Constants.BUCKET_NAME + "/" + mMerchant.getId() + "/" + name + "." + srcFile.getName().substring(srcFile.getName().lastIndexOf(".")));
            }

        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.MediaColumns.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);

        cursor.moveToFirst();
        String imagePath = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));


        return imagePath;
    }

    private void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File(outputPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath);
            out = new FileOutputStream(outputPath + "/" + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
//            new File(inputPath + inputFile).delete();


        } catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        } catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    private void uploadImageInToServer() {
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + "/" + mMerchant.getId(), destFile.getName(), destFile);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                Log.d("", "");
                if (state.name().equalsIgnoreCase("COMPLETED")) {
                    generateCSV();
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

    private class DeleteOldFileFromServer extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            s3.deleteObject(Constants.BUCKET_NAME, imagesModel.getRemote_location());
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }


    }

    private void generateCSV() {

        Common.showProgressDialog(context);
        String fileName = Environment.getExternalStorageDirectory() + "/" + mMerchant.getId() + "/" + mMerchant.getId() + ".csv";

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
        File file = new File(Environment.getExternalStorageDirectory() + "/" + mMerchant.getId() + "/" + mMerchant.getId() + ".csv");
        TransferObserver observer = transferUtility.upload(Constants.BUCKET_NAME + "/" + mMerchant.getId(), file.getName(), file);
        observer.setTransferListener(new TransferListener() {
            @Override
            public void onStateChanged(int id, TransferState state) {
                if (state.name().equalsIgnoreCase("COMPLETED")) {
                    Common.dismissProgressDialog();

                    if (imagesModel != null && srcFile.getName().equalsIgnoreCase(imagesModel.getImage_name())) {
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
                            jsonObject.put(Constants.IMAGE, mMerchant.getId() + "/" + destFile.getName());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        new SendPushNotification(context, Constants.EVENT_UPLOAD_IMAGE, jsonObject.toString()).execute();
                    }
                    Common.showToast(context, "Data Uploaded!");
                    startActivity(new Intent(context, ListImagesActivity.class));
                    finish();
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

    private class CopyFile extends AsyncTask<String, String, String> {

        private final String srcPath;
        private final String fileName;
        private final String destPath;

        CopyFile(String srcPath, String fileName, String destPath) {

            this.srcPath = srcPath;
            this.fileName = fileName;
            this.destPath = destPath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Common.showProgressDialog(context);
        }

        @Override
        protected String doInBackground(String... params) {
            copyFile(srcPath, fileName, destPath);
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            uploadImageInToServer();
        }
    }

    public Bitmap scaleBitmap(Bitmap bitmap) {
        int newWidth = 1280;
        int newHeight = 800;
        int origWidth = bitmap.getWidth();
        int origHeight = bitmap.getHeight();

        // If no new width or height were specified return the original bitmap
        if (newWidth <= 0 && newHeight <= 0) {
            return bitmap;
        }
        // Only the width was specified
        else if (newWidth > 0 && newHeight <= 0) {
            newHeight = (newWidth * origHeight) / origWidth;
        }
        // only the height was specified
        else if (newWidth <= 0 && newHeight > 0) {
            newWidth = (newHeight * origWidth) / origHeight;
        }
        // If the user specified both a positive width and height
        // (potentially different aspect ratio) then the width or height is
        // scaled so that the image fits while maintaining aspect ratio.
        // Alternatively, the specified width and height could have been
        // kept and Bitmap.SCALE_TO_FIT specified when scaling, but this
        // would result in whitespace in the new image.
        else {
            double newRatio = newWidth / (double) newHeight;
            double origRatio = origWidth / (double) origHeight;

            if (origRatio > newRatio) {
                newHeight = (newWidth * origHeight) / origWidth;
            } else if (origRatio < newRatio) {
                newWidth = (newHeight * origWidth) / origHeight;
            }
        }

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFileLocal(File f, boolean flag) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            int w = o.outWidth;
            int h = o.outHeight;
            if ((w < 1280 && h < 800) || (w > 1280 && h > 800) && flag) {

                Common.showToast(context, "Image size must be atleast 1280 X 800");
                return null;
            }
            // The new size we want to scale to
            final int REQUIRED_SIZE = 1280;

            // Find the correct scale value. It should be the power of 2.
            /*int scale = 1;
            while (o.outWidth / scale / 2 >= 1280 ||
                    o.outHeight / scale / 2 >= 800) {
                scale *= 2;
            }
*/
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = 1;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            o2.inJustDecodeBounds = false;
            o2.inDither = true;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    private void setUpToolbar() {
        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        iv_arrow_back = (ImageView) toolBar.findViewById(R.id.iv_arrow_back);
        iv_arrow_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadActivity.super.onBackPressed();
            }
        });

    }
}
