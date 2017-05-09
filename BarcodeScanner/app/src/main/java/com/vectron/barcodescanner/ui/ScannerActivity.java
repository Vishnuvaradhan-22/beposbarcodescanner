package com.vectron.barcodescanner.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.vectron.barcodescanner.R;
import com.vectron.barcodescanner.model.POSSystem;
import com.vectron.barcodescanner.model.Product;
import com.vectron.barcodescanner.model.Venue;

import org.json.JSONException;
import org.json.JSONObject;

public class ScannerActivity extends AppCompatActivity {

    private Button scanButton;
    private SharedPreferences sharedPreferences;
    private POSSystem mposSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        initializeUi();
    }

    private void initializeUi(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.action_menu_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mposSystem = new POSSystem();
        scanButton = (Button)findViewById(R.id.btn_scan);
        scanButton.setOnClickListener(scanButtonListener);
    }

    private OnClickListener scanButtonListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            scanBarcode();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        checkPosObjectInPreferences();
    }

    private void checkPosObjectInPreferences(){
        Gson gson = new Gson();
        Log.d("Boolean:",sharedPreferences.contains("POSSystem")+"");
        if(sharedPreferences.contains("POSSystem")){
            String posJson = sharedPreferences.getString("POSSystem","");
            this.mposSystem = gson.fromJson(posJson,POSSystem.class);
            scanButton.setVisibility(View.VISIBLE);
        }
        else{
            Toast.makeText(this,"Please store API",Toast.LENGTH_LONG).show();
            scanButton.setVisibility(View.INVISIBLE);
        }
    }

    private void scanBarcode(){
        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.initiateScan();
    }
    public void onActivityResult(int requestCode, final int resultCode, Intent intent) {
        final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            Log.d("BarCodeScanned",scanResult.getContents());
            String priceName = mposSystem.getPriceNameSelected().replace("PriceName_","");
            String url =  mposSystem.getApiAdress() + "/product/"+scanResult.getContents()+"?storeId="+mposSystem.getStoreSelected().getId()+"&priceName="+priceName;
            Log.d("PostURL",url);
            if(mposSystem.getApiAdress() != null && mposSystem.getApiAdress().length() >0){
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject product = response;

                            Product productFound = new Product();
                            productFound.setId(Long.parseLong(product.getString("ProductID")));
                            productFound.setStoreId(Long.parseLong(product.getString("StoreID")));
                            productFound.setSize(Long.parseLong(product.getString("Size")));
                            productFound.setName(product.getString("Name"));
                            productFound.setBarcode(scanResult.getContents());
                            productFound.setComment(product.getString("Comment"));
                            productFound.setLongName(product.getString("LongName"));
                            productFound.setPriceName(product.getString("PriceName"));
                            productFound.setValue(Long.parseLong(product.getString("PriceValue")));
                            Bundle productBundle = new Bundle();
                            productBundle.putSerializable("Product", productFound);
                            Intent intent = new Intent();
                            intent.setClass(ScannerActivity.this, ProductDetailsActivity.class);
                            intent.putExtra("ProductBundle", productBundle);
                            startActivity(intent);
                        }catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        NetworkResponse response = error.networkResponse;
                        if(response !=null && response.data != null){
                            try {
                                JSONObject errorJson = new JSONObject(new String(response.data));
                                switch(response.statusCode){
                                    case 400:
                                        Toast.makeText(ScannerActivity.this,errorJson.getString("message"),Toast.LENGTH_LONG).show();
                                        break;
                                    case 404:
                                        Toast.makeText(ScannerActivity.this,"Product Not Found",Toast.LENGTH_LONG).show();
                                        break;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                });
                requestQueue.add(jsonObjectRequest);
            }

        }
    }
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.scanner_activity,menu);
        return true;
    }
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.configure_page) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), ConfigurationActivity.class);
            startActivity(intent);
        }
        return true;
    }
}
