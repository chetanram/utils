package com.agc.lottery.Utils;

import android.content.Context;
import android.util.Log;

import com.agc.lottery.MyApplication;
import com.agc.lottery.R;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by agc-android on 18/1/17.
 */

public class ApiController {

    private Context context;
    public ApiResponseListener apiResponseListener;

    public ApiController(Context context) {
        this.context = context;

    }

    public void actionCallWebService(String url, final HashMap<String, String> params) {

        if (params.get(Constants.PARAM_START_INDEX) != null) {
            if (params.get(Constants.PARAM_START_INDEX).equalsIgnoreCase("0")){
                Common.showProgressDialog(context);
            }
        } else {
            Common.showProgressDialog(context);
        }

        if(params.get(Constants.PARAM_DISPLAY_PROGRESS)!=null && params.get(Constants.PARAM_DISPLAY_PROGRESS).equalsIgnoreCase("false") )
        {
            Common.dismissProgressDialog();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Common.dismissProgressDialog();
                        apiResponseListener.onSuccessResponse(response, params);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Common.dismissProgressDialog();
                        apiResponseListener.onErrorResponse(error, params);
                        Log.e("Login_volley_error", "error in login " + error);
                        if (!Common.isInternetAvailable(context)) {
                            Common.showToast(context, context.getString(R.string.no_internet));
                        }
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                Constants.MY_API_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        MyApplication.getInstance().addToRequestQueue(stringRequest);
    }
}
