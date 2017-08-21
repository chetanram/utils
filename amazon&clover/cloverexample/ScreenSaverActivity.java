package com.texasbrokers.screensaver;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher;

import com.clover.sdk.util.CustomerMode;
import com.clover.sdk.v1.merchant.Merchant;
import com.clover.sdk.v3.employees.Employee;
import com.squareup.picasso.Picasso;
import com.texasbrokers.screensaver.R;
import com.texasbrokers.screensaver.listener.OnServerDataChange;
import com.texasbrokers.screensaver.model.AmazonS3UpdateResponseModel;
import com.texasbrokers.screensaver.model.ImagesModel;
import com.texasbrokers.screensaver.util.Common;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.CustomDialog;
import com.texasbrokers.screensaver.util.DBAdapter;
import com.texasbrokers.screensaver.util.PrefUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ScreenSaverActivity extends Activity implements OnServerDataChange, KeyguardManager.OnKeyguardExitResult {
    public static Context context;
    private ImageSwitcher imageSwitcher;
    private Timer imageTimer;
    private Timer newImageTimer;
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
    private KeyguardManager keyguardManager;
    private KeyguardManager.KeyguardLock lock;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);
        context = ScreenSaverActivity.this;
        customDialog = new CustomDialog(context);
        customDialog.setCancelable(false);
        customDialog.setTitle("Screen Saver");
        customDialog.setOnPositiveClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                finish();
            }
        });
        keyguardManager = ((KeyguardManager) getSystemService(Activity.KEYGUARD_SERVICE));
        ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        ((Activity) context).getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

