package com.vectron.barcodescanner.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.vectron.barcodescanner.R;

public class ConfigurationActivity extends AppCompatActivity {

    private Spinner venue;
    private Spinner store;
    private Spinner priceName;
    private EditText api;
    private EditText printerName;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);
        initializeUi();
    }

    private void initializeUi(){
        venue = (Spinner)findViewById(R.id.sp_venue);
        store = (Spinner)findViewById(R.id.sp_store);
        priceName = (Spinner)findViewById(R.id.sp_price_name);

        saveButton = (Button)findViewById(R.id.btn_save);
        saveButton.setOnClickListener(saveListener);
    }

    private View.OnClickListener saveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            saveData();
        }
    };

    private void saveData(){

    }

}
