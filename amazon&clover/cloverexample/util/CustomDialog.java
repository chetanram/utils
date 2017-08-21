package com.texasbrokers.screensaver.util;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.texasbrokers.screensaver.R;


/**
 * Created by chetan on 22/6/17.
 */

public class CustomDialog extends Dialog {

    private TextView tv_title, tv_msg;
    private Button btn_positive, btn_negative;
    private Context context;

    public CustomDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        setContentView(R.layout.dialog_custom);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_msg = (TextView) findViewById(R.id.tv_msg);

        btn_positive = (Button) findViewById(R.id.btn_positive);
        btn_negative = (Button) findViewById(R.id.btn_negative);


    }

    public void setTitleVisibility(int visibility) {
        tv_title.setVisibility(visibility);
    }

    public void setMsgVisibility(int visibility) {
        tv_msg.setVisibility(visibility);
    }

    public void setPositiveButtonVisibility(int visibility) {
        btn_positive.setVisibility(visibility);
        if (visibility == View.GONE) {
            btn_negative.setLayoutParams(new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.fix_button_width), ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        }else {
            btn_negative.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }
    }

    public void setNegativeButtonVisibility(int visibility) {
        btn_negative.setVisibility(visibility);
        if (visibility == View.GONE) {
            btn_positive.setLayoutParams(new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.fix_button_width), ViewGroup.LayoutParams.WRAP_CONTENT, 0));
        }else {
            btn_positive.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        }

    }



    public void setMessege(String msg) {
        tv_msg.setText(msg);
    }

    public void setPositiveButtonText(String text) {
        btn_positive.setText(text);
    }

    public void setNegativeButtonText(String text) {
        btn_negative.setText(text);
    }


    public void setOnPositiveClickListener(View.OnClickListener clickListener) {
        btn_positive.setOnClickListener(clickListener);
    }

    public void setOnNegativeClickListener(View.OnClickListener clickListener) {
        btn_negative.setOnClickListener(clickListener);
    }

}
