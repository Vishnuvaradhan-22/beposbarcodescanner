package com.vectron.barcodescanner.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

/**
 * Created by Vish on 26/04/2017.
 */
public class WebServiceGateway extends AsyncTask<String,Void,String> {

    private String jsonData;
    private Context context;
    private DataLoader dataLoader;
    private ProgressDialog progressDialog;

    public WebServiceGateway(Context context){
        this.dataLoader = (DataLoader)context;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(this.context);
        progressDialog.setTitle("Connecting");
        progressDialog.setMessage("Loading details");
        progressDialog.setIndeterminate(false);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMax(100);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse;
        HttpGet httpGet;
        HttpPost httpPost;

        switch(strings[0]){
            case "get":
                httpGet = new HttpGet(strings[1]);
                try {
                    httpResponse = httpClient.execute(httpGet);
                    jsonData = EntityUtils.toString(httpResponse.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "post":
                break;
        }

        return jsonData;
    }

    @Override
    protected void onPostExecute(String jsonData) {
        super.onPostExecute(jsonData);
        progressDialog.dismiss();
        dataLoader.loadData(jsonData);
    }
}