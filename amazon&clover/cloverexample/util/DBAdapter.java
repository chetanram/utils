package com.texasbrokers.screensaver.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.texasbrokers.screensaver.model.ImagesModel;

public class DBAdapter {


    private static final String DATABASE_NAME = "Global.db";
    private static final int DATABASE_VERSION = 1;

    //tables
    public static final String TBL_SCREEN_SAVER = "tbl_screen_saver";
    public static final String TBL_IMAGE_DETAILS = "tbl_image_details";


    //tbl_screen_saver constants
    public static final String _ID = "_id";
    public static final String MERCHANT_ID = "merchant_id";
    public static final String STATUS = "status";
    public static final String TIMEOUT = "timeout";
    public static final String INTERVAL = "interval";

    //tbl_image_details constants
    public static final String IMAGE_NAME = "image_name";
    public static final String REMOTE_LOCATION = "remote_location";
    public static final String LOCAL_LOCATION = "local_location";
    public static final String IMAGE_IS_ENABLE = "is_enable";
    public static final String IMAGE_IS_SCHEDULE = "is_schedule";
    public static final String AUTO_START_DATE_TIME = "auto_start_date_time";
    public static final String AUTO_END_DATE_TIME = "auto_end_date_time";


    //create table tbl_screen_saver
    private static final String CREATE_TBL_SCREEN_SAVER = "CREATE TABLE " + TBL_SCREEN_SAVER +
            " (" + _ID + " integer primary key AUTOINCREMENT,"
            + MERCHANT_ID + " text,"
            + STATUS + " integer,"
            + TIMEOUT + " long,"
            + INTERVAL + " long)";
    //create table tbl_image_details
    private static final String CREATE_TBL_IMAGE_DETAILS = "CREATE TABLE " + TBL_IMAGE_DETAILS +
            " (" + _ID + " integer primary key AUTOINCREMENT,"
            + MERCHANT_ID + " text,"
            + IMAGE_NAME + " text,"
            + REMOTE_LOCATION + " text,"
            + LOCAL_LOCATION + " text,"
            + IMAGE_IS_ENABLE + " integer,"
            + IMAGE_IS_SCHEDULE + " integer,"
            + AUTO_START_DATE_TIME + " long,"
            + AUTO_END_DATE_TIME + " long)";


    private final Context mContext;
    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;


    public DBAdapter(Context ctx) {

        this.mContext = ctx;
       /* mSharedPreferences = ctx.getSharedPreferences(Constants.SharedPref.PLANITSYNCIT_SP, Context.MODE_PRIVATE);
        try {
            user_id = Integer.parseInt(mSharedPreferences.getString(Constants.SharedPref.USERID_SP, ""));
        } catch (Exception e) {

        }*/
        DBHelper = new DatabaseHelper(mContext);
    }

