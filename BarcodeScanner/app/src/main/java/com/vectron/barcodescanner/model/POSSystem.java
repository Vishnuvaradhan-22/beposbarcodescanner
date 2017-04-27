package com.vectron.barcodescanner.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Vish on 26/04/2017.
 */
public class POSSystem implements Serializable {
    private String apiAdress;
    private String printerBluetoothAddress;
    private List<Venue> venues;

    public POSSystem(){
        venues = new ArrayList<Venue>();
    }
    public String getApiAdress() {
        return apiAdress;
    }

    public void setApiAdress(String apiAdress) {
        this.apiAdress = apiAdress;
    }

    public String getPrinterBluetoothAddress() {
        return printerBluetoothAddress;
    }

    public void setPrinterBluetoothAddress(String printerBluetoothAddress) {
        this.printerBluetoothAddress = printerBluetoothAddress;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public void addVenue(Venue venue){
        if(!venues.contains(venue))
            venues.add(venue);
    }
}
