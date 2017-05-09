package com.vectron.barcodescanner.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Vish on 27/04/2017.
 */
public class Venue implements Serializable {
    private long id;
    private String name;
    private Map<String,String> priceName;
    private List<Store> stores;
    private List<String> storeNames;

    public Venue(){
        priceName = new HashMap<String,String>();
        stores = new ArrayList<Store>();
        storeNames = new ArrayList<String>();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getPriceName() {
        return priceName;
    }

    public List<Store> getStores() {
        return stores;
    }

    public void addStore(Store store){
        if(!stores.contains(store)) {
            stores.add(store);
            this.addStoreName(store.getName());
        }
    }

    public void addPriceName(String key, String value){
        if(!priceName.containsKey(key)){
            priceName.put(key, value);
        }
    }
    public void addStoreName(String name){
        this.storeNames.add(name);
    }

    public List<String> getStoreNames() {
        return storeNames;
    }

    public List<String> getPriceNameList(){
        List<String> priceNames = new ArrayList<String>();
        Set keys = this.priceName.keySet();
        Iterator keyIterator = keys.iterator();
        while(keyIterator.hasNext()){
            String key = (String)keyIterator.next();
            priceNames.add(this.priceName.get(key));
        }
        return priceNames;
    }
}