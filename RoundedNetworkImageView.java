package com.agc.lottery.Utils;

/**
 * Created by agc-android on 31/1/17.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.agc.lottery.R;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.jetbrains.annotations.NotNull;


public class RoundedNetworkImageView extends NetworkImageView {
    private int secondBorderColor = getResources().getColor(R.color.col_white);
    private int firstBorderColor = getResources().getColor(R.color.profile_pic_border_color);
    private int firstBorderWidth = (int) Common.convertDpToPixel(2,getContext());//10;
    private int secondBorderWidth = (int) Common.convertDpToPixel(1,getContext());//10;
    private Bitmap mLocalBitmap;
    private boolean mShowLocal;
    private int top, right, left, bottom;

    public RoundedNetworkImageView(Context context) {
        super(context);
    }

    public RoundedNetworkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RoundedNetworkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

//    public void setBorderColor(int resourceID) {
//        borderColor = resourceID;
//    }
//
//    public void setBorderWidth(int border) {
//        borderWidth = border;
//    }
//
//    public int getBorderWidth() {
//        return borderWidth;
//    }

    @Override
    protected void onDraw(@NotNull Canvas canvas) {
        Drawable drawable = getDrawable();

        if (drawable == null || getWidth() == 0 || getHeight() == 0)
            return;

        Bitmap b = ((BitmapDrawable) drawable).getBitmap();
        if (b != null) {
            Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);

            int radius = (getWidth() < getHeight()) ? getWidth() / 2 : getHeight() / 2;

            Bitmap roundBitmap = getCroppedBitmap(bitmap, radius);
            canvas.drawBitmap(roundBitmap, 0, top - getResources().getDimension(R.dimen.top_profile), null);
        }
    }

    public Bitmap getCroppedBitmap(Bitmap bmp, int radius) {
        Bitmap scaledBmp = Bitmap.createScaledBitmap(bmp, radius * 2, radius * 2, false);
        Bitmap output = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);


        //added by agc for second border
        paint.setColor(firstBorderColor);
        canvas.drawCircle((getWidth() / 2), (getHeight() / 2), (radius - getResources().getDimension(R.dimen.radius_profile)+secondBorderWidth), paint);

        // Draws a circle to create the border
        paint.setColor(secondBorderColor);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (radius - getResources().getDimension(R.dimen.radius_profile)-firstBorderWidth), paint);


        // Draws the image subtracting the border width
        BitmapShader s = new BitmapShader(scaledBmp, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        paint.setShader(s);
//        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (radius - firstBorderWidth - 0.5f) - getResources().getDimension(R.dimen.radius_profile), paint);
        canvas.drawCircle(getWidth() / 2, getHeight() / 2, (radius - (firstBorderWidth+secondBorderWidth) - 0.5f) - getResources().getDimension(R.dimen.radius_profile), paint);

        return output;
    }

    public void setLocalImageBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            mShowLocal = true;
        }
        this.mLocalBitmap = bitmap;
        requestLayout();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.top = top;
        this.right = right;
        this.left = left;
        this.bottom = bottom;
        super.onLayout(changed, left, top, right, bottom);
        if (mShowLocal) {
            setImageBitmap(mLocalBitmap);
        }
    }

    @Override
    public void setImageUrl(String url, ImageLoader imageLoader) {
        mShowLocal = false;
        super.setImageUrl(url, imageLoader);
    }
}