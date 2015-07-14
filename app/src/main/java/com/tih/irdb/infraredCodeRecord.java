package com.tih.irdb;

import android.graphics.Color;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by pichu on 西元15/6/18.
 */
public class InfraredCodeRecord {
    float x;
    float y;
    int color;


    int device_id;
    String code;
    String functionCode;

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }


    public InfraredCodeRecord(float inputX,float inputY,String inputCode){
        x = inputX;
        y = inputY;
        color = Color.BLUE;
        code = inputCode;
    }

    public String getPostionString() {
        if(functionCode == null)
            return "Long Click to Rename";
        else
            return functionCode;
    }

    public String getCode() {
        return code;
    }

    public String getFunctionCode() {
        return functionCode;
    }


    public int getDeviceId() {
        return device_id;
    }

    public void setDeviceId(int device_id) {
        this.device_id = device_id;
    }

    public String toString(){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("x", x);
            jsonObject.put("y", y);
            jsonObject.put("color", color);
            jsonObject.put("device_id", device_id);
            jsonObject.put("code", code);
            jsonObject.put("function_code", functionCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

}
