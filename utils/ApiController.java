package com.agc.report.utils;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.util.Log;

import com.agc.report.MyApplication;
import com.agc.report.R;
import com.agc.report.ViewDocumentsFragment;
import com.agc.report.listener.ApiResponseListener;
import com.agc.report.listener.OnProgressUpdate;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by agc-android on 18/1/17.
 */

public class ApiController {
    private Context context;
    public ApiResponseListener apiResponseListener;
    private InputStreamVolleyRequest request;
    Map<String, String> responseHeaders;
    private OnProgressUpdate onProgressUpdate;

    public ApiController() {
    }

    public ApiController(Context context, Fragment fragment) {
        this.context = context;
        if (fragment != null) {
            this.apiResponseListener = (ApiResponseListener) fragment;
            if (fragment instanceof ViewDocumentsFragment)
            this.onProgressUpdate = (OnProgressUpdate) fragment;
        } else {
            this.apiResponseListener = (ApiResponseListener) context;
        }

    }

    public void actionCallWebService(String url, final HashMap<String, String> params) {

        Common.showProgressDialog(context);
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
        MyApplication.getInstance().addToRequestQueue(stringRequest);
    }

    public void actionCallWebServiceWithFiles(String url, final HashMap<String, String> params, final Map<String, VolleyMultipartRequest.DataPart> file) {

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                try {
                    JSONObject result = new JSONObject(resultResponse);
                    /*String status = result.getString("status");
                    String message = result.getString("message");

                    if (status.equals(Constants.SUCCESS)) {
                        // tell everybody you have succed upload image and post strings
                        Log.i("Messsage", message);
                    } else {
                        Log.i("Unexpected", message);
                    }*/
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }, onProgressUpdate) {
            @Override
            protected Map<String, String> getParams() {

                return params;
            }


            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
//                params.put("doc_file", new DataPart("file_avatar.jpg", Common.getFileDataFromDrawable(context, context.getDrawable(R.drawable.ic_app_icon)), "image/png"));
//                params.put("upload_file", new DataPart("screens.zip",file,"application/zip"));


                return file;
            }
        };

        MyApplication.getInstance().addToRequestQueue(multipartRequest);
    }

    public void actionDownloadFile(String url, final HashMap<String, String> params) {
        Common.showProgressDialog(context);
        Request<byte[]> req = new Request<byte[]>(Request.Method.GET, url, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "");
            }

        }) {
            @Override
            protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
                responseHeaders = response.headers;


                //Pass the response data here
                return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
            }


            @Override
            protected void deliverResponse(byte[] response) {
                int file_length = response.length;
                FileOutputStream out = null;
                InputStream input = null;
                try {
                    input = new ByteArrayInputStream(response);
                } catch (Exception e) {

                }

                try {
                    File path = new File("/sdcard/Acquaint/");

                    path.mkdir();

                    File file = new File(path, "/" + params.get("name"));
                    if (file.exists()) {
                        file.delete();
                    }
                    out = new FileOutputStream(file);
                    BufferedOutputStream output = new BufferedOutputStream(out);
                    byte data[] = new byte[1024];
                    long total = 0;

                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                        final int progress = (int) (total * 100) / file_length;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ViewDocumentsFragment.edt_search.setText(""+progress);
                                Log.e("progress", "" + progress);
                            }
                        });

                    }
                    output.flush();

                    output.close();
                    input.close();
//                   out.write(response);

//                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Common.dismissProgressDialog();
            }
        };
        /*request = new InputStreamVolleyRequest(Request.Method.GET, url, new Response.Listener<byte[]>() {
            @Override
            public void onResponse(byte[] response) {


                int file_length = response.length;
                FileOutputStream out = null;
                InputStream input = null;
                try {
                    input = new ByteArrayInputStream(response);
                } catch (Exception e) {

                }

                try {
                    File path = new File("/sdcard/Acquaint/");

                    path.mkdir();

                    File file = new File(path, "/screens.zip");
                    out = new FileOutputStream(file);
                    BufferedOutputStream output = new BufferedOutputStream(out);
                    byte data[] = new byte[1024];
                    long total = 0;

                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        output.write(data, 0, count);
                        final int progress = (int) (total * 100) / file_length;
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                ViewDocumentsFragment.edt_search.setText(""+progress);
                                Log.e("progress", "" + progress);
                            }
                        });

                    }
                    output.flush();

                    output.close();
                    input.close();
                   *//* out.write(response);

                    out.close();*//*
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("", "");

            }
        }, params);*/


        MyApplication.getInstance().addToRequestQueue(req);
    }


}
