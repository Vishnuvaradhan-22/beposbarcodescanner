package com.vectron.barcodescanner.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Vish on 26/04/2017.
 */
public class POSSystem implements Serializable {
    private String apiAdress;
    private String printerBluetoothAddress;
    private Venue venueStored;
    private Store storeSelected;
    private String priceNameSelected;
    private List<Venue> venues;
    private List<String> venueNames;


    public POSSystem(){
        venues = new ArrayList<Venue>();
        venueNames = new ArrayList<String>();
        venueStored = new Venue();
        storeSelected = new Store();
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

    public List<String> getVenueNames() {
        return venueNames;
    }

    public void addVenueName(String name){
        if(!venueNames.contains(name))
            venueNames.add(name);
    }
    public void updateVenue(Venue venue){
        for(int i=0;i<venues.size();i++){
            if(venues.get(i).getId() == venue.getId()){
                venues.set(i,venue);
            }
        }
    }

    public Venue getVenueStored() {
        return venueStored;
    }

    public void setVenueStored(Venue venueStored) {
        this.venueStored = venueStored;
    }

    public Store getStoreSelected() {
        return storeSelected;
    }

    public void setStoreSelected(Store storeSelected) {
        this.storeSelected = storeSelected;
    }

    public String getPriceNameSelected() {
        return priceNameSelected;
    }

    public void setPriceNameSelected(String priceNameSelected) {
        this.priceNameSelected = priceNameSelected;
    }
}