//        lock = keyguardManager.newKeyguardLock("CustomLock");
//        keyguardManager.exitKeyguardSecurely(this);

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
//                myView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                myView.setScaleType(ImageView.ScaleType.FIT_XY);

                return myView;
            }
        });
        new AsyncTask<String, String, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    CustomerMode.enable(context);
                    /*getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_LOW_PROFILE
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);*/
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

        Cursor c1 = dbAdapter.getImagesDetailsBasedOnNewConditions(mMerchant.getId(), 0);

        if (status == 0 && c1.getCount() > 0) {
            setScreenSaver();
        } else if (status == 0) {
            if (customDialog != null) {
                imageSwitcher.setImageResource(R.drawable.no_image);
                customDialog.setMessege("There Is No Image!");
                customDialog.show();
            }

        } else {
            if (customDialog != null) {
                customDialog.setMessege("Screen Saver Disabled!");
                customDialog.show();
            }

        }
        dbAdapter.close();
        imageSwitcher.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return false;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, true);
        PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
        if (keyguardManager.isKeyguardSecure()) {

        } else {
            Common.showToast(context, "Please Setup Device Screen Lock Security");
        }
        if (!keyguardManager.isKeyguardLocked()) {
            lock = keyguardManager.newKeyguardLock("CustomLock");
            lock.disableKeyguard();
            keyguardManager.exitKeyguardSecurely(ScreenSaverActivity.this);
        }

    }

    /*  @Override
      protected void onStop() {
          super.onStop();
          PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
          PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
      }

      @Override
      protected void onDestroy() {
          super.onDestroy();
          PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
          PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
      }
  */
    @Override
    protected void onResume() {
        super.onResume();
        PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, true);
        PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
    }

    private void setScreenSaver() {

        if (imagesModelList != null) {
            imagesModelList.clear();
        }
        dbAdapter.open();
        Cursor cursor = dbAdapter.getImagesDetailsBasedOnNewConditions(mMerchant.getId(), 0);
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

        if (newImageTimer != null) {
            newImageTimer.cancel();
            newImageTimer = null;
        }
        if (totImagesInTravelDestination == 1) {
            if (imageTimer != null) {
                imageTimer.cancel();
                imageTimer = null;
            }

            curIndexTravelDestination = 0;
            setImageSwitcherDestination(curIndexTravelDestination);
            newImageTimer = new Timer();
            //Set the schedule function and rate
            newImageTimer.scheduleAtFixedRate(new TimerTask() {

                public void run() {

                    List<ImagesModel> list = getImageModelList();
                    if (list.size() != imagesModelList.size()) {
                        setScreenSaver();
                    }
                }

            }, 0, imageInterval * 1000);

        } else if (totImagesInTravelDestination > 1) {
            curIndexTravelDestination = 0;
            travelDestinationSwitchImage();
        } else {
            if (imageTimer != null) {
                imageTimer.cancel();
                imageTimer = null;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (customDialog != null && PrefUtils.getBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false)) {
                        imageSwitcher.setImageResource(R.drawable.no_image);
                        customDialog.setMessege("There Is No Image!");
                        customDialog.show();
                    }

                }
            });

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

            String strPath = Environment.getExternalStorageDirectory() + "/" + imagesModelList.get(index).getMerchant_id() + "/" + imagesModelList.get(index).getImage_name();

            if (strPath != null && !strPath.equalsIgnoreCase("no data found")) {
                File srcFile = new File(strPath);
                Bitmap bitmap = decodeFileLocal(srcFile);

               /* BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 8;
                options.inJustDecodeBounds = false;
                options.inPreferredConfig = Bitmap.Config.RGB_565;
                options.inDither = true;
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeFile(strPath);
                } catch (OutOfMemoryError e) {
                    try {
                        bitmap = BitmapFactory.decodeFile(strPath, options);
                    } catch (OutOfMemoryError e1) {
                        options.inSampleSize = 16;
                        try {

                            bitmap = BitmapFactory.decodeFile(strPath, options);
                        } catch (OutOfMemoryError e2) {

                        }
                    }
                }
*/

//                Uri uri = Uri.fromFile(new File(strPath));
                if (bitmap != null) {
                    final Drawable d = new BitmapDrawable(getResources(), bitmap);
                    imageSwitcher.setImageDrawable(d);
//                        bitmap.recycle();
//                    imageSwitcher.setImageURI(uri);


                }
            } else {
                imageSwitcher.setImageResource(R.drawable.ic_launcher);
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
        if (customDialog != null && customDialog.isShowing()) {
            customDialog.dismiss();
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
            setScreenSaver();

        } else {
            if (imageTimer != null) {
                imageTimer.cancel();
                imageTimer = null;
            }
            if (customDialog != null && PrefUtils.getBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false)) {
                customDialog.setMessege("Screen Saver Disabled!");
                customDialog.show();
            }
        }

    }

    private List<ImagesModel> getImageModelList() {
        List<ImagesModel> imagesModels = new ArrayList<>();
        dbAdapter.open();
        Cursor cursor = dbAdapter.getImagesDetailsBasedOnNewConditions(mMerchant.getId(), 0);
        while (cursor.moveToNext()) {
            ImagesModel imagesModel = new ImagesModel();
            imagesModel.setCursorToModel(cursor);
            long timeStamp = System.currentTimeMillis();
            if (imagesModel.getIs_schedule() == 0) {
                if (imagesModel.getAuto_start_date_time() <= timeStamp && imagesModel.getAuto_end_date_time() >= timeStamp) {
                    imagesModels.add(imagesModel);
                } else {

                }
            } else {
                imagesModels.add(imagesModel);
            }

        }

        dbAdapter.close();
        return imagesModels;

    }

    @Override
    public void onKeyguardExitResult(boolean success) {
        if (success) {
            PrefUtils.saveBoolean(context, Constants.PREF_IS_SCREEN_FOREGROUND, false);
            PrefUtils.saveLong(context, Constants.PREF_LONG_INACTIVE, System.currentTimeMillis());
            lock.reenableKeyguard();
            CustomerMode.disable(context);
            finish();
        } else {
            lock.reenableKeyguard();
            lock = keyguardManager.newKeyguardLock("CustomLock");
            lock.disableKeyguard();
            keyguardManager.exitKeyguardSecurely(ScreenSaverActivity.this);
        }
    }

    private Bitmap decodeFileLocal(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

           /* int w = o.outWidth;
            int h = o.outHeight;
            if (w < 1280 && h < 800) {

                Common.showToast(context, "Image size must be atleast 1280 X 800");
                return null;
            }
*/
            // The new size we want to scale to
            final int REQUIRED_SIZE = 1280;

            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= 1280 ||
                    o.outHeight / scale / 2 >= 800) {
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            o2.inPreferredConfig = Bitmap.Config.RGB_565;
            o2.inJustDecodeBounds = false;
            o2.inDither = true;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}
