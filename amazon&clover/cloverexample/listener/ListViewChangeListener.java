package com.texasbrokers.screensaver.listener;

import com.texasbrokers.screensaver.model.ImagesModel;

/**
 * Created by chetan on 30/6/17.
 */

public interface ListViewChangeListener {
    void onEnabledDisabled(ImagesModel ImagesModel, int position, boolean isChecked);

    void onRemoveImage(ImagesModel ImagesModel, int position);

    void onEditImage(ImagesModel imagesModel, int position);
}
