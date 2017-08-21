package com.texasbrokers.screensaver.model;

import android.database.Cursor;

import com.texasbrokers.screensaver.util.DBAdapter;

/**
 * Created by chetan on 30/6/17.
 */

public class ImagesModel {
    public int id;


    public String merchant_id;
    public String image_name;
    public String remote_location;
    public String local_location;
    public int is_enable;
    public int is_schedule;
    public long auto_start_date_time;
    public long auto_end_date_time;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMerchant_id() {
        return merchant_id;
    }

    public void setMerchant_id(String merchant_id) {
        this.merchant_id = merchant_id;
    }

    public String getImage_name() {
        return image_name;
    }

    public void setImage_name(String image_name) {
        this.image_name = image_name;
    }

    public String getRemote_location() {
        return remote_location;
    }

    public void setRemote_location(String remote_location) {
        this.remote_location = remote_location;
    }

    public String getLocal_location() {
        return local_location;
    }

    public void setLocal_location(String local_location) {
        this.local_location = local_location;
    }

    public int getIs_enable() {
        return is_enable;
    }

    public int getIs_schedule() {
        return is_schedule;
    }

    public void setIs_schedule(int is_schedule) {
        this.is_schedule = is_schedule;
    }

    public void setIs_enable(int is_enable) {
        this.is_enable = is_enable;
    }

    public long getAuto_start_date_time() {
        return auto_start_date_time;
    }

    public void setAuto_start_date_time(long auto_start_date_time) {
        this.auto_start_date_time = auto_start_date_time;
    }

    public long getAuto_end_date_time() {
        return auto_end_date_time;
    }

    public void setAuto_end_date_time(long auto_end_date_time) {
        this.auto_end_date_time = auto_end_date_time;
    }

    public void setCursorToModel(Cursor cursor) {
        setId(cursor.getInt(cursor.getColumnIndex(DBAdapter._ID)));
        setMerchant_id(cursor.getString(cursor.getColumnIndex(DBAdapter.MERCHANT_ID)));
        setImage_name(cursor.getString(cursor.getColumnIndex(DBAdapter.IMAGE_NAME)));
        setRemote_location(cursor.getString(cursor.getColumnIndex(DBAdapter.REMOTE_LOCATION)));
        setLocal_location(cursor.getString(cursor.getColumnIndex(DBAdapter.LOCAL_LOCATION)));
        setIs_enable(cursor.getInt(cursor.getColumnIndex(DBAdapter.IMAGE_IS_ENABLE)));
        setIs_schedule(cursor.getInt(cursor.getColumnIndex(DBAdapter.IMAGE_IS_SCHEDULE)));

        setAuto_start_date_time(cursor.getLong(cursor.getColumnIndex(DBAdapter.AUTO_START_DATE_TIME)));
        setAuto_end_date_time(cursor.getLong(cursor.getColumnIndex(DBAdapter.AUTO_END_DATE_TIME)));
    }
    public void setStringArrayToModel(String[] strArr) {
        setId(Integer.parseInt(strArr[0]));
        setMerchant_id(strArr[1]);
        setImage_name(strArr[2]);
        setRemote_location(strArr[3]);
        setLocal_location(strArr[4]);
        setIs_enable(Integer.parseInt(strArr[5]));
        setIs_schedule(Integer.parseInt(strArr[6]));

        setAuto_start_date_time(Long.parseLong(strArr[7]));
        setAuto_end_date_time(Long.parseLong(strArr[8]));
    }
}
