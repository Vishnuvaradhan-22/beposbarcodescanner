package com.vectron.barcodescanner.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.vectron.barcodescanner.R;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationActivity extends AppCompatActivity {

    private Spinner venue;
    private Spinner store;
    private Spinner priceName;
    private EditText api;
    private EditText printerName;
    private Button saveButton;

    private List<String> listOfVenues;
    private List<String> listOfStores;
    private List<String> listOfPriceNames;

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

        venue = (Spinner)findViewById(R.id.sp_venue);
        store = (Spinner)findViewById(R.id.sp_store);
        priceName = (Spinner)findViewById(R.id.sp_price_name);
        listOfVenues = new ArrayList<String>();
        listOfStores = new ArrayList<String>();
        listOfPriceNames = new ArrayList<String>();

        ArrayAdapter<String> venueAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,listOfVenues);
        ArrayAdapter<String> storeAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,listOfStores);
        ArrayAdapter<String> priceAdaper = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line,listOfPriceNames);
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
        public void onFocusChange(View view, boolean b) {
            /** To-Do
             * Create Async task
             */
            listOfVenues.add("Venue1");
            listOfVenues.add("Venue2");

            listOfStores.add("Store1");
            listOfStores.add("Store2");

            listOfPriceNames.add("Price1");
            listOfPriceNames.add("Price1");
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
}