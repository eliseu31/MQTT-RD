/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sniffer.list.utils;

import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author eliseu
 */
public class ListWriter {
    private JSONObject listJSON;
    private final String listPath;

    public ListWriter(String listPath) {
        this.listPath = listPath;
    }
    
    public void setList(JSONObject listJSON){
         this.listJSON = listJSON;
    }
    
    public synchronized void saveListFile(){
        FileWriter file;
        try {
            file = new FileWriter(listPath);
            file.write(listJSON.toString(2));
            file.flush();
        } catch (IOException | JSONException ex) {
            System.err.println("Can not save the list in a file.");
        }
    }
    
    public void resetList() throws JSONException{
        JSONObject resetList = new JSONObject();
        JSONArray sniffersArray = new JSONArray();
        resetList.put("sniffers", sniffersArray);
        setList(resetList);
        saveListFile();
    }
    
    public void addSniffer(JSONObject snifferJSON) throws JSONException{
        JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
        sniffersArray.put(snifferJSON);
        listJSON.put("sniffers", sniffersArray);
    }
    
    public void addDevice(String snifferID, JSONObject deviceJSON) throws JSONException {
        ListReader reader = new ListReader(listPath);
        
        int positionSniffer = reader.findSniffer(snifferID);
        int positionDevice = reader.findDevice(positionSniffer, deviceJSON.getString("device_id"));
        if (positionDevice < 0) {
            //deviceJSON.remove("entity_type");
            //deviceJSON.remove("entity_name");
            
            JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
            JSONObject snifferJSON = (JSONObject) sniffersArray.get(positionSniffer);
            JSONArray devicesArray = snifferJSON.getJSONArray("devices");
            devicesArray.put(deviceJSON);
            snifferJSON.put("devices", devicesArray);
            sniffersArray.put(positionSniffer, snifferJSON);
            listJSON.put("sniffers", sniffersArray);
        }
    }
    
    //removeDevice
    public void removeDevice(String snifferID, String deviceID) throws JSONException{
        ListReader reader = new ListReader(listPath);
        
        int positionSniffer = reader.findSniffer(snifferID);
        int positionDevice = reader.findDevice(positionSniffer, deviceID);
        if(positionDevice >= 0){
            JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
            JSONObject snifferJSON = (JSONObject) sniffersArray.get(positionSniffer);
            JSONArray devicesArray = snifferJSON.getJSONArray("devices");
            
            devicesArray.remove(positionDevice);
            
            snifferJSON.put("devices", devicesArray);
            sniffersArray.put(positionSniffer, snifferJSON);
            listJSON.put("sniffers", sniffersArray);
        }
    }
    
    //removeSniffer
    public void removeSniffer(String snifferID) throws JSONException{
        ListReader reader = new ListReader(listPath);
        
        int positionSniffer = reader.findSniffer(snifferID);
        if(positionSniffer >= 0){
            JSONArray sniffersArray = listJSON.getJSONArray("sniffers");
            
            sniffersArray.remove(positionSniffer);
            
            listJSON.put("sniffers", sniffersArray);
        }
    }
}
