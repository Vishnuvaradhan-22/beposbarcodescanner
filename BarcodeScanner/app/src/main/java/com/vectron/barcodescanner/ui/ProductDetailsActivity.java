package com.vectron.barcodescanner.ui;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Looper;
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
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.vectron.barcodescanner.R;
import com.vectron.barcodescanner.model.POSSystem;
import com.vectron.barcodescanner.model.Product;
import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.PrinterStatus;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ProductDetailsActivity extends AppCompatActivity {

    private Product product;
    private POSSystem posSystem;

    private TextView productId;
    private TextView productName;
    private TextView comment;
    private TextView longName;
    private EditText priceValue;
    private Button saveButton;
    private Button printButton;

    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;

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

        boolean networkStatus = NetworkStatusChecker.getNetworkConnectivity(this);
        if(networkStatus){
            Bundle productBundle = getIntent().getBundleExtra("ProductBundle");
            this.product = (Product)productBundle.getSerializable("Product");
            this.posSystem = (POSSystem)productBundle.getSerializable("POSSystem");
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
            printButton = (Button)findViewById(R.id.btn_print);
            printButton.setOnClickListener(printListener);

        }
        else
            Toast.makeText(ProductDetailsActivity.this, "Please connect to internet", Toast.LENGTH_SHORT).show();
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

    private boolean checkBluetooth(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            Toast.makeText(ProductDetailsActivity.this, "Bluetooth not supported in this device", Toast.LENGTH_LONG).show();
            return false;
        }
        else{
            if(!mBluetoothAdapter.isEnabled()) {
                Toast.makeText(ProductDetailsActivity.this, "Please turn on bluetooth to Print", Toast.LENGTH_SHORT).show();
                return false;
            }
            else
                return true;
        }
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

    private View.OnClickListener printListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            printImage();
        }
    };

    private void printImage(){
        try {

            boolean testBluetooth = checkBluetooth();

            if(testBluetooth){
                //Encode the barcode
                Bitmap barcodeBitmap = getBarcodeBitmap(BarcodeFormat.CODE_128, 600, 300);
                //Send data to the printer for printing.
                sendZplOverBluetooth(this.posSystem.getPrinterBluetoothAddress(),barcodeBitmap);
            }

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    private Bitmap getBarcodeBitmap(BarcodeFormat format, int img_width, int img_height) throws WriterException {
        String contentsToEncode = product.getBarcode();

        if (contentsToEncode == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contentsToEncode);
        if (encoding != null) {
            hints = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contentsToEncode, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

        return bitmap;

    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;
    }

    private  void sendZplOverBluetooth(final String theBtMacAddress, final Bitmap barcodeBitmap) {
        new Thread(new Runnable() {
            public void run() {


                try {
                    // Instantiate insecure connection for given Bluetooth MAC Address.
                    Connection insecureBluetoothConnection = new BluetoothConnectionInsecure(theBtMacAddress);
                    Log.d("PrinterMac",theBtMacAddress);
                    // Initialize pritn job
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    insecureBluetoothConnection.open();
                    //Create an instance of the printer
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(insecureBluetoothConnection);
                    PrinterStatus printerStatus = printer.getCurrentStatus();

                    if (printerStatus.isReadyToPrint) {

                        String printProductContent = product.getName()+":$"+product.getValue();
                        String position = "200,10";
                        if(printProductContent.length()<15) {
                            printProductContent = printProductContent + "\t\t";
                            position = "250,10";
                        }
                        else if(printProductContent.length()>15 && printProductContent.length()<20)
                            position = "200,10";
                        else
                            position = "100,10";
                        String productDetailsZPL = "^XA^MNN^LL90" +
                                "^FO"+position+"^ADI,30,20^FD"+printProductContent+
                                "^FS" +
                                "^XZ";

                        String separatorZPL = "^XA^MNN^LL90^FO90,20^A0N,25,25^FD--------------------------------------^FS^XZ";
                        // Send the data to printer as a byte array.

                        insecureBluetoothConnection.write(separatorZPL.getBytes());
                        insecureBluetoothConnection.write(productDetailsZPL.getBytes());
                        printer.printImage(new ZebraImageAndroid(barcodeBitmap), -150, 0, 550, 412, false);
                        insecureBluetoothConnection.write(separatorZPL.getBytes());

                    }
                    else if (printerStatus.isPaused) {
                        Toast.makeText(ProductDetailsActivity.this, "Cannot Print because the printer is paused.", Toast.LENGTH_SHORT).show();
                    }
                    else if (printerStatus.isHeadOpen) {
                        Toast.makeText(ProductDetailsActivity.this, "Please check the printer media door.", Toast.LENGTH_SHORT).show();
                    }
                    else if (printerStatus.isPaperOut) {
                        Toast.makeText(ProductDetailsActivity.this, "Cannot Print because the paper is out.", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        Toast.makeText(ProductDetailsActivity.this, "Something went wrong! Please try again", Toast.LENGTH_SHORT).show();
                    }

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(500);

                    // Close the insecure connection to release resources.
                    insecureBluetoothConnection.close();

                    Looper.myLooper().quit();

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
