package com.screensaver.util;

/**
 * Created by chetan on 29/6/17.
 */

public class Constants {
    /*
        * You should replace these values with your own. See the README for details
        * on what to fill in.
        */
    public static final String COGNITO_POOL_ID = "us-east-1:012de2d1-f2eb-45e5-86d7-01b683b92b31";

    /*
     * Region of your Cognito identity pool ID.
     */
    public static final String COGNITO_POOL_REGION = "us-east-1";

    /*
     * Note, you must first create a bucket using the S3 console before running
     * the sample (https://console.aws.amazon.com/s3/). After creating a bucket,
     * put it's name in the field below.
     */
    public static final String BUCKET_NAME = "acquaint";

    /*
     * Region of your bucket.
     */
    public static final String BUCKET_REGION = "us-east-1";
    public static final String EMPLOYEE = "employee";
    public static final String MERCHANT = "merchant";

    public static final String IMAGE_MODEL = "image_model";
    public static final String IS_UPDATE = "is_update";
    public static final String SNS_TOPIC = "screensaver";
    public static final String FIREBASE_WEB_SERVER_API = "AIzaSyAMpoptqyAkgiTDk04RGyur-xWlR1GixBk";
    public static final String APP_NAME = "ScreenSaver";
    public static final String PREF_END_POINT_ARN = "end_point_arn";
    public static final String FILE_NAME = "screen_saver";
    public static final String PREF_LONG_INACTIVE = "long_inactive";
    public static final String PREF_MERCHANT_ID = "merchant_id";
    public static final String PREF_ROLE = "role";
}