    // ---opens the database---
    public DBAdapter open() throws SQLException {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    // ---closes the database---
    public void close() {
        DBHelper.close();
    }


    public Cursor getScreenSaver() {
        String sql = "SELECT * FROM " + TBL_SCREEN_SAVER;
        return db.rawQuery(sql, null);
    }

    public boolean isScreenSaverDataAvailable(String merchant_id) {
        String sql = "SELECT * FROM " + TBL_SCREEN_SAVER + " where " + MERCHANT_ID + "='" + merchant_id + "'";
        Cursor c = db.rawQuery(sql, null);
        return c.getCount() > 0 ? true : false;
    }

    public boolean isImageDataAvailable(String fileName) {
        String sql = "SELECT * FROM " + TBL_IMAGE_DETAILS + " where " + IMAGE_NAME + "='" + fileName + "'";
        Cursor c = db.rawQuery(sql, null);
        return c.getCount() > 0 ? true : false;
    }

    public Cursor getScreenSaverBasedOnMerchantId(String merchant_id) {
        String sql = "SELECT * FROM " + TBL_SCREEN_SAVER + " where " + MERCHANT_ID + "='" + merchant_id + "'";
        return db.rawQuery(sql, null);
    }

    public Cursor getImagesDetails(String merchant_id) {
        String sql = "SELECT * FROM " + TBL_IMAGE_DETAILS + " where " + MERCHANT_ID + "='" + merchant_id + "'";
        return db.rawQuery(sql, null);
    }

    public Cursor getImagesDetailsNotInImageName(String imageNames, String merchant_id) {
        String sql = "SELECT * FROM " + TBL_IMAGE_DETAILS + " where " + MERCHANT_ID + "='" + merchant_id + "' and " + IMAGE_NAME + " NOT IN(" + imageNames + ")";
        return db.rawQuery(sql, null);
    }

    public Cursor getImagesDetailsBasedOnConditions(String merchant_id, int auto_deploy, long currentTimestamp) {
        String sql = "SELECT * FROM " + TBL_IMAGE_DETAILS + " where " + MERCHANT_ID + "='" + merchant_id + "' and " + IMAGE_IS_ENABLE + "=" + auto_deploy + " and " + AUTO_START_DATE_TIME + " <= " + currentTimestamp + " and " + AUTO_END_DATE_TIME + " >= " + currentTimestamp;
        return db.rawQuery(sql, null);
    }

    public Cursor getImagesDetailsBasedOnNewConditions(String merchant_id, int is_enabled) {
        String sql = "SELECT * FROM " + TBL_IMAGE_DETAILS + " where " + MERCHANT_ID + "='" + merchant_id + "' and " + IMAGE_IS_ENABLE + "=" + is_enabled;
        return db.rawQuery(sql, null);
    }


    public long insertScreenSaverSettings(String merchant_id, int status, long timeout, long interval) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MERCHANT_ID, merchant_id);
        initialValues.put(STATUS, status);
        initialValues.put(TIMEOUT, timeout);
        initialValues.put(INTERVAL, interval);
        return db.insert(TBL_SCREEN_SAVER, null, initialValues);

    }

    public long insertImageDetails(String merchant_id, String imageName, String remoteLocation, String localLocation, int is_enable, int is_schedule, long startDateTimeTimestamp, long endDateTimeTimestamp) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MERCHANT_ID, merchant_id);
        initialValues.put(IMAGE_NAME, imageName);
        initialValues.put(REMOTE_LOCATION, remoteLocation);
        initialValues.put(LOCAL_LOCATION, localLocation);
        initialValues.put(IMAGE_IS_ENABLE, is_enable);
        initialValues.put(IMAGE_IS_SCHEDULE, is_schedule);
        initialValues.put(AUTO_START_DATE_TIME, startDateTimeTimestamp);
        initialValues.put(AUTO_END_DATE_TIME, endDateTimeTimestamp);
        return db.insert(TBL_IMAGE_DETAILS, null, initialValues);

    }

    public void dropLotteryTable() {
        db.execSQL(CREATE_TBL_SCREEN_SAVER);
    }


    public boolean updateScreenSaver(String merchant_id, int id, int status, long timeout, long interval) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(STATUS, status);
        initialValues.put(TIMEOUT, timeout);
        initialValues.put(INTERVAL, interval);
        return db.update(TBL_SCREEN_SAVER, initialValues, MERCHANT_ID + "=?", new String[]{merchant_id}) > 0;
    }

    public boolean insertUpdateScreenSaver(int id, String merchant_id, int status, long timeout, long interval) {


        ContentValues initialValues = new ContentValues();
        initialValues.put(MERCHANT_ID, merchant_id);
        initialValues.put(STATUS, status);
        initialValues.put(TIMEOUT, timeout);
        initialValues.put(INTERVAL, interval);
        if (isScreenSaverDataAvailable(merchant_id)) {
            return db.update(TBL_SCREEN_SAVER, initialValues, MERCHANT_ID + "=?", new String[]{merchant_id}) > 0;
        } else {
            int b = (int) db.insert(TBL_SCREEN_SAVER, null, initialValues);
            return b > 0 ? true : false;
        }


    }

    public boolean updateImageDetails(ImagesModel imagesModel) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(MERCHANT_ID, imagesModel.getMerchant_id());
        initialValues.put(IMAGE_NAME, imagesModel.getImage_name());
        initialValues.put(REMOTE_LOCATION, imagesModel.getRemote_location());
        initialValues.put(LOCAL_LOCATION, imagesModel.getLocal_location());
        initialValues.put(IMAGE_IS_ENABLE, imagesModel.getIs_enable());
        initialValues.put(IMAGE_IS_SCHEDULE, imagesModel.getIs_schedule());
        initialValues.put(AUTO_START_DATE_TIME, imagesModel.getAuto_start_date_time());
        initialValues.put(AUTO_END_DATE_TIME, imagesModel.getAuto_end_date_time());
        return db.update(TBL_IMAGE_DETAILS, initialValues, _ID + "=" + imagesModel.getId(), null) > 0;
    }

    public boolean insertUpdateImageDetails(ImagesModel imagesModel) {


        ContentValues initialValues = new ContentValues();
        initialValues.put(MERCHANT_ID, imagesModel.getMerchant_id());
        initialValues.put(IMAGE_NAME, imagesModel.getImage_name());
        initialValues.put(REMOTE_LOCATION, imagesModel.getRemote_location());
        initialValues.put(LOCAL_LOCATION, imagesModel.getLocal_location());
        initialValues.put(IMAGE_IS_ENABLE, imagesModel.getIs_enable());
        initialValues.put(IMAGE_IS_SCHEDULE, imagesModel.getIs_schedule());
        initialValues.put(AUTO_START_DATE_TIME, imagesModel.getAuto_start_date_time());
        initialValues.put(AUTO_END_DATE_TIME, imagesModel.getAuto_end_date_time());

        if (isImageDataAvailable(imagesModel.getImage_name())) {
            return db.update(TBL_IMAGE_DETAILS, initialValues, IMAGE_NAME + "=?", new String[]{imagesModel.getImage_name()}) > 0;
        } else {
            int b = (int) db.insert(TBL_IMAGE_DETAILS, null, initialValues);
            return b > 0 ? true : false;
        }


    }


    public void deleteScreenSaver(int id) {

        db.delete(TBL_SCREEN_SAVER, _ID + " = ?", new String[]{"" + id});

    }

    public void deleteAllScreenSaver(String merchant_id) {

        db.delete(TBL_SCREEN_SAVER, MERCHANT_ID + " = ?", new String[]{merchant_id});

    }

    public int deleteImageDetails(int id) {

        return db.delete(TBL_IMAGE_DETAILS, _ID + " = ?", new String[]{"" + id});

    }

    public int deleteImageDetailsBasedOnFileName(String fileName) {

        return db.delete(TBL_IMAGE_DETAILS, IMAGE_NAME + " = ?", new String[]{"" + fileName});

    }

    public void deleteImagesDetailsNotInImageName(String fileNames, String merchant_id) {

        String sql = "DELETE FROM " + TBL_IMAGE_DETAILS + " WHERE " + MERCHANT_ID + "='" + merchant_id + "' AND " + IMAGE_NAME + " NOT IN (" + fileNames + ")";
        db.rawQuery(sql, null).getCount();

    }

    public int deleteAllImageDetails(String merchant_id) {

        return db.delete(TBL_IMAGE_DETAILS, MERCHANT_ID + "=?", new String[]{merchant_id});

    }

    public boolean updateImageBasedOnImageName(String oldFileName, String currentFullPath, String newFileName) {
        ContentValues initialValues = new ContentValues();


        initialValues.put(LOCAL_LOCATION, currentFullPath);
        initialValues.put(IMAGE_NAME, newFileName);
        return db.update(TBL_IMAGE_DETAILS, initialValues, IMAGE_NAME + "='" + oldFileName + "'", null) > 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }


        @Override
        public void onCreate(SQLiteDatabase db) {
            // SQLiteDatabase.openOrCreateDatabase(DATABASE_NAME, null);
            //db.execSQL(DATABASE_CREATE);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_SCREEN_SAVER);
            db.execSQL("DROP TABLE IF EXISTS " + TBL_IMAGE_DETAILS);


            db.execSQL(CREATE_TBL_SCREEN_SAVER);
            db.execSQL(CREATE_TBL_IMAGE_DETAILS);
            Log.d("CREATE_TBL_SCREEN_SAVER", CREATE_TBL_SCREEN_SAVER);


        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("UPDATE", "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
//            db.execSQL("DROP TABLE IF EXISTS titles");

            db.execSQL(CREATE_TBL_SCREEN_SAVER);
            db.execSQL(CREATE_TBL_IMAGE_DETAILS);
            Log.v("CREATE_TBL_SCREEN_SAVER", CREATE_TBL_SCREEN_SAVER);


            onCreate(db);
        }
    }


}
