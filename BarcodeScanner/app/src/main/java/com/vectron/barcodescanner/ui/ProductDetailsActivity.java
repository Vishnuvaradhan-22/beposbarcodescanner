package com.vectron.barcodescanner.ui;

import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.vectron.barcodescanner.R;
import com.vectron.barcodescanner.model.POSSystem;
import com.vectron.barcodescanner.model.Product;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private Product product;

    private TextView productId;
    private TextView productName;
    private TextView comment;
    private TextView longName;
    private EditText priceValue;
    private Button saveButton;
    private Button printButton;

    private boolean showToast = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);
        initializeUI();
    }

    private void initializeUI(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.action_menu_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle productBundle = getIntent().getBundleExtra("ProductBundle");
        this.product = (Product)productBundle.getSerializable("Product");

        productId = (TextView)findViewById(R.id.tv_product_id);
        productName = (TextView)findViewById(R.id.tv_product_name);
        comment = (TextView)findViewById(R.id.tv_comment);
        longName = (TextView)findViewById(R.id.tv_long_name);
        priceValue = (EditText)findViewById(R.id.et_price_value);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if(product != null)
            loadData();
        saveButton = (Button)findViewById(R.id.btn_save_product);
        saveButton.setOnClickListener(saveListener);
        //printButton.setOnClickListener(printListener);

    }

    private void loadData(){
        productId.setText(String.valueOf(product.getId()));
        productName.setText(product.getName());
        if(!product.getComment().equals("null"))
            comment.setText(product.getComment());
        longName.setText(product.getLongName());
        priceValue.setText(String.valueOf(product.getValue()));
        if(showToast)
            Toast.makeText(this,"Price Value Updated",Toast.LENGTH_LONG).show();
    }

    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();
        }
    };
    private void saveData(){
        String price = priceValue.getText().toString();
        if(Long.parseLong(price) != product.getValue()){
            product.setValue(Long.parseLong(price));
            Gson gson = new Gson();
            if(sharedPreferences.contains("POSSystem")){
                String posJson = sharedPreferences.getString("POSSystem","");
                POSSystem posSystem = gson.fromJson(posJson,POSSystem.class);
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                String postRequestUrl =  posSystem.getApiAdress() + "/product/setprice?ProductID="+product.getId()+"&StoreID="+product.getStoreId()+"&PriceName="+product.getPriceName()+"&PriceValue="+priceValue.getText().toString();
                Log.d("Url",postRequestUrl);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,postRequestUrl, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject productJson = (JSONObject)response.get("data");
                            Log.d("JSON",productJson.toString());
                            product.setId(Long.parseLong(productJson.getString("ProductID")));
                            product.setStoreId(Long.parseLong(productJson.getString("StoreID")));
                            product.setPriceName(productJson.getString("PriceName"));
                            product.setValue(Long.parseLong(productJson.getString("PriceValue")));
                            Gson gson = new Gson();
                            String newProductJson = gson.toJson(product);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("POSSystem",newProductJson);
                            editor.apply();
                            showToast = true;
                            loadData();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> postParameters = new HashMap<String,String>();
                        postParameters.put("ProductID",String.valueOf(product.getId()));
                        postParameters.put("StoreID",String.valueOf(product.getStoreId()));
                        postParameters.put("PriceName",product.getName());
                        postParameters.put("PriceValue",String.valueOf(product.getValue()));
                        return postParameters;
                    }
                };
                requestQueue.add(jsonObjectRequest);

            }
            else{
                Toast.makeText(this,"Please save API",Toast.LENGTH_LONG).show();
            }

        }
        else{
            Intent intent = new Intent();
            intent.setClass(this,ScannerActivity.class);
            startActivity(intent);
        }
    }
}
