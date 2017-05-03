package com.agc.lottery.Utils;

import com.android.volley.VolleyError;

import java.util.HashMap;

/**
 * Created by agc-android on 18/1/17.
 */

public interface ApiResponseListener {

    void onSuccessResponse(String response, HashMap<String,String> hashMap);
    void onErrorResponse(VolleyError error, HashMap<String,String> hashMap);

}
