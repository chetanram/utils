package com.texasbrokers.screensaver.util;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.util.CloverAuth;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by chetan on 28/7/17.
 */

public class SendPushNotification extends AsyncTask<String, String, String> {

    private String data;
    private String event;
    private Context context;

    public SendPushNotification(Context context, String event, String data) {
        this.context = context;
        this.data = data;
        this.event = event;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            CloverAuth.AuthResult authorization = CloverAuth.authenticate(context, CloverAccount.getAccount(context));

            String uri = authorization.baseUrl +
                    "/v3/apps/" + authorization.appId +
                    "/merchants/" + authorization.merchantId +
                    "/notifications";
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("data", data);
            jsonObject.put("event", event);
            DefaultHttpClient client = new DefaultHttpClient();
            HttpPost request = new HttpPost(uri);
            request.addHeader("Content-type", "application/json");
            request.addHeader("Authorization", "Bearer " + authorization.authToken);
            HttpEntity entity = new ByteArrayEntity(jsonObject.toString().getBytes("UTF-8"));
            request.setEntity(entity);
            org.apache.http.HttpResponse response = client.execute(request);


            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuffer result = new StringBuffer();
            String line = "";
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
            return result.toString();


        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), e.getMessage(), e.fillInStackTrace());
        }


        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
    }
}
