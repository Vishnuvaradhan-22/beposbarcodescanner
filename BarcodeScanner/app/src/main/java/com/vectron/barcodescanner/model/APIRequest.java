package com.vectron.barcodescanner.model;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vish on 27/04/2017.
 */
public class APIRequest {
    private String url;
    private String endPoint;
    private Map<String,String> parameters;
    private String type;

    public APIRequest(){
        parameters = new HashMap<String,String>();
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addParameter(String key, String value){
        if(!parameters.containsKey(key)){
            parameters.put(key, value);
        }
    }

    public Map<String, String> getParameters() {
        return parameters;
    }
}
