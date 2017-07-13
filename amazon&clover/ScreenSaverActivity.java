package com.screensaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.clover.sdk.util.CustomerMode;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v3.customers.Customer;
import com.clover.sdk.v3.employees.Employee;
import com.screensaver.listener.OnServerDataChange;
import com.screensaver.model.AmazonS3UpdateResponseModel;
import com.screensaver.model.ImagesModel;
import com.screensaver.util.CustomDialog;
import com.screensaver.util.DBAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenSaverActivity extends Activity implements OnServerDataChange {
    public static Context context;
    private ImageSwitcher imageSwitcher;
    private Timer imageTimer;
    private int curIndexTravelDestination;
    private int totImagesInTravelDestination;
    private Merchant mMerchant;
    private Employee mEmployee;
    private DBAdapter dbAdapter;
    private Cursor cursor;
    private List<ImagesModel> imagesModelList;
    private long screenTimeout, imageInterval;
    private int status;
    private CustomDialog customDialog;

    Handler h = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                imagesModelList.remove(msg.arg1);
                if (imagesModelList.size()==1){
                    if (imageTimer != null) {
                        imageTimer.cancel();
                        imageTimer = null;
                    }
                    curIndexTravelDestination = 0;
                    setImageSwitcherDestination(curIndexTravelDestination);
                }
            } else {
                Log.d("", "");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        context = ScreenSaverActivity.this;

        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    CustomerMode.enable(context);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute();


        dbAdapter = new DBAdapter(context);
        mMerchant = SplashScreenActivity.mMerchant;
        mEmployee = SplashScreenActivity.mEmployee;
        imagesModelList = new ArrayList<>();

        dbAdapter.open();
        Cursor c = dbAdapter.getScreenSaverBasedOnMerchantId(mMerchant.getId());
        if (c.moveToFirst()) {
            screenTimeout = c.getLong(c.getColumnIndex(DBAdapter.TIMEOUT));
            imageInterval = c.getLong(c.getColumnIndex(DBAdapter.INTERVAL));
            status = c.getInt(c.getColumnIndex(DBAdapter.STATUS));

        }
        dbAdapter.close();
        if (status == 0) {
            setScreenSaver();
        } else {
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            customDialog.setOnPositiveClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    finish();
                }
            });
            customDialog.show();

        }

    }

    private void setScreenSaver() {
        dbAdapter.open();
        cursor = dbAdapter.getImagesDetailsBasedOnNewConditions(mMerchant.getId(), 0);
        long timeStamp = System.currentTimeMillis();
        while (cursor.moveToNext()) {
            ImagesModel imagesModel = new ImagesModel();
            imagesModel.setCursorToModel(cursor);
            if (imagesModel.getIs_schedule() == 0) {
                if (imagesModel.getAuto_start_date_time() <= timeStamp && imagesModel.getAuto_end_date_time() >= timeStamp) {
                    imagesModelList.add(imagesModel);
                } else {

                }
            } else {
                imagesModelList.add(imagesModel);
            }


        }

        dbAdapter.close();


        totImagesInTravelDestination = imagesModelList.size();

        imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);
        Animation in = AnimationUtils.loadAnimation(context, R.anim.slide_in_right_imgswitcher);
        Animation out = AnimationUtils.loadAnimation(context, R.anim.slide_out_left_imgswitcher);
        imageSwitcher.setInAnimation(in);
        imageSwitcher.setOutAnimation(out);
        imageSwitcher.setFactory(new ViewSwitcher.ViewFactory() {
            public View makeView() {
                ImageView myView = new ImageView(context);
                myView.setLayoutParams(new ImageSwitcher.LayoutParams(
                        ActionBar.LayoutParams.FILL_PARENT, ActionBar.LayoutParams.FILL_PARENT
                ));
                myView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                return myView;
            }
        });
        if (totImagesInTravelDestination == 1) {

            setImageSwitcherDestination(curIndexTravelDestination);
        } else {

            travelDestinationSwitchImage();
        }
    }

    public void travelDestinationSwitchImage() {
        if (imageTimer != null) {
            imageTimer.cancel();
            imageTimer = null;
        }
        imageTimer = new Timer();
        //Set the schedule function and rate
        imageTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                //Called each time when 1000 milliseconds (1 second) (the period parameter)
                curIndexTravelDestination++;
                // If index reaches maximum reset it
                if (curIndexTravelDestination >= totImagesInTravelDestination)
                    curIndexTravelDestination = 0;
                ((ScreenSaverActivity) context).runOnUiThread(new Runnable() {

                    public void run() {
//                        is_destination.setImageResource(imageIds[currentIndex]);
                        setImageSwitcherDestination(curIndexTravelDestination);

                    }
                });
            }

        }, 0, imageInterval * 1000);
    }

    public void setImageSwitcherDestination(int index) {
        try {
            if (System.currentTimeMillis() > imagesModelList.get(index).getAuto_end_date_time() && imagesModelList.get(index).getIs_schedule() == 0) {
                Message message = new Message();
                message.what = 1;
                message.arg1 = index;
                h.dispatchMessage(message);
            }
            String strPath = imagesModelList.get(index).getLocal_location();

            if (strPath != null && !strPath.equalsIgnoreCase("no data found")) {
                Bitmap bitmap = BitmapFactory.decodeFile(strPath);

                final Drawable d = new BitmapDrawable(getResources(), bitmap);

                imageSwitcher.setImageDrawable(d);
            } else {
                imageSwitcher.setImageResource(R.mipmap.ic_launcher);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public void go(View view) {
        startActivity(new Intent(context, SettingsActivity.class));
    }

    @Override
    public void onDataChange(AmazonS3UpdateResponseModel amazonS3UpdateResponseModel) {
        if (imagesModelList != null) {
            imagesModelList.clear();
        }

        dbAdapter.open();
        Cursor c = dbAdapter.getScreenSaverBasedOnMerchantId(mMerchant.getId());
        if (c.moveToFirst()) {
            screenTimeout = c.getLong(c.getColumnIndex(DBAdapter.TIMEOUT));
            imageInterval = c.getLong(c.getColumnIndex(DBAdapter.INTERVAL));
            status = c.getInt(c.getColumnIndex(DBAdapter.STATUS));

        }
        dbAdapter.close();
        if (status == 0) {

            dbAdapter.open();
            cursor = dbAdapter.getImagesDetailsBasedOnNewConditions(mMerchant.getId(), 0);
            while (cursor.moveToNext()) {
                ImagesModel imagesModel = new ImagesModel();
                imagesModel.setCursorToModel(cursor);
                long timeStamp = System.currentTimeMillis();
                if (imagesModel.getIs_schedule() == 0) {
                    if (imagesModel.getAuto_start_date_time() <= timeStamp && imagesModel.getAuto_end_date_time() >= timeStamp) {
                        imagesModelList.add(imagesModel);
                    } else {

                    }
                } else {
                    imagesModelList.add(imagesModel);
                }

            }

            dbAdapter.close();


            totImagesInTravelDestination = imagesModelList.size();
            if (totImagesInTravelDestination == 1) {
                if (imageTimer != null) {
                    imageTimer.cancel();
                    imageTimer = null;
                }

                curIndexTravelDestination = 0;
                setImageSwitcherDestination(curIndexTravelDestination);
            } else {
                curIndexTravelDestination = 0;
                travelDestinationSwitchImage();
            }

        } else {
            if (imageTimer != null) {
                imageTimer.cancel();
                imageTimer = null;
            }
            imageSwitcher.setImageDrawable(null);
            customDialog = new CustomDialog(context);
            customDialog.setCancelable(false);
            customDialog.setOnPositiveClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    customDialog.dismiss();
                    finish();
                }
            });
            customDialog.show();

        }


    }
}
