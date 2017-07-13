package com.screensaver;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.gson.Gson;
import com.screensaver.model.ImagesModel;
import com.screensaver.util.Common;
import com.screensaver.util.Constants;
import com.screensaver.util.DBAdapter;
import com.screensaver.util.Util;

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
    private TextView textRemoteViewLocation;
    private EditText editTextAutoStartDate, editTextAutoStartTime, editTextAutoEndDate, editTextAutoEndTime;
    private Spinner spinnerGlobalAction;
    private Switch switchAutoDeploy;
    private Switch switchAutoDeployAction;
    private Context context;
    private String srcPath = "", destPath = "";
    private File srcFile, destFile;

    private int d, m, y;
    private int hr, min;
    private boolean isDateSet = false;
    private Button btn_upload;
    private DBAdapter dbAdapter;
    private String merchant_id = "";
    private Merchant mMerchant;
    private Employee mEmployee;
    private Bundle mBundle;
    private ImagesModel imagesModel;
    private boolean isUpdate = false;
    private boolean isUploaded = false;
    private TransferUtility transferUtility;
    private AmazonS3Client s3;
    private List<TransferObserver> observers;

    private String fileName;
    private LinearLayout ll_set_schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        context = UploadActivity.this;
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
        destPath = destFile.getAbsolutePath();
        imageView = (ImageView) findViewById(R.id.imageView);
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
            btn_upload.setText("Update");
            imageView.setImageBitmap(BitmapFactory.decodeFile(imagesModel.getLocal_location()));
            if (imagesModel.getIs_schedule() == 0) {
                switchAutoDeploy.setChecked(true);
            } else {
                switchAutoDeploy.setChecked(false);
            }
            editTextAutoStartDate.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_start_date_time(), "dd/MM/yyyy"));
            editTextAutoStartTime.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_start_date_time(), "HH:mm"));

            editTextAutoEndDate.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_end_date_time(), "dd/MM/yyyy"));
            editTextAutoEndTime.setText(Common.getDateFromTimeStamp(imagesModel.getAuto_end_date_time(), "HH:mm"));
            textRemoteViewLocation.setText(Constants.BUCKET_NAME + "/" + imagesModel.getRemote_location());
            srcPath = imagesModel.getLocal_location();
            srcFile = new File(srcPath);

        } else {
            btn_upload.setText("Upload");
        }

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {


                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, 500);

                    //startActivityForResult(new avtImageChooser().create(), 500);
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
                if (isUpdate) {
                    c.setTimeInMillis(imagesModel.getAuto_start_date_time());
                }
                DatePickerDialog datePicker = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (isDateSet) {
                            editTextAutoStartDate.setText(Common.getDigits(dayOfMonth) + "/" + Common.getDigits(monthOfYear + 1) + "/" + year);
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
                if (isUpdate) {
                    c.setTimeInMillis(imagesModel.getAuto_start_date_time());
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
                if (isUpdate) {
                    c.setTimeInMillis(imagesModel.getAuto_end_date_time());
                }
                DatePickerDialog datePicker = new DatePickerDialog(context, AlertDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        if (isDateSet) {
                            editTextAutoEndDate.setText(Common.getDigits(dayOfMonth) + "/" + Common.getDigits(monthOfYear + 1) + "/" + year);
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
                if (isUpdate) {
                    c.setTimeInMillis(imagesModel.getAuto_end_date_time());
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

    private void uploadNewImage() {
        if (srcPath != null && srcPath.equalsIgnoreCase("")) {
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

            String fileName = "";
            if (isUpdate && isUploaded) {
                String[] str = srcFile.getName().split(".");
                String extension = str[str.length - 1];
                String name = Constants.FILE_NAME + "_" + System.currentTimeMillis();
                fileName = name + "." + extension;
            } else if (isUpdate && !isUploaded) {
                fileName = srcFile.getName();
            } else {
                String[] str = srcFile.getName().split("\\.");
                String extension = str[str.length - 1];
                String name = Constants.FILE_NAME + "_" + System.currentTimeMillis();
                fileName = name + "." + extension;
            }
            String remoteLocation = mMerchant.getId() + "/" + fileName;
            String localLocation = destFile.getAbsolutePath() + "/" + fileName;
            int is_schedule = switchAutoDeploy.isChecked() ? 0 : 1;
            int is_enable = imagesModel.getIs_enable();

            destFile = new File(destFile, fileName);
            if (isUpdate) {
                ImagesModel imagesModel = new ImagesModel();
                imagesModel.setId(this.imagesModel.getId());
                imagesModel.setMerchant_id(mMerchant.getId());
                imagesModel.setImage_name(fileName);
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
                        File file = new File(this.imagesModel.getLocal_location());
                        if (file.exists()) {
                            file.delete();
                        }

//                        copyFile(srcPath, fileName, destPath);


                        new DeleteOldFileFromServer().execute();
                    }


                }
            } else {
                dbAdapter.open();
                int id = (int) dbAdapter.insertImageDetails(mMerchant.getId(), fileName, remoteLocation, localLocation, is_enable, is_schedule, startDateTimeTimestamp, endDateTimeTimestamp);
                dbAdapter.close();
                if (id > 0) {

//                    copyFile(srcPath, fileName, destPath);
                    new CopyFile(srcPath, fileName, destPath).execute();


                }
            }

        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 500 && data != null) {
            srcPath = getPath(data.getData());
            srcFile = new File(srcPath);
            isUploaded = true;
            imageView.setImageBitmap(BitmapFactory.decodeFile(srcPath));
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
                Log.e("", "");
                if (state.name().equalsIgnoreCase("COMPLETED")) {
                    generateCSV();
                }
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
        fileName = srcFile.getParent() + "/" + mMerchant.getId() + ".csv";

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
                if (state.name().equalsIgnoreCase("COMPLETED")) {
                    Common.dismissProgressDialog();
                    Common.showToast(context, "Data Uploaded!");
                    startActivity(new Intent(context, ListImagesActivity.class));
                    finish();
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
}
