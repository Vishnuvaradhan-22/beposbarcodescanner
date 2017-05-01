package com.vectron.barcodescanner.utility;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.vectron.barcodescanner.model.APIRequest;


import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;


import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Vish on 26/04/2017.
 */
public class WebServiceGateway extends AsyncTask<Void,Void,String> {

    private String jsonData;
    private Context context;
    private DataLoader dataLoader;
    private ProgressDialog progressDialog;
    private APIRequest requestObject;
    private String url = "http://192.168.16.210:8080/BepozProBarcodeSer/public/api/";
    public WebServiceGateway(Context context,APIRequest requestObject){
        this.dataLoader = (DataLoader)context;
        this.context = context;
        this.requestObject = requestObject;
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
        //progressDialog.show();
    }

    @Override
    protected String doInBackground(Void...voids) {
        HttpClient httpClient = new DefaultHttpClient();
        HttpResponse httpResponse;
        HttpGet httpGet;
        HttpPost httpPost;

        String parameterString;

        switch(requestObject.getType()){
            case "GET":
                Map<String,String> parameters = requestObject.getParameters();
                if(!parameters.isEmpty()){
                    ArrayList<NameValuePair> requestParameters = new ArrayList<NameValuePair>();
                    for(Map.Entry<String,String> entry : parameters.entrySet()){
                        requestParameters.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
                    }
                    parameterString = URLEncodedUtils.format(requestParameters,"utf-8");
                    requestObject.setEndPoint(requestObject.getEndPoint()+parameterString);
                }
                try {
                    httpGet = new HttpGet();
                    URI uri = new URI(requestObject.getUrl()+requestObject.getEndPoint());
                    httpGet.setURI(uri);
                    Log.d("URL",httpGet.getURI().toString());
                    httpResponse = httpClient.execute(httpGet);
                    jsonData = EntityUtils.toString(httpResponse.getEntity());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
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
       // progressDialog.dismiss();
        dataLoader.loadData(jsonData);
    }
}