package com.texasbrokers.screensaver.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;

import com.texasbrokers.screensaver.R;
import com.texasbrokers.screensaver.listener.ListViewChangeListener;
import com.texasbrokers.screensaver.model.ImagesModel;

import java.util.List;

/**
 * Created by chetan on 30/6/17.
 */

public class ImageListAdapter extends BaseAdapter {

    private Context context;
    private List<ImagesModel> imagesModelList;
    private ListViewChangeListener listViewChangeListener;


    public ImageListAdapter(Context context, List<ImagesModel> imagesModelList) {
        this.context = context;
        this.imagesModelList = imagesModelList;
        listViewChangeListener = (ListViewChangeListener) context;
    }

    @Override
    public int getCount() {
        return imagesModelList.size();
    }

    @Override
    public Object getItem(int position) {
        return imagesModelList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.inflator_images_list, parent, false);
        ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
        Button btn_remove = (Button) view.findViewById(R.id.btn_remove);
        Button btn_edit = (Button) view.findViewById(R.id.btn_edit);
        Switch switchEnableDisable = (Switch) view.findViewById(R.id.switchEnableDisable);

        final ImagesModel imagesModel = imagesModelList.get(position);
        String srcPath = Environment.getExternalStorageDirectory() + "/" + imagesModel.getMerchant_id() + "/" + imagesModel.getImage_name();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        options.inSampleSize = 8;
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(srcPath);
        } catch (OutOfMemoryError e) {
            try {
                bitmap = BitmapFactory.decodeFile(srcPath, options);
            } catch (OutOfMemoryError e1) {
                options.inSampleSize = 16;
                try {
                    bitmap = BitmapFactory.decodeFile(srcPath, options);
                } catch (OutOfMemoryError e2) {

                }
            }
        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        }
        if (imagesModel.getIs_enable() == 0) {
            switchEnableDisable.setChecked(true);
        } else {
            switchEnableDisable.setChecked(false);
        }
        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewChangeListener.onRemoveImage(imagesModel, position);
            }
        });
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listViewChangeListener.onEditImage(imagesModel, position);
            }
        });
        switchEnableDisable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listViewChangeListener.onEnabledDisabled(imagesModel, position, isChecked);
            }
        });


        return view;
    }
}
