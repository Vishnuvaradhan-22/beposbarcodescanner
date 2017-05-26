package com.vectron.barcodescanner.ui;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.vectron.barcodescanner.R;

import com.vectron.barcodescanner.model.POSSystem;
import com.vectron.barcodescanner.model.Store;
import com.vectron.barcodescanner.model.Venue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;


public class ConfigurationActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner venue;
    private Spinner store;
    private Spinner priceName;
    private EditText api;
    private EditText printerName;
    private Button saveButton;

    private SharedPreferences sharedPreferences;
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

        boolean networkStatus = NetworkStatusChecker.getNetworkConnectivity(this);
        if(networkStatus){
            Cache cache = new DiskBasedCache(getCacheDir(),1024*1024);
            Network network = new BasicNetwork(new HurlStack());
            mRequestQueue = new RequestQueue(cache,network);

            mRequestQueue.start();

            mposSystem = new POSSystem();
            selectedVenue = new Venue();
            venue = (Spinner)findViewById(R.id.sp_venue);
            store = (Spinner)findViewById(R.id.sp_store);
            priceName = (Spinner)findViewById(R.id.sp_price_name);
            printerName = (EditText)findViewById(R.id.et_printer_name);
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            api = (EditText)findViewById(R.id.et_api_ip);

            //Loads stored configuration from Shared Preferences
            checkPosObjectInPreferences();

            api.setOnFocusChangeListener(apiFocusChangeListener);
            saveButton = (Button)findViewById(R.id.btn_save);
            saveButton.setOnClickListener(saveListener);
        }
        else
            Toast.makeText(ConfigurationActivity.this, "Please connect to internet!", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onStop() {
        super.onStop();
        mRequestQueue.stop();
    }

    private View.OnFocusChangeListener apiFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean hasFocus) {
            if(!hasFocus){
                String api = ConfigurationActivity.this.api.getText().toString();
                if(api.length() >0){
                    try {
                        String url = api + "/" + "venues";

                        JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                Log.d("Response",response.toString());
                                try {
                                    ConfigurationActivity.this.mposSystem.setApiAdress(ConfigurationActivity.this.api.getText().toString());
                                    JSONArray venuesArray = response;
                                    Log.d("Data",venuesArray.toString(2));
                                    for (int i = 0; i < venuesArray.length(); i++) {
                                        JSONObject venueJson = venuesArray.getJSONObject(i);
                                        Venue venue = new Venue();
                                        venue.setId(Long.parseLong(venueJson.getString("VenueID")));
                                        venue.setName(venueJson.getString("Name"));
                                        Iterator keys = venueJson.keys();
                                        while (keys.hasNext()) {
                                            String key = (String) keys.next();
                                            if (key.contains("PriceName_") && venueJson.getString(key).length()>0) {
                                                venue.addPriceName(key, venueJson.getString(key));
                                            }
                                        }
                                        ConfigurationActivity.this.mposSystem.addVenue(venue);
                                        ConfigurationActivity.this.mposSystem.addVenueName(venue.getName());
                                        loadArrayAdapter();
                                    }
                                } catch (JSONException e) {
                                    Log.d("Error in Volley",e.getMessage());
                                    Toast.makeText(ConfigurationActivity.this,"Error with JSON",Toast.LENGTH_LONG).show();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("Error in Volley",error.getMessage());
                                Toast.makeText(ConfigurationActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();
                            }
                        });
                        mRequestQueue.add(jsonObjectRequest);
                    }catch (RuntimeException exception){
                        Log.d("Error",exception.getClass().getSimpleName());
                    }
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

    private void loadArrayAdapter(){
        ArrayAdapter<String> venueAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,ConfigurationActivity.this.mposSystem.getVenueNames());
        venueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        venue.setAdapter(venueAdapter);
        venue.setOnItemSelectedListener(this);
        if(mposSystem.getVenueStored().getName()!= null){
            int position = venueAdapter.getPosition(mposSystem.getVenueStored().getName());
            venue.setSelection(position);
        }
    }
    private void saveData(){
        boolean validationResult = validateInput();
        if(validationResult){
            this.mposSystem.setPrinterBluetoothAddress(printerName.getText().toString());
            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String jsonData = gson.toJson(this.mposSystem);
            editor.putString("POSSystem",jsonData);
            editor.apply();
            Toast.makeText(ConfigurationActivity.this, "Configuration Saved!", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validateInput(){
        String api = this.api.getText().toString();
        String printerName = this.printerName.getText().toString();
        if(printerName.length() > 0){
            if(api.length() > 0){
                return true;
            }
            else{
                this.api.setError("Please enter valid API");
                return false;
            }
        }
        else{
            this.printerName.setError("Please enter printer name");
            return false;
        }
    }

    private void checkPosObjectInPreferences(){
        Gson gson = new Gson();
        if(sharedPreferences.contains("POSSystem")){
            String posJson = sharedPreferences.getString("POSSystem","");
            this.mposSystem = gson.fromJson(posJson,POSSystem.class);
            this.api.setText(mposSystem.getApiAdress());
            this.printerName.setText(mposSystem.getPrinterBluetoothAddress());
            loadArrayAdapter();

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

        String venueSelected = (String) adapterView.getItemAtPosition(i);
        String venueId = null;
        Iterator venues = mposSystem.getVenues().iterator();
        while (venues.hasNext()) {
            Venue venue = (Venue) venues.next();
            if (venue.getName().equals(venueSelected)) {
                venueId = venue.getId() + "";
                selectedVenue = venue;
                mposSystem.setVenueStored(venue);
                break;
            }
        }
        try {
            String url = api.getText().toString() + "/" + "stores?VenueID=" + venueId;
            Log.d("Url",url);
            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    try {
                        JSONArray stores = response;
                        for (int i = 0; i < stores.length(); i++) {
                            JSONObject storeJson = stores.getJSONObject(i);
                            Store store = new Store();
                            store.setName(storeJson.getString("Name"));
                            store.setId(Long.parseLong(storeJson.getString("StoreID")));
                            selectedVenue.addStore(store);
                            mposSystem.updateVenue(selectedVenue);
                            loadStoresArrayAdapter();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    Log.d("error", error.toString());
                }
            });
            if (venueId != null)
                mRequestQueue.add(jsonObjectRequest);
        }catch(RuntimeException exception){
            Log.d("Exception",exception.toString());
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        Log.d("Nothing Selected","test");
    }

    private void loadStoresArrayAdapter(){
        ArrayAdapter<String> storeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,selectedVenue.getStoreNames());
        storeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        store.setAdapter(storeAdapter);
        store.setOnItemSelectedListener(storeItemSelectedListener);
        if(mposSystem.getStoreSelected().getName()!=null){
            int position = storeAdapter.getPosition(mposSystem.getStoreSelected().getName());
            store.setSelection(position);
        }
        loadPriceAdapter();
    }

    private void loadPriceAdapter(){
        ArrayAdapter<String> priceAdaper = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,selectedVenue.getPriceNameList());
        priceAdaper.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        priceName.setAdapter(priceAdaper);
        priceName.setOnItemSelectedListener(priceSelectedListener);
        if(mposSystem.getPriceNameSelected() != null){
            int position = priceAdaper.getPosition(mposSystem.getPriceNameSelected());
            priceName.setSelection(position);
        }

    }

    private AdapterView.OnItemSelectedListener storeItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String storeName = (String)adapterView.getItemAtPosition(i);
            Iterator<Store> stores = selectedVenue.getStores().iterator();
            while(stores.hasNext()){
                Store store = stores.next();
                if(store.getName() == storeName ){
                    mposSystem.setStoreSelected(store);
                    break;
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private AdapterView.OnItemSelectedListener priceSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            for(Map.Entry<String,String> entry : selectedVenue.getPriceName().entrySet()){
                if(entry.getValue().equals((String)adapterView.getItemAtPosition(i))){
                    mposSystem.setPriceNameSelected(entry.getKey());
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };
}