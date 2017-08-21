package com.texasbrokers.screensaver.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.texasbrokers.screensaver.util.Constants;


public class PushNotificationModel {


    @SerializedName(Constants.MERCHANT_ID)
    @Expose
    private String merchantId;

    @SerializedName(Constants.CSV)
    @Expose
    private String csv;

    @SerializedName(Constants.IMAGE)
    @Expose
    String image;

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getCsv() {
        return csv;
    }

    public void setCsv(String csv) {
        this.csv = csv;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
