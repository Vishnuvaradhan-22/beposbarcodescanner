package com.vectron.barcodescanner.ui;

import android.app.DownloadManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonObjectRequest;
import com.vectron.barcodescanner.R;
import com.vectron.barcodescanner.model.APIRequest;
import com.vectron.barcodescanner.model.POSSystem;
import com.vectron.barcodescanner.model.Store;
import com.vectron.barcodescanner.model.Venue;
import com.vectron.barcodescanner.utility.DataLoader;
import com.vectron.barcodescanner.utility.WebServiceGateway;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationActivity extends AppCompatActivity implements DataLoader {

    private Spinner venue;
    private Spinner store;
    private Spinner priceName;
    private EditText api;
    private EditText printerName;
    private Button saveButton;

    private RequestQueue mRequestQueue;
    private POSSystem mposSystem;
    private Venue selectedVenue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        initializeUi();
    }

    private void initializeUi(){
        Toolbar toolbar = (Toolbar)findViewById(R.id.action_menu_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Cache cache = new DiskBasedCache(getCacheDir(),1024*1024);
        Network network = new BasicNetwork(new HurlStack());
        mRequestQueue = new RequestQueue(cache,network);

        mRequestQueue.start();

        venue = (Spinner)findViewById(R.id.sp_venue);
        store = (Spinner)findViewById(R.id.sp_store);
        priceName = (Spinner)findViewById(R.id.sp_price_name);

        venue.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String venueSelected = (String)adapterView.getItemAtPosition(i);
                String venueId = null;
                Iterator venues = ConfigurationActivity.this.mposSystem.getVenues().iterator();
                while(venues.hasNext()){
                    Venue venue = (Venue)venues.next();
                    if(venue.getName().equals(venueSelected)) {
                        venueId = venue.getId() + "";
                        selectedVenue = venue;
                    }
                }
                String url = api + "/" + "stores?venueID="+venueId;
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray stores = response.getJSONArray("Stores");
                            for(int i=0;i<stores.length();i++){
                                JSONObject storeJson = stores.getJSONObject(i);
                                Store store = new Store();
                                store.setName(storeJson.getString("Name"));
                                store.setId(Long.parseLong(storeJson.getString("StoreID")));
                                selectedVenue.addStore(store);
                                mposSystem.updateVenue(selectedVenue);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
                if(venueId!=null)
                    mRequestQueue.add(jsonObjectRequest);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ArrayAdapter<String> venueAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,this.mposSystem.getVenueNames());
        ArrayAdapter<String> storeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,selectedVenue.getStoreNames());
        ArrayAdapter<String> priceAdaper = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,selectedVenue.getPriceNameList());
        venue.setAdapter(venueAdapter);
        store.setAdapter(storeAdapter);
        priceName.setAdapter(priceAdaper);

        api = (EditText)findViewById(R.id.et_api_ip);

        api.setOnFocusChangeListener(apiFocusChangeListener);
        saveButton = (Button)findViewById(R.id.btn_save);
        saveButton.setOnClickListener(saveListener);
    }

    private View.OnFocusChangeListener apiFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if(!hasFocus){
                String api = ConfigurationActivity.this.api.getText().toString();
                if(api != null){
                    String url = api+"/"+"venues";
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,url,null, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                ConfigurationActivity.this.mposSystem.setApiAdress(ConfigurationActivity.this.api.getText().toString());
                                JSONArray venuesArray = (JSONArray) response.get("Venues");
                                for(int i=0; i<venuesArray.length();i++){
                                    JSONObject venueJson = venuesArray.getJSONObject(i);
                                    Venue venue = new Venue();
                                    venue.setId(Long.parseLong(venueJson.getString("VenueID")));
                                    venue.setName(venueJson.getString("Name"));
                                    Iterator keys = response.keys();
                                    while(keys.hasNext()){
                                        String key = (String)keys.next();
                                        if(key.contains("PriceName_")){
                                            venue.addPriceName(key,venueJson.getString(key));
                                        }
                                    }
                                    ConfigurationActivity.this.mposSystem.addVenue(venue);
                                    ConfigurationActivity.this.mposSystem.addVenueName(venue.getName());
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    });

                }

            }
        }
    };

    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();
        }
    };

    private void saveData(){

    }

    @Override
    public void loadData(String jsonData) {

    }
}