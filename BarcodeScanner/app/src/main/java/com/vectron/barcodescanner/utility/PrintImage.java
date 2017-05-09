package com.vectron.barcodescanner.utility;

import android.graphics.Bitmap;
import android.os.Looper;


import com.zebra.sdk.comm.BluetoothConnectionInsecure;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.graphics.internal.ZebraImageAndroid;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

/**
 * Created by Vish on 9/05/2017.
 */
public class PrintImage implements Serializable {
    private  void sendZplOverBluetooth(final String theBtMacAddress, final Bitmap prodcutBitmap, final Bitmap barcodeBitmap) {
        new Thread(new Runnable() {
            public void run() {


                try {
                    // Instantiate insecure connection for given Bluetooth MAC Address.
                    Connection thePrinterConn = new BluetoothConnectionInsecure(theBtMacAddress);


                    // Verify the printer is ready to print


                    // Initialize
                    Looper.prepare();

                    // Open the connection - physical connection is established here.
                    thePrinterConn.open();

                    // This example prints "This is a ZPL test." near the top of the label.
                    ZebraPrinter printer = ZebraPrinterFactory.getInstance(thePrinterConn);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    barcodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    // Send the data to printer as a byte array.

                    printer.printImage(new ZebraImageAndroid(barcodeBitmap), 0, 0, 550, 412, false);

                    // Make sure the data got to the printer before closing the connection
                    Thread.sleep(500);

                    // Close the insecure connection to release resources.
                    thePrinterConn.close();

                    Looper.myLooper().quit();

                } catch (Exception e) {
                    // Handle communications error here.
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
