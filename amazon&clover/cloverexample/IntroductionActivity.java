package com.texasbrokers.screensaver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.squareup.picasso.Picasso;
import com.texasbrokers.screensaver.util.Constants;
import com.texasbrokers.screensaver.util.PrefUtils;

public class IntroductionActivity extends Activity {

    private Context context;
    private Toolbar toolBar;

    private ViewPager viewPager;
    private ImageView iv_indicator_1, iv_indicator_2, iv_indicator_3, iv_indicator_4;
    private ViewPagerAdapter viewPagerAdapter;
    TextView tv_lets_start;
    private String isIntroLoaded;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.introduction);
        context = IntroductionActivity.this;

        toolBar = (Toolbar) findViewById(R.id.my_toolbar);
        setUpToolbar();

        bundle = getIntent().getExtras();
        findViewByIds();

        isIntroLoaded = PrefUtils.getString(context, Constants.PREF_IS_INTRO_LOADED, "no");

        if (isIntroLoaded.equalsIgnoreCase("yes")) {
            tv_lets_start.performClick();
        }

        viewPagerAdapter = new ViewPagerAdapter(context);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setDotIndicator(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setDotIndicator(int position) {
        switch (position) {
            case 0:
                iv_indicator_1.setImageResource(R.drawable.dot_indicator_blue);
                iv_indicator_2.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_3.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_4.setImageResource(R.drawable.dot_indicator_dark);
                break;
            case 1:
                iv_indicator_1.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_2.setImageResource(R.drawable.dot_indicator_blue);
                iv_indicator_3.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_4.setImageResource(R.drawable.dot_indicator_dark);
                break;
            case 2:
                iv_indicator_1.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_2.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_3.setImageResource(R.drawable.dot_indicator_blue);
                iv_indicator_4.setImageResource(R.drawable.dot_indicator_dark);
                break;
            case 3:
                iv_indicator_1.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_2.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_3.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_4.setImageResource(R.drawable.dot_indicator_blue);
                break;
            default:
                iv_indicator_1.setImageResource(R.drawable.dot_indicator_blue);
                iv_indicator_2.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_3.setImageResource(R.drawable.dot_indicator_dark);
                iv_indicator_4.setImageResource(R.drawable.dot_indicator_dark);
                break;
        }
    }

    private void findViewByIds() {


        iv_indicator_1 = (ImageView) findViewById(R.id.iv_indicator_1);
        iv_indicator_2 = (ImageView) findViewById(R.id.iv_indicator_2);
        iv_indicator_3 = (ImageView) findViewById(R.id.iv_indicator_3);
        iv_indicator_4 = (ImageView) findViewById(R.id.iv_indicator_4);

        viewPager = (ViewPager) findViewById(R.id.viewPager);


    }


    public class ViewPagerAdapter extends PagerAdapter {
        private Context context;

        public ViewPagerAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((LinearLayout) object);

        }


        @Override
        public Object instantiateItem(ViewGroup container, int i) {
            View view = LayoutInflater.from(context).inflate(R.layout.inflator_introduction_slider, container, false);

            ImageView mImageView = (ImageView) view.findViewById(R.id.iv_image);
            switch (i) {
                case 0:

                    mImageView.setBackground(getResources().getDrawable(R.drawable.intro_1));
                    break;
                case 1:

                    mImageView.setBackground(getResources().getDrawable(R.drawable.intro_2));
                    break;
                case 2:

                    mImageView.setBackground(getResources().getDrawable(R.drawable.intro_3));
                    break;
                case 3:

                    mImageView.setBackground(getResources().getDrawable(R.drawable.intro_4));
                    break;
                default:
                    break;
            }


//            mImageView.setScaleType(NetworkImageView.ScaleType.CENTER_CROP);
//            mImageView.setImageUrl(imagesModelList.get(i).getImagePath(), MyApplication.getImageLoaderInstance());
            container.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int i, Object obj) {
            container.removeView((LinearLayout) obj);
        }
    }

    private void setUpToolbar() {
        tv_lets_start = (TextView) toolBar.findViewById(R.id.tv_lets_start);
        tv_lets_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefUtils.saveString(context, Constants.PREF_IS_INTRO_LOADED, "yes");
                Intent intent = new Intent(IntroductionActivity.this, SplashScreenActivity.class);
                if (bundle != null)
                    intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

    }
}
