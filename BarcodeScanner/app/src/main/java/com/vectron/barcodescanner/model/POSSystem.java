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
    private List<Venue> venues;
    private List<String> venueNames;

    public POSSystem(){
        venues = new ArrayList<Venue>();
        venueNames = new ArrayList<String>();
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
        Iterator venues = this.venues.iterator();
        while(venues.hasNext()){
            Venue venueObject = (Venue) venues.next();
            if(venueObject.getId() == venue.getId())
                venueObject = venue;
        }
    }
}
