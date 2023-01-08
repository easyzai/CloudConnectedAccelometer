package com.awaissaikhu.cloudconnectedaccelometer;

import org.json.JSONException;
import org.json.JSONObject;

public class Points {
    float x,y,z;
    public Points(float x,float y,float z){
        this.x=x;
        this.y=y;
        this.z=z;
    }
    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("x", x);
            obj.put("y", y);
            obj.put("z", z);
        } catch (JSONException e) {

        }
        return obj;
    }
}
