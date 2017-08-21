package com.texasbrokers.screensaver.listener;

import com.texasbrokers.screensaver.model.AmazonS3UpdateResponseModel;

/**
 * Created by chetan on 7/7/17.
 */

public interface OnServerDataChange {
    void onDataChange(AmazonS3UpdateResponseModel amazonS3UpdateResponseModel);
}
