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
    private List<String> listOfVenues;
    private List<String> listOfStores;
    private List<String> listOfPriceNames;

    public POSSystem(){
        listOfVenues = new ArrayList<String>();
        listOfVenues = new ArrayList<String>();
        listOfPriceNames = new ArrayList<String>();
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

    public List<String> getListOfVenues() {
        return listOfVenues;
    }

    public void setListOfVenues(List<String> listOfVenues) {
        this.listOfVenues = listOfVenues;
    }

    public List<String> getListOfStores() {
        return listOfStores;
    }

    public void setListOfStores(List<String> listOfStores) {
        this.listOfStores = listOfStores;
    }

    public List<String> getListOfPriceNames() {
        return listOfPriceNames;
    }

    public void setListOfPriceNames(List<String> listOfPriceNames) {
        this.listOfPriceNames = listOfPriceNames;
    }

    public void addVenue(String venue){
        if(!this.listOfVenues.contains(venue))
            listOfVenues.add(venue);
    }

    public void addStore(String store){
        if(!this.listOfStores.contains(store))
            listOfStores.add(store);
    }

    public void  addPriceName(String priceName){
        if(!this.listOfPriceNames.contains(priceName))
            listOfPriceNames.add(priceName);
    }
}
