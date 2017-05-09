package com.vectron.barcodescanner.model;

import java.io.Serializable;

/**
 * Created by Vish on 27/04/2017.
 */
public class Store implements Serializable {
    private long id;
    private String name;

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
}
