package com.agc.report.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by agc-android on 1/3/17.
 */

public class CustomButton extends Button {


    public CustomButton(Context context) {
        super(context);
        this.setTypeface(Common.getTypeFace(context));
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setTypeface(Common.getTypeFace(context));
    }

    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setTypeface(Common.getTypeFace(context));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.setTypeface(Common.getTypeFace(context));
    }
}
